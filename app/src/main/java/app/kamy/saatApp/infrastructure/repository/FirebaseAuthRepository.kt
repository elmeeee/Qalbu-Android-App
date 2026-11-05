package app.kamy.saatApp.infrastructure.repository

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.AuthUser
import app.kamy.saatApp.domain.repository.AuthRepository
import app.kamy.saatApp.domain.util.DisposableEmailValidator
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import app.kamy.saatApp.domain.model.BrandAvatar
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val _currentUser = MutableStateFlow(auth.currentUser?.toAuthUser())
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            val keep = _currentUser.value?.takeIf { it.uid == fbUser?.uid }?.avatarKey
            _currentUser.value = fbUser?.toAuthUser(keep)
            if (fbUser != null) {
                // Backfill: any account whose name exists in Auth gets it in Firestore too.
                fbUser.displayName?.takeIf { it.isNotBlank() }?.let { saveDisplayName(fbUser.uid, it) }
                ensureBrandAvatar(fbUser.uid)
            }
        }
    }

    /**
     * Reads `users/{uid}.avatarKey`. If the account has none yet, picks a random brand avatar
     * and stores it (merge, so other fields are untouched). Runs in the background; the UI
     * shows the initial letter until the key arrives.
     */
    private fun ensureBrandAvatar(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val existing = BrandAvatar.fromKey(doc.getString("avatarKey"))
                if (existing != null) {
                    applyAvatarKey(existing.key)
                } else {
                    val picked = BrandAvatar.random()
                    firestore.collection("users").document(uid)
                        .set(mapOf("avatarKey" to picked.key), SetOptions.merge())
                        .addOnSuccessListener { applyAvatarKey(picked.key) }
                }
            }
    }

    /** Writes the name to users/{uid} (merge). Failures are logged; Auth still holds the name. */
    private fun saveDisplayName(uid: String, name: String) {
        firestore.collection("users").document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "displayName" to name.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .addOnFailureListener { Log.w(TAG, "Failed to save displayName to Firestore", it) }
    }

    private fun applyAvatarKey(key: String) {
        _currentUser.update { it?.copy(avatarKey = key) }
    }

    /** Lets the user switch to another brand avatar; persisted to Firestore. */
    override suspend fun setAvatarKey(key: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = auth.currentUser?.uid ?: throw Exception(appContext.getString(R.string.auth_error_user_not_signed_in))
            firestore.collection("users").document(uid)
                .set(mapOf("avatarKey" to key), SetOptions.merge()).await()
            applyAvatarKey(key)
        }
    }

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val serverClientId = context.getString(R.string.default_web_client_id)
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = try {
                    credentialManager.getCredential(
                        request = request,
                        context = context
                    )
                } catch (e: GetCredentialCancellationException) {
                    throw CancellationException(appContext.getString(R.string.auth_error_login_cancelled))
                } catch (e: GetCredentialException) {
                    Log.e(TAG, "GetCredential error: ${e.message}", e)
                    throw Exception("${appContext.getString(R.string.auth_error_google_failed)}: ${e.message ?: ""}")
                }

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val user = authResult.user?.toAuthUser()
                        ?: throw Exception(appContext.getString(R.string.auth_error_user_not_found))
                    _currentUser.value = user
                    user
                } else {
                    throw Exception(appContext.getString(R.string.auth_error_generic))
                }
            }
        }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                try {
                    val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
                    val user = authResult.user?.toAuthUser()
                        ?: throw Exception(appContext.getString(R.string.auth_error_user_not_found))
                    _currentUser.value = user
                    user
                } catch (e: FirebaseAuthException) {
                    throw Exception(mapFirebaseAuthError(e))
                } catch (e: Exception) {
                    throw Exception(e.message ?: appContext.getString(R.string.auth_error_sign_in_failed))
                }
            }
        }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<AuthUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cleanEmail = email.trim()
                if (DisposableEmailValidator.isDisposable(cleanEmail)) {
                    throw Exception(appContext.getString(R.string.auth_error_disposable_email))
                }

                val cleanName = name.trim()
                if (cleanName.isBlank()) {
                    throw Exception(appContext.getString(R.string.auth_error_empty_name))
                }

                try {
                    val authResult = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                    val firebaseUser = authResult.user ?: throw Exception(appContext.getString(R.string.auth_error_create_account_failed))

                    val profileUpdates = userProfileChangeRequest {
                        displayName = cleanName
                    }
                    firebaseUser.updateProfile(profileUpdates).await()
                    firebaseUser.reload().await()

                    val user = (auth.currentUser ?: firebaseUser).toAuthUser()
                    _currentUser.value = user
                    saveDisplayName(user.uid, cleanName)
                    user
                } catch (e: FirebaseAuthException) {
                    throw Exception(mapFirebaseAuthError(e))
                } catch (e: Exception) {
                    throw Exception(e.message ?: appContext.getString(R.string.auth_error_create_account_failed))
                }
            }
        }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                try {
                    auth.sendPasswordResetEmail(email.trim()).await()
                    Unit
                } catch (e: FirebaseAuthException) {
                    throw Exception(mapFirebaseAuthError(e))
                } catch (e: Exception) {
                    throw Exception(e.message ?: appContext.getString(R.string.auth_error_reset_failed))
                }
            }
        }

    override suspend fun updateProfile(displayName: String): Result<AuthUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val firebaseUser = auth.currentUser ?: throw Exception(appContext.getString(R.string.auth_error_user_not_signed_in))
                val cleanName = displayName.trim()
                if (cleanName.isBlank()) {
                    throw Exception(appContext.getString(R.string.auth_error_empty_name))
                }
                val profileUpdates = userProfileChangeRequest {
                    this.displayName = cleanName
                }
                firebaseUser.updateProfile(profileUpdates).await()
                firebaseUser.reload().await()
                // Keep the brand avatar: a name change must not reset it.
                val keepAvatar = _currentUser.value?.takeIf { it.uid == firebaseUser.uid }?.avatarKey
                val updatedUser = (auth.currentUser ?: firebaseUser).toAuthUser(keepAvatar)
                _currentUser.value = updatedUser
                // Persist to users/{uid} in the background. Firestore's write Task completes only
                // after server acknowledgement, so awaiting it would keep the UI loading when offline.
                saveDisplayName(firebaseUser.uid, cleanName)
                updatedUser
            }
        }

    override suspend fun signOut(context: Context): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                auth.signOut()
                _currentUser.value = null
                try {
                    val credentialManager = CredentialManager.create(context)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Gagal membersihkan sesi kredensial: ${e.message}")
                }
                Unit
            }
        }

    override suspend fun deleteAccount(context: Context): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val user = auth.currentUser ?: throw Exception(appContext.getString(R.string.auth_error_user_not_signed_in))
                try {
                    user.delete().await()
                } catch (e: Exception) {
                    val msg = e.localizedMessage.orEmpty()
                    val isRecentLoginRequired = e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ||
                        (e is FirebaseAuthException && (
                            e.errorCode in setOf("ERROR_REQUIRES_RECENT_LOGIN", "requires-recent-login", "ERROR_USER_TOKEN_EXPIRED", "user-token-expired", "ERROR_CREDENTIAL_TOO_OLD_LOGIN_AGAIN", "credential-too-old-login-again") ||
                            msg.contains("credential is no longer valid", ignoreCase = true) ||
                            msg.contains("sign in again", ignoreCase = true) ||
                            msg.contains("recent", ignoreCase = true)
                        ))

                    if (isRecentLoginRequired) {
                        val isGoogleUser = user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
                        if (isGoogleUser) {
                            val serverClientId = context.getString(R.string.default_web_client_id)
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(serverClientId)
                                .setAutoSelectEnabled(false)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val credResult = try {
                                credentialManager.getCredential(
                                    context = context,
                                    request = request
                                )
                            } catch (cancelEx: GetCredentialCancellationException) {
                                throw CancellationException(appContext.getString(R.string.auth_error_login_cancelled))
                            } catch (ex: Exception) {
                                throw Exception(appContext.getString(R.string.auth_error_recent_login_required))
                            }
                            val cred = credResult.credential
                            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
                                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                                user.reauthenticate(firebaseCredential).await()
                                user.delete().await()
                            } else {
                                throw Exception(appContext.getString(R.string.auth_error_recent_login_required))
                            }
                        } else {
                            throw Exception(appContext.getString(R.string.auth_error_recent_login_required))
                        }
                    } else if (e is FirebaseAuthException) {
                        throw Exception(mapFirebaseAuthError(e))
                    } else {
                        throw Exception(e.message ?: appContext.getString(R.string.auth_error_delete_failed))
                    }
                }
                _currentUser.value = null
                try {
                    val credentialManager = CredentialManager.create(context)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Gagal membersihkan sesi kredensial: ${e.message}")
                }
                Unit
            }
        }

    private fun mapFirebaseAuthError(e: FirebaseAuthException): String = when (e.errorCode) {
        "ERROR_REQUIRES_RECENT_LOGIN", "requires-recent-login",
        "ERROR_CREDENTIAL_TOO_OLD_LOGIN_AGAIN", "credential-too-old-login-again",
        "ERROR_USER_TOKEN_EXPIRED", "user-token-expired" ->
            appContext.getString(R.string.auth_error_recent_login_required)
        "ERROR_INVALID_EMAIL", "invalid-email" ->
            appContext.getString(R.string.auth_error_invalid_email)
        "ERROR_WRONG_PASSWORD", "wrong-password", "invalid-credential" ->
            appContext.getString(R.string.auth_error_wrong_credentials)
        "ERROR_USER_NOT_FOUND", "user-not-found" ->
            appContext.getString(R.string.auth_error_user_not_found)
        "ERROR_USER_DISABLED", "user-disabled" ->
            appContext.getString(R.string.auth_error_user_disabled)
        "ERROR_EMAIL_ALREADY_IN_USE", "email-already-in-use" ->
            appContext.getString(R.string.auth_error_email_already_in_use)
        "ERROR_WEAK_PASSWORD", "weak-password" ->
            appContext.getString(R.string.auth_error_weak_password)
        "ERROR_TOO_MANY_REQUESTS", "too-many-requests" ->
            appContext.getString(R.string.auth_error_too_many_requests)
        else -> {
            val msg = e.localizedMessage.orEmpty()
            if (msg.contains("credential is no longer valid", ignoreCase = true) ||
                msg.contains("sign in again", ignoreCase = true) ||
                msg.contains("recent", ignoreCase = true)) {
                appContext.getString(R.string.auth_error_recent_login_required)
            } else {
                e.localizedMessage ?: appContext.getString(R.string.auth_error_generic)
            }
        }
    }

    // Must not read _currentUser: it is called while the property is still being initialised.
    private fun FirebaseUser.toAuthUser(avatarKey: String? = null): AuthUser =
        AuthUser(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            avatarKey = avatarKey,
            providerIds = providerData.map { it.providerId }
        )

    private class CancellationException(message: String) : Exception(message)

    companion object {
        private const val TAG = "FirebaseAuthRepo"
    }
}


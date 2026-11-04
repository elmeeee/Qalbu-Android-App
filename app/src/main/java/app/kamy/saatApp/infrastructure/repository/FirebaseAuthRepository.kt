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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    private val _currentUser = MutableStateFlow(auth.currentUser?.toAuthUser())
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser?.toAuthUser()
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
                    throw CancellationException("Login dibatalkan oleh pengguna")
                } catch (e: GetCredentialException) {
                    Log.e(TAG, "GetCredential error: ${e.message}", e)
                    throw Exception("Gagal mendapatkan akun Google: ${e.message ?: "Silakan coba lagi"}")
                }

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val user = authResult.user?.toAuthUser()
                        ?: throw Exception("User tidak ditemukan setelah autentikasi")
                    _currentUser.value = user
                    user
                } else {
                    throw Exception("Kredensial tidak valid dari Google")
                }
            }
        }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                try {
                    val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
                    val user = authResult.user?.toAuthUser()
                        ?: throw Exception("User tidak ditemukan")
                    _currentUser.value = user
                    user
                } catch (e: FirebaseAuthException) {
                    throw Exception(mapFirebaseAuthError(e))
                } catch (e: Exception) {
                    throw Exception(e.message ?: "Gagal masuk dengan email")
                }
            }
        }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<AuthUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                try {
                    val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                    val firebaseUser = authResult.user ?: throw Exception("Gagal membuat akun")

                    if (name.isNotBlank()) {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = name.trim()
                        }
                        firebaseUser.updateProfile(profileUpdates).await()
                    }

                    val user = auth.currentUser?.toAuthUser() ?: firebaseUser.toAuthUser()
                    _currentUser.value = user
                    user
                } catch (e: FirebaseAuthException) {
                    throw Exception(mapFirebaseAuthError(e))
                } catch (e: Exception) {
                    throw Exception(e.message ?: "Gagal mendaftar akun")
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
                    throw Exception(e.message ?: "Gagal mengirim email reset kata sandi")
                }
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
                val user = auth.currentUser ?: throw Exception("Tidak ada pengguna yang sedang masuk")
                try {
                    user.delete().await()
                } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                    throw Exception("Demi keamanan, silakan keluar lalu masuk kembali sebelum menghapus akun.")
                } catch (e: FirebaseAuthException) {
                    throw Exception(mapFirebaseAuthError(e))
                } catch (e: Exception) {
                    throw Exception(e.message ?: "Gagal menghapus akun")
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
        "ERROR_INVALID_EMAIL", "invalid-email" -> "Format email tidak valid"
        "ERROR_WRONG_PASSWORD", "wrong-password", "invalid-credential" -> "Email atau kata sandi salah"
        "ERROR_USER_NOT_FOUND", "user-not-found" -> "Akun dengan email ini tidak ditemukan"
        "ERROR_USER_DISABLED", "user-disabled" -> "Akun telah dinonaktifkan"
        "ERROR_EMAIL_ALREADY_IN_USE", "email-already-in-use" -> "Email ini sudah terdaftar. Silakan masuk."
        "ERROR_WEAK_PASSWORD", "weak-password" -> "Kata sandi terlalu lemah (minimal 6 karakter)"
        "ERROR_TOO_MANY_REQUESTS", "too-many-requests" -> "Terlalu banyak percobaan. Silakan coba beberapa saat lagi."
        else -> e.localizedMessage ?: "Terjadi kesalahan autentikasi"
    }

    private fun FirebaseUser.toAuthUser(): AuthUser =
        AuthUser(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString()
        )

    private class CancellationException(message: String) : Exception(message)

    companion object {
        private const val TAG = "FirebaseAuthRepo"
    }
}

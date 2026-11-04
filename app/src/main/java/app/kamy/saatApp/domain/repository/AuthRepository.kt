package app.kamy.saatApp.domain.repository

import android.content.Context
import app.kamy.saatApp.domain.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    suspend fun signInWithGoogle(context: Context): Result<AuthUser>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<AuthUser>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun updateProfile(displayName: String): Result<AuthUser>
    suspend fun signOut(context: Context): Result<Unit>
    suspend fun deleteAccount(context: Context): Result<Unit>
}

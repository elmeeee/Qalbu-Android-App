package app.kamy.saatApp.core.util

import com.google.firebase.auth.FirebaseAuth

object UserProfileUtils {
    /**
     * Returns the first name of the currently authenticated Firebase user,
     * or null if the user is not signed in or has no display name.
     */
    fun getCurrentUserFirstName(): String? {
        val user = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull() ?: return null
        return user.displayName?.trim()?.split("\\s+".toRegex())?.firstOrNull()?.takeIf { it.isNotBlank() }
    }
}


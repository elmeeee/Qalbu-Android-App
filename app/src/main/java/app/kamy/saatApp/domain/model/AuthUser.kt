package app.kamy.saatApp.domain.model

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    /** Brand avatar key chosen for this account (see [BrandAvatar]); null until assigned. */
    val avatarKey: String? = null
) {
    val firstName: String?
        get() = displayName?.trim()?.split("\\s+".toRegex())?.firstOrNull()?.takeIf { it.isNotBlank() }
}

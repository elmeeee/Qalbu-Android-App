package app.kamy.saatApp.domain.model

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    /** Brand avatar key chosen for this account (see [BrandAvatar]); null until assigned. */
    val avatarKey: String? = null,
    /** Firebase provider ids, e.g. "google.com" or "password". */
    val providerIds: List<String> = emptyList()
) {
    val firstName: String?
        get() = displayName?.trim()?.split("\\s+".toRegex())?.firstOrNull()?.takeIf { it.isNotBlank() }
}

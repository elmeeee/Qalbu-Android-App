package app.kamy.saatApp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileAvatarUrls(
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null
)

@Serializable
data class UserProfileSettings(
    val ayahLanguages: List<Int>? = null,
    val reflectionLanguages: List<Int>? = null
)

@Serializable
data class UserProfilePayload(
    val avatarUrls: UserProfileAvatarUrls? = null,
    val createdAt: String? = null,
    val joiningYear: Int? = null,
    val isPasswordSet: Boolean? = null,
    val settings: UserProfileSettings? = null,
    val username: String? = null,
    @Serializable(with = StringFromAnyScalarSerializer::class)
    val id: String,
    val verified: Boolean? = null,
    val postAs: Boolean? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val postsCount: Int? = null,
    val averageToxicity: Double? = null,
    val languageId: Int? = null,
    val banned: Boolean? = null,
    val memberType: Int? = null,
    val followersCount: Int? = null,
    val likesCount: Int? = null,
    val isAdmin: Boolean? = null,
    val languageIsoCode: String? = null,
    val bio: String? = null,
    val country: String? = null,
    val followed: Boolean? = null
) {
    val displayTitle: String
        get() {
            val parts = listOfNotNull(firstName, lastName).filter { it.isNotEmpty() }
            if (parts.isNotEmpty()) return parts.joinToString(" ")
            if (!username.isNullOrEmpty()) return username
            return if (id.isEmpty()) "Profile" else id
        }

    val preferredAvatarUrl: String?
        get() = avatarUrls?.medium ?: avatarUrls?.large ?: avatarUrls?.small
}

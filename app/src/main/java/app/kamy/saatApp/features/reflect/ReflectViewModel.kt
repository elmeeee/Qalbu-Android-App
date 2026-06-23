package app.kamy.saatApp.features.reflect

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.R
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.error.AppErrorKind
import app.kamy.saatApp.core.error.toAppError
import app.kamy.saatApp.core.error.invalidateIfAuthenticationFailure
import app.kamy.saatApp.core.error.isAuthenticationFailure
import app.kamy.saatApp.domain.model.ReflectFeedPost
import app.kamy.saatApp.infrastructure.auth.UserSession
import app.kamy.saatApp.infrastructure.repository.ReflectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReflectSegment { ALL, MINE }

data class ReflectUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isAuthenticated: Boolean = false,
    val segment: ReflectSegment = ReflectSegment.ALL,
    val posts: List<ReflectFeedPost> = emptyList(),
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val error: AppError? = null,
    val togglingLikePostIds: Set<String> = emptySet(),
    val togglingFollowAuthorIds: Set<String> = emptySet(),
    val currentUserId: String? = null
)

@HiltViewModel
class ReflectViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: ReflectRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(
        ReflectUiState(isAuthenticated = userSession.isSignedIn.value)
    )
    val state: StateFlow<ReflectUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userSession.isSignedIn.collect { signedIn ->
                _state.update { it.copy(isAuthenticated = signedIn) }
                if (signedIn) {
                    if (_state.value.posts.isEmpty()) loadPosts(reset = true)
                    fetchCurrentUserId()
                }
            }
        }
        if (_state.value.isAuthenticated) {
            loadPosts(reset = true)
            fetchCurrentUserId()
        }
    }

    fun switchSegment(segment: ReflectSegment) {
        if (segment == _state.value.segment) return
        _state.update {
            it.copy(
                segment = segment,
                posts = emptyList(),
                currentPage = 0,
                hasMore = true,
                error = null
            )
        }
        loadPosts(reset = true)
    }

    fun loadPosts(reset: Boolean = false) {
        viewModelScope.launch { loadPostsInternal(reset) }
    }

    suspend fun refreshFeed() {
        if (!_state.value.isAuthenticated) return
        loadPostsInternal(reset = true)
    }

    private suspend fun loadPostsInternal(reset: Boolean) {
        val s = _state.value
        if (!s.isAuthenticated) return
        if (!reset && (s.isLoadingMore || !s.hasMore)) return

        val targetPage = if (reset) 1 else s.currentPage + 1
        _state.update {
            if (reset) it.copy(isLoading = true, error = null)
            else it.copy(isLoadingMore = true)
        }
        try {
            val envelope = when (s.segment) {
                ReflectSegment.ALL -> repository.fetchAllReflectFeed(targetPage)
                ReflectSegment.MINE -> repository.fetchMyReflections(targetPage)
            }
            val newPosts = envelope.items
            _state.update {
                val combined = if (reset) newPosts else it.posts + newPosts
                val page = envelope.currentPage ?: targetPage
                val totalPages = envelope.pages
                val pageLimit = envelope.limit ?: 20
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    posts = combined,
                    currentPage = page,
                    hasMore = when {
                        totalPages != null -> page < totalPages
                        else -> newPosts.size >= pageLimit
                    }
                )
            }
        } catch (t: Throwable) {
            val signedOut = userSession.invalidateIfAuthenticationFailure(t)
            _state.update {
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isAuthenticated = if (signedOut) false else it.isAuthenticated,
                    error = t.toAppError()
                )
            }
        }
    }

    fun loadMoreIfNeeded(currentIndex: Int) {
        val s = _state.value
        if (s.isLoadingMore || !s.hasMore) return
        if (currentIndex < s.posts.size - 3) return
        loadPosts(reset = false)
    }

    fun toggleLike(postId: String) {
        val current = _state.value
        if (postId in current.togglingLikePostIds) return
        val index = current.posts.indexOfFirst { it.id == postId }.takeIf { it >= 0 } ?: return
        val original = current.posts[index]
        val wasLiked = original.isLiked == true
        val optimistic = original.copy(
            isLiked = !wasLiked,
            likesCount = (original.likesCount ?: 0) + if (wasLiked) -1 else 1
        )
        _state.update {
            it.copy(
                togglingLikePostIds = it.togglingLikePostIds + postId,
                posts = it.posts.toMutableList().also { list -> list[index] = optimistic }
            )
        }
        viewModelScope.launch {
            try {
                val liked = repository.togglePostLike(postId)
                // Verify against server truth (may differ if rapid double-tap).
                _state.update { s ->
                    val refreshedIdx = s.posts.indexOfFirst { it.id == postId }
                    if (refreshedIdx < 0) s.copy(togglingLikePostIds = s.togglingLikePostIds - postId)
                    else {
                        val cur = s.posts[refreshedIdx]
                        val corrected = cur.copy(isLiked = liked)
                        s.copy(
                            togglingLikePostIds = s.togglingLikePostIds - postId,
                            posts = s.posts.toMutableList().also { it[refreshedIdx] = corrected }
                        )
                    }
                }
            } catch (t: Throwable) {
                val signedOut = userSession.invalidateIfAuthenticationFailure(t)
                _state.update { s ->
                    val rollIdx = s.posts.indexOfFirst { it.id == postId }
                    val rolled = if (rollIdx < 0) {
                        s.copy(togglingLikePostIds = s.togglingLikePostIds - postId)
                    } else {
                        s.copy(
                            togglingLikePostIds = s.togglingLikePostIds - postId,
                            posts = s.posts.toMutableList().also { it[rollIdx] = original }
                        )
                    }
                    if (signedOut) {
                        rolled.copy(
                            isAuthenticated = false,
                            error = AppError(AppErrorKind.Unauthorized)
                        )
                    } else {
                        rolled
                    }
                }
            }
        }
    }

    private fun fetchCurrentUserId() {
        viewModelScope.launch {
            try {
                val profile = repository.fetchMyProfile()
                _state.update { it.copy(currentUserId = profile.id) }
            } catch (t: Throwable) {
                // Ignore profile fetch failure for follow button, it just won't hide own follow button
            }
        }
    }

    fun toggleFollowAuthor(authorId: String) {
        val current = _state.value
        if (authorId in current.togglingFollowAuthorIds) return

        val postsByAuthor = current.posts.filter { it.author?.id == authorId }
        if (postsByAuthor.isEmpty()) return

        val firstAuthor = postsByAuthor.first().author
        val wasFollowed = firstAuthor?.followed == true
        val newFollowed = !wasFollowed

        // Optimistically update all posts by this author in the list
        val updatedPosts = current.posts.map { post ->
            if (post.author?.id == authorId) {
                post.copy(author = post.author.copy(followed = newFollowed))
            } else {
                post
            }
        }

        _state.update {
            it.copy(
                togglingFollowAuthorIds = it.togglingFollowAuthorIds + authorId,
                posts = updatedPosts
            )
        }

        viewModelScope.launch {
            try {
                val action = if (newFollowed) "follow" else "unfollow"
                val followedResult = repository.toggleFollow(authorId, action)

                _state.update { s ->
                    val serverPosts = s.posts.map { post ->
                        if (post.author?.id == authorId) {
                            post.copy(author = post.author.copy(followed = followedResult))
                        } else {
                            post
                        }
                    }
                    s.copy(
                        togglingFollowAuthorIds = s.togglingFollowAuthorIds - authorId,
                        posts = serverPosts
                    )
                }
            } catch (t: Throwable) {
                val signedOut = userSession.invalidateIfAuthenticationFailure(t)
                _state.update { s ->
                    val revertedPosts = s.posts.map { post ->
                        if (post.author?.id == authorId) {
                            post.copy(author = post.author.copy(followed = wasFollowed))
                        } else {
                            post
                        }
                    }
                    val rolled = s.copy(
                        togglingFollowAuthorIds = s.togglingFollowAuthorIds - authorId,
                        posts = revertedPosts
                    )
                    if (signedOut) {
                        rolled.copy(
                            isAuthenticated = false,
                            error = AppError(AppErrorKind.Unauthorized)
                        )
                    } else {
                        rolled.copy(
                            error = t.toAppError()
                        )
                    }
                }
            }
        }
    }
}

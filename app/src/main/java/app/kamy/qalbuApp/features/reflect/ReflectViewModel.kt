package app.kamy.qalbuApp.features.reflect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.core.error.QFError
import app.kamy.qalbuApp.domain.model.ReflectFeedPost
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.repository.ReflectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val error: String? = null,
    val togglingLikePostIds: Set<String> = emptySet()
)

/**
 * Mirrors iOS Features/Reflection/ViewModels/ReflectionViewModel.swift.
 *
 * Vertical reel feed with optimistic like updates and pagination at index
 * `posts.size - 3`. Resets pagination on segment switch.
 */
@HiltViewModel
class ReflectViewModel @Inject constructor(
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
                if (signedIn && _state.value.posts.isEmpty()) loadPosts(reset = true)
            }
        }
        if (_state.value.isAuthenticated) loadPosts(reset = true)
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
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    posts = combined,
                    currentPage = envelope.currentPage ?: targetPage,
                    hasMore = (envelope.currentPage ?: targetPage) < (envelope.pages ?: targetPage)
                )
            }
        } catch (e: QFError.MissingUserSession) {
            _state.update {
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isAuthenticated = false,
                    error = "Session expired"
                )
            }
        } catch (t: Throwable) {
            _state.update {
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = t.message ?: "Failed to load feed"
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

    /**
     * Optimistic like toggle: flip UI immediately, await API, roll back on failure.
     * Mirrors iOS ReflectionViewModel.toggleLike.
     */
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
                _state.update { s ->
                    val rollIdx = s.posts.indexOfFirst { it.id == postId }
                    if (rollIdx < 0) s.copy(togglingLikePostIds = s.togglingLikePostIds - postId)
                    else s.copy(
                        togglingLikePostIds = s.togglingLikePostIds - postId,
                        posts = s.posts.toMutableList().also { it[rollIdx] = original }
                    )
                }
            }
        }
    }
}

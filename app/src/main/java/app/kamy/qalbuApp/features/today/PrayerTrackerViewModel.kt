package app.kamy.qalbuApp.features.today

import android.content.Context
import androidx.lifecycle.ViewModel
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.infrastructure.preferences.PrayerDayProgress
import app.kamy.qalbuApp.infrastructure.preferences.PrayerTrackerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PrayerTrackerUiState(
    val todayProgress: PrayerDayProgress = PrayerDayProgress(PrayerTrackerStore.todayKey(), 0),
    val weekProgress: List<PrayerDayProgress> = emptyList(),
    val streak: Int = 0,
    val completedPrayers: Set<PrayerType> = emptySet()
)

@HiltViewModel
class PrayerTrackerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PrayerTrackerUiState())
    val state: StateFlow<PrayerTrackerUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val today = PrayerTrackerStore.todayKey()
        val completed = PrayerTrackerStore.TRACKED_PRAYERS
            .filter { PrayerTrackerStore.isCompleted(appContext, it, today) }
            .toSet()
        _state.update {
            it.copy(
                todayProgress = PrayerTrackerStore.dayProgress(appContext, today),
                weekProgress = PrayerTrackerStore.weekProgress(appContext),
                streak = PrayerTrackerStore.currentStreak(appContext),
                completedPrayers = completed
            )
        }
    }

    fun togglePrayer(prayer: PrayerType) {
        PrayerTrackerStore.toggle(appContext, prayer)
        refresh()
    }

    fun isCompleted(prayer: PrayerType): Boolean =
        _state.value.completedPrayers.contains(prayer)
}

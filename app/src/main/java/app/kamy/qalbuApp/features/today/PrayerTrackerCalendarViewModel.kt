package app.kamy.qalbuApp.features.today

import android.content.Context
import androidx.lifecycle.ViewModel
import app.kamy.qalbuApp.infrastructure.preferences.PrayerDayProgress
import app.kamy.qalbuApp.infrastructure.preferences.PrayerTrackerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

data class PrayerTrackerCalendarUiState(
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val days: List<PrayerDayProgress> = emptyList(),
    val streak: Int = 0,
    val bestStreak: Int = 0
)

@HiltViewModel
class PrayerTrackerCalendarViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PrayerTrackerCalendarUiState())
    val state: StateFlow<PrayerTrackerCalendarUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun previousMonth() {
        val current = _state.value
        val cal = Calendar.getInstance().apply {
            set(current.year, current.month - 1, 1)
            add(Calendar.MONTH, -1)
        }
        loadMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun nextMonth() {
        val current = _state.value
        val cal = Calendar.getInstance().apply {
            set(current.year, current.month - 1, 1)
            add(Calendar.MONTH, 1)
        }
        loadMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    private fun refresh() {
        val cal = Calendar.getInstance()
        loadMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    private fun loadMonth(year: Int, month: Int) {
        _state.update {
            it.copy(
                year = year,
                month = month,
                days = PrayerTrackerStore.monthProgress(appContext, year, month),
                streak = PrayerTrackerStore.currentStreak(appContext),
                bestStreak = PrayerTrackerStore.bestStreak(appContext)
            )
        }
    }
}

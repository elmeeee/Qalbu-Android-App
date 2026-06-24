package app.kamy.saatApp.di

import app.kamy.saatApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import app.kamy.saatApp.infrastructure.repository.AlAdhanRepository
import app.kamy.saatApp.infrastructure.repository.KhgtCalendarRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PrayerRefreshEntryPoint {
    fun alAdhanRepository(): AlAdhanRepository
    fun prayerCalculationStore(): PrayerCalculationStore
    fun khgtCalendarRepository(): KhgtCalendarRepository
    fun prayerNotificationPreferencesStore(): PrayerNotificationPreferencesStore
}

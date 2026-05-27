package app.kamy.qalbuApp.di

import app.kamy.qalbuApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.qalbuApp.infrastructure.repository.AlAdhanRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PrayerRefreshEntryPoint {
    fun alAdhanRepository(): AlAdhanRepository
    fun prayerCalculationStore(): PrayerCalculationStore
}

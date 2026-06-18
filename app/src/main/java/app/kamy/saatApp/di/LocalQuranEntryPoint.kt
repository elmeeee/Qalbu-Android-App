package app.kamy.saatApp.di

import app.kamy.saatApp.infrastructure.local.LocalQuranDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocalQuranEntryPoint {
    fun localQuranDatabase(): LocalQuranDatabase
}

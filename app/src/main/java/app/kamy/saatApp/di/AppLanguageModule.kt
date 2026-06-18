package app.kamy.saatApp.di

import android.content.Context
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppLanguageModule {

    @Provides
    @Singleton
    fun provideAppLanguageStore(@ApplicationContext context: Context): AppLanguageStore =
        AppLanguageStore.from(context)
}

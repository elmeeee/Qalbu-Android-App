package app.kamy.saatApp.di

import app.kamy.saatApp.infrastructure.network.AlAdhanApi
import app.kamy.saatApp.infrastructure.network.AuthV1Api
import app.kamy.saatApp.infrastructure.network.ContentApi
import app.kamy.saatApp.infrastructure.network.api.AlAdhanApiService
import app.kamy.saatApp.infrastructure.network.api.AuthV1ApiService
import app.kamy.saatApp.infrastructure.network.api.ContentApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideContentApi(@ContentApi retrofit: Retrofit): ContentApiService =
        retrofit.create()

    @Provides
    @Singleton
    fun provideAuthV1Api(@AuthV1Api retrofit: Retrofit): AuthV1ApiService =
        retrofit.create()

    @Provides
    @Singleton
    fun provideAlAdhanApi(@AlAdhanApi retrofit: Retrofit): AlAdhanApiService =
        retrofit.create()
}

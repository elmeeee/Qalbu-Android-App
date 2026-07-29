package app.kamy.saatApp.di

import app.kamy.saatApp.infrastructure.network.ContentApi
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
}

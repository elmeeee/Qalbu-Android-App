package app.kamy.qalbuApp.di

import app.kamy.qalbuApp.infrastructure.network.AlAdhanApi
import app.kamy.qalbuApp.infrastructure.network.AuthV1Api
import app.kamy.qalbuApp.infrastructure.network.ContentApi
import app.kamy.qalbuApp.infrastructure.network.ReflectApi
import app.kamy.qalbuApp.infrastructure.network.SearchApi
import app.kamy.qalbuApp.infrastructure.network.api.AlAdhanApiService
import app.kamy.qalbuApp.infrastructure.network.api.AuthV1ApiService
import app.kamy.qalbuApp.infrastructure.network.api.ContentApiService
import app.kamy.qalbuApp.infrastructure.network.api.ReflectApiService
import app.kamy.qalbuApp.infrastructure.network.api.SearchApiService
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
    fun provideReflectApi(@ReflectApi retrofit: Retrofit): ReflectApiService =
        retrofit.create()

    @Provides
    @Singleton
    fun provideAuthV1Api(@AuthV1Api retrofit: Retrofit): AuthV1ApiService =
        retrofit.create()

    @Provides
    @Singleton
    fun provideAlAdhanApi(@AlAdhanApi retrofit: Retrofit): AlAdhanApiService =
        retrofit.create()

    @Provides
    @Singleton
    fun provideSearchApi(@SearchApi retrofit: Retrofit): SearchApiService =
        retrofit.create()
}

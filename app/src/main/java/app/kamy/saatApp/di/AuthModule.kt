package app.kamy.saatApp.di

import app.kamy.saatApp.domain.repository.AuthRepository
import app.kamy.saatApp.domain.repository.UserDataSyncRepository
import app.kamy.saatApp.infrastructure.repository.FirebaseAuthRepository
import app.kamy.saatApp.infrastructure.repository.FirestoreUserDataSyncRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserDataSyncRepository(
        impl: FirestoreUserDataSyncRepository
    ): UserDataSyncRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}


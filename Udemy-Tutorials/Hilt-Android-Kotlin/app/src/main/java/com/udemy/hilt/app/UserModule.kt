package com.udemy.hilt.app

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@InstallIn(SingletonComponent::class)
@Module
class UserModule {

    @Provides
    @Named(Constants.SQL)
    fun providesSQLRepository(): UserRepository {
        return SQLRepository()
    }

    @Named(Constants.FIREBASE)
    @Provides
    fun providesFirebaseRepository(): UserRepository {
        return FirebaseRepository()
    }
}
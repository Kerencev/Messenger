package com.kerencev.messenger.di.modules

import com.kerencev.messenger.model.repository.AuthRepository
import com.kerencev.messenger.model.repository.impl.AuthRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AuthRepositoryModule {

    @Singleton
    @Provides
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }
}
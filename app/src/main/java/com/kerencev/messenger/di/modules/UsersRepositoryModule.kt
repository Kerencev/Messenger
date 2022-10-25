package com.kerencev.messenger.di.modules

import com.kerencev.messenger.model.repository.UsersRepository
import com.kerencev.messenger.model.repository.impl.UsersRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UsersRepositoryModule {

    @Singleton
    @Provides
    fun provideUsersRepository(): UsersRepository {
        return UsersRepositoryImpl()
    }
}
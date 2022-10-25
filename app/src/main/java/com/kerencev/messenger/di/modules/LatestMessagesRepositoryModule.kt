package com.kerencev.messenger.di.modules

import com.kerencev.messenger.model.repository.LatestMessagesRepository
import com.kerencev.messenger.model.repository.impl.LatestMessagesRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LatestMessagesRepositoryModule {

    @Singleton
    @Provides
    fun provideLatestMessagesRepository(): LatestMessagesRepository {
        return LatestMessagesRepositoryImpl()
    }
}
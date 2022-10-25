package com.kerencev.messenger.di.modules

import com.kerencev.messenger.data.remote.NotificationAPI
import com.kerencev.messenger.model.repository.MessagesRepository
import com.kerencev.messenger.model.repository.impl.MessagesRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MessagesRepositoryModule {

    @Singleton
    @Provides
    fun provideMessagesRepository(notificationAPI: NotificationAPI): MessagesRepository {
        return MessagesRepositoryImpl(notificationAPI)
    }
}
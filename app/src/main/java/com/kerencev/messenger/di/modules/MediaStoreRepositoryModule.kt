package com.kerencev.messenger.di.modules

import com.kerencev.messenger.model.repository.MediaStoreRepository
import com.kerencev.messenger.model.repository.impl.MediaStoreRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MediaStoreRepositoryModule {

    @Singleton
    @Provides
    fun provideMediaStoreRepository(): MediaStoreRepository {
        return MediaStoreRepositoryImpl()
    }
}
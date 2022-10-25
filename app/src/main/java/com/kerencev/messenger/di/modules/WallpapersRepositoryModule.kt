package com.kerencev.messenger.di.modules

import com.kerencev.messenger.model.repository.WallpapersRepository
import com.kerencev.messenger.model.repository.impl.WallpapersRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class WallpapersRepositoryModule {

    @Singleton
    @Provides
    fun provideWallpapersRepository(): WallpapersRepository {
        return WallpapersRepositoryImpl()
    }
}
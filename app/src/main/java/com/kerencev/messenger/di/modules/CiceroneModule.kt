package com.kerencev.messenger.di.modules

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CiceroneModule {

    private val navigation = Cicerone.create()

    @Provides
    @Singleton
    fun provideRouter(): Router {
        return navigation.router
    }

    @Provides
    @Singleton
    fun provideCicerone(): Cicerone<Router> {
        return Cicerone.create()
    }

    @Provides
    @Singleton
    fun provideNavigationHolder(): NavigatorHolder {
        return navigation.getNavigatorHolder()
    }
}
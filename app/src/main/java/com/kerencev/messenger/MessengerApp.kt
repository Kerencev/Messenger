package com.kerencev.messenger

import android.app.Application
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import io.reactivex.rxjava3.plugins.RxJavaPlugins

class MessengerApp : Application() {

    private val cicerone: Cicerone<Router> by lazy { Cicerone.create() }
    val navigationHolder = cicerone.getNavigatorHolder()
    val router = cicerone.router

    override fun onCreate() {
        super.onCreate()
        instance = this

        RxJavaPlugins.setErrorHandler {
        }
    }

    companion object {
        lateinit var instance: MessengerApp
    }
}
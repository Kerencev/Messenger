package com.kerencev.messenger

import android.app.Application
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.di.component.AppComponent
import com.kerencev.messenger.di.component.DaggerAppComponent
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.utils.log
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import io.reactivex.rxjava3.plugins.RxJavaPlugins

class MessengerApp : Application() {

    private val cicerone: Cicerone<Router> by lazy { Cicerone.create() }
    val navigationHolder = cicerone.getNavigatorHolder()
    val router = cicerone.router
    var chatPartner: User? = null

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder().build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        RxJavaPlugins.setErrorHandler {
            log(it.stackTraceToString())
        }
        EmojiManager.install(GoogleEmojiProvider())
    }

    companion object {
        lateinit var instance: MessengerApp
    }
}
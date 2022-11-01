package com.kerencev.messenger

import android.app.Application
import androidx.core.content.getSystemService
import com.kerencev.messenger.di.component.AppComponent
import com.kerencev.messenger.di.component.DaggerAppComponent
import com.kerencev.messenger.di.modules.AppModule
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.utils.log
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.File

class MessengerApp : Application() {

    var chatPartner: User? = null

    val appComponent: AppComponent by lazy {
        DaggerAppComponent
            .builder()
            .appModule(AppModule(applicationContext))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        RxJavaPlugins.setErrorHandler {
            log(it.stackTraceToString())
        }
        EmojiManager.install(GoogleEmojiProvider())
    }
}
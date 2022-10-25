package com.kerencev.messenger.di.component

import com.kerencev.messenger.di.modules.*
import com.kerencev.messenger.ui.login.loginactivity.LoginActivity
import com.kerencev.messenger.ui.login.loginactivity.LoginPresenter
import com.kerencev.messenger.ui.main.activity.MainActivity
import com.kerencev.messenger.ui.main.activity.MainPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ApiModule::class,
        AuthRepositoryModule::class,
        CiceroneModule::class,
        LatestMessagesRepositoryModule::class,
        MediaStoreRepositoryModule::class,
        MessagesRepositoryModule::class,
        UsersRepositoryModule::class,
        WallpapersRepositoryModule::class
    ]
)
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(mainPresenter: MainPresenter)
    fun inject(loginPresenter: LoginPresenter)
}
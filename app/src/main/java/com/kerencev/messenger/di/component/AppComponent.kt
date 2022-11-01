package com.kerencev.messenger.di.component

import com.kerencev.messenger.di.modules.*
import com.kerencev.messenger.ui.login.loginactivity.LoginActivity
import com.kerencev.messenger.ui.login.loginactivity.LoginActivityPresenter
import com.kerencev.messenger.ui.login.loginfragment.LoginPresenter
import com.kerencev.messenger.ui.login.signin.SignInPresenter
import com.kerencev.messenger.ui.login.signup.SignUpPresenter
import com.kerencev.messenger.ui.login.welcome.WelcomePresenter
import com.kerencev.messenger.ui.main.activity.MainActivity
import com.kerencev.messenger.ui.main.activity.MainPresenter
import com.kerencev.messenger.ui.main.chat.ChatPresenter
import com.kerencev.messenger.ui.main.chatlist.ChatListPresenter
import com.kerencev.messenger.ui.main.newmessage.NewMessagePresenter
import com.kerencev.messenger.ui.main.settings.SettingsPresenter
import com.kerencev.messenger.ui.main.settings.changename.ChangeNamePresenter
import com.kerencev.messenger.ui.main.settings.cropimage.CropImagePresenter
import com.kerencev.messenger.ui.main.wallpapers.WallpapersPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ApiModule::class,
        AuthRepositoryModule::class,
        CiceroneModule::class,
        LatestMessagesRepositoryModule::class,
        MediaStoreRepositoryModule::class,
        MessagesRepositoryModule::class,
        UsersRepositoryModule::class,
        WallpapersRepositoryModule::class,
        VibrationModule::class,
        PlayerModule::class
    ]
)
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(mainPresenter: MainPresenter)
    fun inject(loginActivityPresenter: LoginActivityPresenter)
    fun inject(loginPresenter: LoginPresenter)
    fun inject(signInPresenter: SignInPresenter)
    fun inject(signUpPresenter: SignUpPresenter)
    fun inject(welcomePresenter: WelcomePresenter)
    fun inject(chatPresenter: ChatPresenter)
    fun inject(chatListPresenter: ChatListPresenter)
    fun inject(newMessagePresenter: NewMessagePresenter)
    fun inject(changeNamePresenter: ChangeNamePresenter)
    fun inject(cropImagePresenter: CropImagePresenter)
    fun inject(settingsPresenter: SettingsPresenter)
    fun inject(wallpapersPresenter: WallpapersPresenter)
}
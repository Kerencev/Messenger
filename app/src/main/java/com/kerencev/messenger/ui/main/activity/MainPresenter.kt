package com.kerencev.messenger.ui.main.activity

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.AuthRepository
import com.kerencev.messenger.navigation.main.ChatListScreen
import com.kerencev.messenger.navigation.main.ChatScreen
import com.kerencev.messenger.navigation.main.SettingsScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import javax.inject.Inject

class MainPresenter : BasePresenter<MainView>() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var repoAuth: AuthRepository

    fun verifyUserIsLoggedIn() {
        repoAuth.verifyUserIsLoggedIn().flatMap { userId ->
            if (userId.isEmpty()) viewState.startLoginActivity()
            repoAuth.updateFirebaseToken(userId).flatMap { repoAuth.getUserById(userId) }
        }
            .subscribeByDefault()
            .subscribe { user ->
                viewState.setUserData(user)
            }.disposeBy(bag)
    }

    fun navigateToChatList(chatPartner: User?) {
        when (chatPartner) {
            null -> router.replaceScreen(ChatListScreen)
            else -> router.newRootChain(ChatListScreen, ChatScreen(chatPartner))
        }
    }

    fun navigateToSettingsFragment() {
        router.navigateTo(SettingsScreen)
    }

    fun onBackPressed() {
        router.exit()
    }
}
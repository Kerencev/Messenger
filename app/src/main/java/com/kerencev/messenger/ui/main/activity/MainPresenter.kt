package com.kerencev.messenger.ui.main.activity

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.AuthRepository
import com.kerencev.messenger.navigation.main.ChatListScreen
import com.kerencev.messenger.navigation.main.ChatScreen
import com.kerencev.messenger.navigation.main.SettingsScreen
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter
import javax.inject.Inject

class MainPresenter : MvpPresenter<MainView>() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var repoAuth: AuthRepository

    private val bag = CompositeDisposable()

    fun verifyUserIsLoggedIn() {
        repoAuth.verifyUserIsLoggedIn().flatMap { userId ->
            if (userId.isEmpty()) viewState.startLoginActivity()
            repoAuth.updateFirebaseToken(userId).flatMap { repoAuth.getUserById(userId) }
        }
            .subscribeByDefault()
            .subscribe(
                { user ->
                    viewState.setUserData(user)
                },
                {
                    Log.d(TAG, "Failed to verify user is logged in")
                }
            ).disposeBy(bag)
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

    override fun onDestroy() {
        bag.dispose()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainPresenter"
    }
}
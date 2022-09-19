package com.kerencev.messenger.ui.main.activity

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.model.repository.FirebaseAuthRepository
import com.kerencev.messenger.navigation.main.ChatListScreen
import com.kerencev.messenger.navigation.main.SettingsScreen
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter

class MainPresenter(
    private val router: Router,
    private val repoAuth: FirebaseAuthRepository
) : MvpPresenter<MainView>() {

    private val bag = CompositeDisposable()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        router.replaceScreen(ChatListScreen)
    }

    fun verifyUserIsLoggedIn() {
        repoAuth.verifyUserIsLoggedIn()
            .flatMap { userId ->
                if (userId.isEmpty()) {
                    viewState.startLoginActivity()
                }
                repoAuth.getUserById(userId)
            }
            .subscribeByDefault()
            .subscribe(
                { user ->
                    MessengerApp.instance.user = user
                    viewState.setUserData(user)
                    viewState.startStatusWorkManager()
                },
                {
                    Log.d(TAG, "Failed to verify user is logged in")
                }
            )
            .disposeBy(bag)
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
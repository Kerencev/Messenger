package com.kerencev.messenger.ui.main.settings

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen
import com.kerencev.messenger.model.repository.FirebaseAuthRepository
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault

private const val TAG = "SettingsPresenter"

class SettingsPresenter(
    private val repoAuth: FirebaseAuthRepository,
    private val router: Router
) : BasePresenter<SettingsView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getUserWithFirebase()
    }

    fun signOutWithFirebase() {
        repoAuth.signOut()
            .subscribeByDefault()
            .subscribe(
                {
                    viewState.startLoginActivity()
                },
                {
                    Log.d(TAG, "Failed to Signed out with FirebaseAuth")
                }
            ).disposeBy(bag)
    }

    fun navigateTo(screen: Screen) {
        router.navigateTo(screen)
    }

    private fun getUserWithFirebase() {
        repoAuth.verifyUserIsLoggedIn()
            .flatMap { useId ->
                repoAuth.getUserById(useId)
            }
            .subscribeByDefault()
            .subscribe(
                {
                    viewState.renderUserInfo(it)
                },
                {
                    Log.d(TAG, "${it.message}")
                }
            ).disposeBy(bag)
    }
}
package com.kerencev.messenger.presenters.main

import android.nfc.Tag
import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.FirebaseRepository
import com.kerencev.messenger.navigation.main.ChatListScreen
import com.kerencev.messenger.ui.main.MainView
import com.kerencev.messenger.utils.subscribeByDefault
import moxy.MvpPresenter

class MainPresenter(
    private val router: Router,
    private val repository: FirebaseRepository
) : MvpPresenter<MainView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        router.replaceScreen(ChatListScreen)
    }

    fun verifyUserIsLoggedIn() {
        repository.verifyUserIsLoggedIn()
            .subscribeByDefault()
            .subscribe(
                { isLoggedIn ->
                    if (!isLoggedIn) {
                        viewState.startLoginActivity()
                    }
                },
                {
                    Log.d(TAG, "Failed to verify user is logged in")
                }
            )
    }

    fun onBackPressed() {
        router.exit()
    }

    companion object {
        private const val TAG = "MainPresenter"
    }
}
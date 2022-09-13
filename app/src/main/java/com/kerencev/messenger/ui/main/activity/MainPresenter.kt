package com.kerencev.messenger.ui.main.activity

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.FirebaseRepository
import com.kerencev.messenger.navigation.main.ChatListScreen
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter

class MainPresenter(
    private val router: Router,
    private val repository: FirebaseRepository
) : MvpPresenter<MainView>() {

    private val bag = CompositeDisposable()

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
            ).disposeBy(bag)
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
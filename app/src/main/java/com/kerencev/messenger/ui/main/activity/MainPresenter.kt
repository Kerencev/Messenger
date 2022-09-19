package com.kerencev.messenger.ui.main.activity

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.google.firebase.database.FirebaseDatabase
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.FirebaseAuthRepository
import com.kerencev.messenger.navigation.main.ChatListScreen
import com.kerencev.messenger.navigation.main.SettingsScreen
import com.kerencev.messenger.services.TestService
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
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
                    repoAuth.saveUserStatus(user.uid, "Онлайн")
                        .subscribeByDefault()
                        .subscribe()
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
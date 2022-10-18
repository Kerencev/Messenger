package com.kerencev.messenger.ui.main.settings

import android.content.Context
import android.util.Log
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen
import com.kerencev.messenger.model.repository.FirebaseAuthRepository
import com.kerencev.messenger.model.repository.impl.MediaStoreRepository
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.log
import com.kerencev.messenger.utils.subscribeByDefault

private const val TAG = "SettingsPresenter"

class SettingsPresenter(
    private val repoMedia: MediaStoreRepository,
    private val repoAuth: FirebaseAuthRepository,
    private val router: Router
) : BasePresenter<SettingsView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getUserWithFirebase()
    }

    fun signOutWithFirebase() {
        repoAuth.clearUserToken()
            .subscribeByDefault()
            .subscribe(
                {
                    repoAuth.signOut()
                        .subscribeByDefault()
                        .subscribe(
                            {
                                viewState.startLoginActivity()
                            },
                            {
                                log(it.message.toString())
                            }
                        )
                },
                {
                    Log.d(TAG, "Failed to Signed out with FirebaseAuth")
                }
            ).disposeBy(bag)
    }

    fun navigateTo(screen: Screen) {
        router.navigateTo(screen)
    }

    fun getImagesFromExternalStorage(context: Context) {
        repoMedia.getImagesFromExternalStorage(context)
            .subscribeByDefault()
            .subscribe { listOfAllImages ->
                viewState.showChoosePhotoDialog(listOfAllImages)
            }.disposeBy(bag)
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
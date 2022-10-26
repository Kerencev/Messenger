package com.kerencev.messenger.ui.main.settings

import android.content.Context
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen
import com.kerencev.messenger.model.repository.AuthRepository
import com.kerencev.messenger.model.repository.MediaStoreRepository
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import javax.inject.Inject

class SettingsPresenter() : BasePresenter<SettingsView>() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var repoAuth: AuthRepository

    @Inject
    lateinit var repoMedia: MediaStoreRepository

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getUserWithFirebase()
    }

    fun signOutWithFirebase() {
        repoAuth.clearUserToken().concatWith(repoAuth.signOut())
            .subscribeByDefault()
            .subscribe {
                viewState.startLoginActivity()
            }.disposeBy(bag)
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

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }

    private fun getUserWithFirebase() {
        repoAuth.verifyUserIsLoggedIn()
            .flatMap { useId ->
                repoAuth.getUserById(useId)
            }
            .subscribeByDefault()
            .subscribe { user ->
                viewState.renderUserInfo(user)
            }.disposeBy(bag)
    }
}
package com.kerencev.messenger.presenters.login

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.navigation.login.LoginScreen
import com.kerencev.messenger.ui.login.LoginContainerView
import moxy.MvpPresenter

class LoginContainerPresenter(
    private val router: Router
) : MvpPresenter<LoginContainerView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        router.replaceScreen(LoginScreen)
    }

    fun onBackPressed() {
        router.exit()
    }
}
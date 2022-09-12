package com.kerencev.messenger.ui.login.loginactivity

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.navigation.login.LoginScreen
import moxy.MvpPresenter

class LoginContainerPresenter(
    private val router: Router
) : MvpPresenter<LoginContainerView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        //TODO: replace LoginScreen
        router.replaceScreen(LoginScreen)
    }

    fun onBackPressed() {
        router.exit()
    }
}
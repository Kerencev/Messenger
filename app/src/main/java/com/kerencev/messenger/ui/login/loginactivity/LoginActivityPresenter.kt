package com.kerencev.messenger.ui.login.loginactivity

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.navigation.login.LoginScreen
import moxy.MvpPresenter

class LoginActivityPresenter(
    private val router: Router
) : MvpPresenter<LoginActivityView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        router.replaceScreen(LoginScreen)
    }

    fun onBackPressed() {
        router.exit()
    }
}
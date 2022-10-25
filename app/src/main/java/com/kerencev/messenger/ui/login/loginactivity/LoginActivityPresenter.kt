package com.kerencev.messenger.ui.login.loginactivity

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.navigation.login.LoginScreen
import moxy.MvpPresenter
import javax.inject.Inject

class LoginActivityPresenter() : MvpPresenter<LoginActivityView>() {

    @Inject
    lateinit var router: Router

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        router.replaceScreen(LoginScreen)
    }

    fun onBackPressed() {
        router.exit()
    }
}
package com.kerencev.messenger.presenters.login

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.ui.login.signin.SignInView
import moxy.MvpPresenter

class SignInPresenter(
    private val router: Router
) : MvpPresenter<SignInView>() {

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }
}
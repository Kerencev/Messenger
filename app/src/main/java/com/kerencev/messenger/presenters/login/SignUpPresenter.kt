package com.kerencev.messenger.presenters.login

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.ui.login.signup.SignUpView
import moxy.MvpPresenter

class SignUpPresenter(
    private val router: Router
) : MvpPresenter<SignUpView>() {

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }
}
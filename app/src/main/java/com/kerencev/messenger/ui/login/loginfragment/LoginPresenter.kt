package com.kerencev.messenger.ui.login.loginfragment

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.navigation.login.SignInScreen
import com.kerencev.messenger.navigation.login.SignUpScreen
import moxy.MvpPresenter
import javax.inject.Inject

class LoginPresenter() : MvpPresenter<LoginView>() {

    @Inject
    lateinit var router: Router

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }

    fun navigateToSignInFragment() {
        router.navigateTo(SignInScreen)
    }

    fun navigateToSignUpFragment() {
        router.navigateTo(SignUpScreen)
    }
}
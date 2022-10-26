package com.kerencev.messenger.ui.login.signin

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.AuthRepository
import com.kerencev.messenger.navigation.login.SignUpScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import javax.inject.Inject

class SignInPresenter() : BasePresenter<SignInView>() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var repository: AuthRepository

    fun navigateToSignUpFragment() {
        router.navigateTo(SignUpScreen)
    }

    fun signInWithFirebase(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            viewState.showEmptyFieldsMessage()
            return
        }
        repository.signInWithEmailAndPassword(email, password)
            .subscribeByDefault()
            .subscribe(
                {
                    viewState.startMainActivity()
                },
                {
                    viewState.showErrorMessage()
                }
            ).disposeBy(bag)
    }

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }
}
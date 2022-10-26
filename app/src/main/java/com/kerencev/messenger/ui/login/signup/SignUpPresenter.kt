package com.kerencev.messenger.ui.login.signup

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.AuthRepository
import com.kerencev.messenger.navigation.login.WelcomeScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import javax.inject.Inject

class SignUpPresenter : BasePresenter<SignUpView>() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var repository: AuthRepository

    fun authWithFirebase(login: String, email: String, password: String, passwordAgain: String) {
        viewState.showProgressBar()
        if (login.isEmpty() || email.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
            viewState.hideProgressBar()
            viewState.showEmptyFieldsMessage()
            return
        }
        if (password != passwordAgain) {
            viewState.showNotCorrectPasswordMessage()
            viewState.hideProgressBar()
            return
        }

        repository.createUserWithEmailAndPassword(email, password)
            .andThen(repository.saveUserToFirebaseDatabase(login = login, email = email))
            .subscribeByDefault()
            .subscribe(
                {
                    router.replaceScreen(WelcomeScreen)
                },
                {
                    viewState.hideProgressBar()
                    viewState.showErrorMessage()
                }

            ).disposeBy(bag)
    }

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }
}

package com.kerencev.messenger.presenters.login

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.FirebaseRepository
import com.kerencev.messenger.ui.login.signup.SignUpView
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter

class SignUpPresenter(
    private val router: Router,
    private val repository: FirebaseRepository
) : MvpPresenter<SignUpView>() {

    private val bag = CompositeDisposable()

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }

    fun authWithFirebase(login: String, email: String, password: String, passwordAgain: String) {
        if (!checkFillAllFields(login, email, password, passwordAgain)) {
            viewState.showEmptyFieldsMessage()
            return
        }
        if (password != passwordAgain) {
            viewState.showNotCorrectPasswordMessage()
            return
        }
        repository.createUserWithEmailAndPassword(email, password)
            .subscribeByDefault()
            .subscribe(
                {
                    viewState.navigateToChatFragment()
                },
                {
                    viewState.showErrorMessage()
                }

            ).disposeBy(bag)
    }

    private fun checkFillAllFields(
        login: String,
        email: String,
        password: String,
        passwordAgain: String
    ): Boolean {
        if (login.isEmpty() || email.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
            return false
        }
        return true
    }

    override fun onDestroy() {
        bag.dispose()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SignUpPresenter"
    }
}

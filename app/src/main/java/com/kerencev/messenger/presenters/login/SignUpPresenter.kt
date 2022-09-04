package com.kerencev.messenger.presenters.login

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.FirebaseRepository
import com.kerencev.messenger.navigation.login.WelcomeScreen
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
            .subscribeByDefault()
            .subscribe(
                {
                    repository.saveUserToFirebaseDatabase(login = login, email = email)
                        .subscribeByDefault()
                        .subscribe(
                            {
                                viewState.startMainActivity()
                            },
                            {
                                viewState.hideProgressBar()
                                Log.d(TAG, "${it.message}")
                            }
                        )
                },
                {
                    viewState.hideProgressBar()
                    viewState.showErrorMessage()
                    Log.d(TAG, "Failed to create user with Firebase")
                }

            ).disposeBy(bag)
    }

    override fun onDestroy() {
        bag.dispose()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SignUpPresenter"
    }
}

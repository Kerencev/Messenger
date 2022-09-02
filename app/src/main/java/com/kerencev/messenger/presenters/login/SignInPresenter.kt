package com.kerencev.messenger.presenters.login

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.FirebaseRepository
import com.kerencev.messenger.navigation.login.SignUpScreen
import com.kerencev.messenger.ui.login.signin.SignInView
import com.kerencev.messenger.utils.subscribeByDefault
import moxy.MvpPresenter

class SignInPresenter(
    private val router: Router,
    private val repository: FirebaseRepository
) : MvpPresenter<SignInView>() {

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }

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
                    Log.d(TAG, "Вошли")
                },
                {
                    viewState.showErrorMessage()
                }
            )
    }

    companion object {
        private const val TAG = "SignInPresenter"
    }
}
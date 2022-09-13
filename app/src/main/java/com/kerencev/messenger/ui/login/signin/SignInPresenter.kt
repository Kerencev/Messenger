package com.kerencev.messenger.ui.login.signin

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.FirebaseAuthRepository
import com.kerencev.messenger.navigation.login.SignUpScreen
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter

class SignInPresenter(
    private val router: Router,
    private val repository: FirebaseAuthRepository
) : MvpPresenter<SignInView>() {

    private val bag = CompositeDisposable()

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
                    viewState.startMainActivity()
                },
                {
                    viewState.showErrorMessage()
                }
            ).disposeBy(bag)
    }

    override fun onDestroy() {
        bag.dispose()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SignInPresenter"
    }
}
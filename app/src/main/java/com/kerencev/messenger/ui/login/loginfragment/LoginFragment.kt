package com.kerencev.messenger.ui.login.loginfragment

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.databinding.FragmentLoginBinding
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.utils.app
import moxy.ktx.moxyPresenter

class LoginFragment :
    ViewBindingFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate),
    LoginView,
    OnBackPressedListener {

    private val presenter: LoginPresenter by moxyPresenter {
        LoginPresenter().apply { app.appComponent.inject(this) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            btnSignIn.setOnClickListener {
                presenter.navigateToSignInFragment()
            }
            btnSignUp.setOnClickListener {
                presenter.navigateToSignUpFragment()
            }
        }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    companion object {
        fun getInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}
package com.kerencev.messenger.ui.login.signup

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.databinding.FragmentSignUpBinding
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.presenters.login.SignUpPresenter
import com.kerencev.messenger.ui.base.ViewBindingFragment
import moxy.ktx.moxyPresenter

class SignUpFragment :
    ViewBindingFragment<FragmentSignUpBinding>(FragmentSignUpBinding::inflate),
    SignUpView,
    OnBackPressedListener {

    private val presenter by moxyPresenter {
        SignUpPresenter(MessengerApp.instance.router)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            signUpActionBack.setOnClickListener {
                presenter.onBackPressed()
            }
        }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    companion object {
        fun getInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }
}
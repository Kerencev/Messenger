package com.kerencev.messenger.ui.login.signin

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.databinding.FragmentSignInBinding
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.presenters.login.SignInPresenter
import com.kerencev.messenger.ui.base.ViewBindingFragment
import moxy.ktx.moxyPresenter

class SignInFragment :
    ViewBindingFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate),
    SignInView,
    OnBackPressedListener {

    private val presenter by moxyPresenter {
        SignInPresenter(MessengerApp.instance.router)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signInActionBack.setOnClickListener {
            presenter.onBackPressed()
        }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    companion object {
        fun getInstance(): SignInFragment {
            return SignInFragment()
        }
    }
}
package com.kerencev.messenger.ui.login.signin

import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentSignInBinding
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.login.loginactivity.LoginActivityView
import com.kerencev.messenger.utils.app
import com.kerencev.messenger.utils.hideKeyboard
import moxy.ktx.moxyPresenter

class SignInFragment :
    ViewBindingFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate),
    SignInView,
    OnBackPressedListener {

    private val presenter by moxyPresenter {
        SignInPresenter().apply { app.appComponent.inject(this) }
    }

    private var loginActivity: LoginActivityView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginActivity = (activity as? LoginActivityView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            signInToolbar.setNavigationOnClickListener {
                presenter.onBackPressed()
            }
            signInBtnEnter.setOnClickListener {
                requireActivity().hideKeyboard(signInEditEmail)
                presenter.signInWithFirebase(
                    signInEditEmail.text.toString(),
                    signInEditPassword.text.toString()
                )
            }
            signInTvRegister.setOnClickListener {
                presenter.navigateToSignUpFragment()
            }
        }
    }

    override fun showEmptyFieldsMessage() {
        Snackbar.make(
            requireContext(),
            binding.root,
            getString(R.string.not_all_fields_are_filled_in),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showErrorMessage() {
        Snackbar.make(
            requireContext(),
            binding.root,
            getString(R.string.faild_to_enter_check_validity_of_the_data),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun startMainActivity() {
        loginActivity?.startMainActivity()
    }

    override fun onBackPressed() = presenter.onBackPressed()

    companion object {
        fun getInstance(): SignInFragment {
            return SignInFragment()
        }
    }
}
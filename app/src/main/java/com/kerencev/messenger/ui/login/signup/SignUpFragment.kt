package com.kerencev.messenger.ui.login.signup

import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentSignUpBinding
import com.kerencev.messenger.model.FirebaseRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.presenters.login.SignUpPresenter
import com.kerencev.messenger.ui.base.ViewBindingFragment
import moxy.ktx.moxyPresenter

class SignUpFragment :
    ViewBindingFragment<FragmentSignUpBinding>(FragmentSignUpBinding::inflate),
    SignUpView,
    OnBackPressedListener {

    private val presenter by moxyPresenter {
        SignUpPresenter(
            MessengerApp.instance.router,
            FirebaseRepositoryImpl()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            signUpActionBack.setOnClickListener {
                presenter.onBackPressed()
            }
            signUpBtn.setOnClickListener {
                presenter.authWithFirebase(
                    login = signUpEditLogin.text.toString(),
                    email = signUpEditEmail.text.toString(),
                    password = signUpEditPassword.text.toString(),
                    passwordAgain = signUpEditPasswordAgain.text.toString()
                )
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

    override fun showNotCorrectPasswordMessage() {
        Snackbar.make(
            requireContext(),
            binding.root,
            getString(R.string.password_must_match_in_both_fields),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun navigateToChatFragment() {
        Snackbar.make(
            requireContext(),
            binding.root,
            "Перешли во фрагмент чата",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showErrorMessage() {
        Snackbar.make(
            requireContext(),
            binding.root,
            getString(R.string.failed_to_register_check_the_validity_of_the_email),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onBackPressed() = presenter.onBackPressed()

    companion object {
        private const val TAG = "SignUpFragment"
        fun getInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }
}
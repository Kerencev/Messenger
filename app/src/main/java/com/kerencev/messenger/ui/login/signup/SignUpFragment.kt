package com.kerencev.messenger.ui.login.signup

import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentSignUpBinding
import com.kerencev.messenger.model.repository.impl.FirebaseAuthRepositoryImpl
import com.kerencev.messenger.navigation.FinishActivity
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.login.loginactivity.LoginActivityView
import moxy.ktx.moxyPresenter

class SignUpFragment :
    ViewBindingFragment<FragmentSignUpBinding>(FragmentSignUpBinding::inflate),
    SignUpView,
    OnBackPressedListener {

    private val presenter by moxyPresenter {
        SignUpPresenter(
            MessengerApp.instance.router,
            FirebaseAuthRepositoryImpl()
        )
    }

    private var loginActivity: LoginActivityView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginActivity = (activity as? LoginActivityView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            signUpToolbar.setNavigationOnClickListener {
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

    override fun showErrorMessage() {
        Snackbar.make(
            requireContext(),
            binding.root,
            getString(R.string.failed_to_register_check_the_validity_of_the_email),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showProgressBar() {
        with(binding) {
            signUpBtn.visibility = View.GONE
            signUpProgress.visibility = View.VISIBLE
        }
    }

    override fun hideProgressBar() {
        with(binding) {
            signUpBtn.visibility = View.VISIBLE
            signUpProgress.visibility = View.GONE
        }
    }

    override fun startMainActivity() {
        loginActivity?.startMainActivity()
    }

    override fun onBackPressed() = presenter.onBackPressed()

    companion object {
        private const val TAG = "SignUpFragment"
        fun getInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }
}
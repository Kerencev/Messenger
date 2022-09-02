package com.kerencev.messenger.ui.login.signin

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentSignInBinding
import com.kerencev.messenger.model.FirebaseRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.presenters.login.SignInPresenter
import com.kerencev.messenger.ui.base.ViewBindingFragment
import moxy.ktx.moxyPresenter
import java.util.*

class SignInFragment :
    ViewBindingFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate),
    SignInView,
    OnBackPressedListener {

    private val presenter by moxyPresenter {
        SignInPresenter(
            MessengerApp.instance.router,
            FirebaseRepositoryImpl()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            signInActionBack.setOnClickListener {
                presenter.onBackPressed()
            }
            signInBtnEnter.setOnClickListener {
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

    override fun onBackPressed() = presenter.onBackPressed()

    companion object {
        private const val TAG = "SignInFragment"
        fun getInstance(): SignInFragment {
            return SignInFragment()
        }
    }
}
package com.kerencev.messenger.ui.login.signup

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentSignUpBinding
import com.kerencev.messenger.model.FirebaseRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.presenters.login.SignUpPresenter
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.login.signin.SignInFragment
import moxy.ktx.moxyPresenter
import java.util.*

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

    private var selectedPhotoUri: Uri? = null

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
                //Test
                uploadImageFileToFirebaseStorage()
            }
            ivSelectPhoto.setOnClickListener {
                //Test
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(TAG, "Photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                selectedPhotoUri
            )
            val drawable = BitmapDrawable(bitmap)
            binding.ivSelectPhoto.setBackgroundDrawable(drawable)
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

    override fun onBackPressed() = presenter.onBackPressed()

    private fun uploadImageFileToFirebaseStorage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        selectedPhotoUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener { task ->
                    Log.d(TAG, "Successfully uploaded image: ${task.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(TAG, "File location: $it")

                        saveUserToFirebaseDataBAse(it.toString())
                    }
                }
        }
    }

    private fun saveUserToFirebaseDataBAse(avatarUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, binding.signUpEditLogin.text.toString(), avatarUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Save the User to Firebase Database")
            }
    }

    companion object {
        private const val TAG = "SignUpFragment"
        fun getInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }
}

data class User(
    val uid: String,
    val userName: String,
    val avatarUrl: String
)
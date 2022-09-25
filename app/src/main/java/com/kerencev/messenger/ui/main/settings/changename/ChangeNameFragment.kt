package com.kerencev.messenger.ui.main.settings.changename

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.jakewharton.rxbinding.widget.RxTextView
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentChangeNameBinding
import com.kerencev.messenger.model.repository.impl.FirebaseAuthRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.utils.showKeyBoard
import moxy.ktx.moxyPresenter
import java.util.concurrent.TimeUnit

private const val TAG = "ChangeNameFragment"

class ChangeNameFragment :
    ViewBindingFragment<FragmentChangeNameBinding>(FragmentChangeNameBinding::inflate),
    OnBackPressedListener,
    ChangeNameView {

    private val presenter by moxyPresenter {
        ChangeNamePresenter(
            MessengerApp.instance.router,
            FirebaseAuthRepositoryImpl()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun renderUserLogin(login: String) {
        with(binding) {
            changeNameEditText.setText(login)
            requireActivity().showKeyBoard(binding.changeNameEditText)
            changeNameEditText.setSelection(changeNameEditText.length())
            listenTextChange(login)
        }
    }

    private fun listenTextChange(login: String) {
        RxTextView.textChanges(binding.changeNameEditText)
            .debounce(400, TimeUnit.MILLISECONDS)
            .subscribe {
                if (it.toString() != login) {
                    Log.d(TAG, "$it")
                }
            }
        binding.changeNameEditText.addTextChangedListener { text ->
            text?.let {
                if (text.length < 5) {
                    showShortLoginError()
                } else {
                    showCheckLoginInfo()
                }
            }
        }
    }

    private fun showShortLoginError() = with(binding) {
        changeNameHint.visibility = View.VISIBLE
        changeNameHint.text = resources.getString(R.string.short_name_error)
        changeNameHint.setTextColor(resources.getColor(androidx.appcompat.R.color.error_color_material_dark))
    }

    private fun showCheckLoginInfo() = with(binding) {
        changeNameHint.visibility = View.VISIBLE
        changeNameHint.text = resources.getString(R.string.check_login)
        changeNameHint.setTextColor(resources.getColor(R.color.change_name_info_text))
    }
}
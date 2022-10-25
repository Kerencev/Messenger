package com.kerencev.messenger.ui.main.settings.changename

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.jakewharton.rxbinding.widget.RxTextView
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentChangeNameBinding
import com.kerencev.messenger.model.repository.impl.AuthRepositoryImpl
import com.kerencev.messenger.model.repository.impl.UsersRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.main.settings.SettingsFragment
import com.kerencev.messenger.utils.hideKeyboard
import com.kerencev.messenger.utils.showKeyBoard
import moxy.ktx.moxyPresenter
import java.util.concurrent.TimeUnit

const val MIN_LOGIN_LETTERS = 5
const val LOGIN_BUNDLE_KEY = "LOGIN_BUNDLE_KEY"

class ChangeNameFragment :
    ViewBindingFragment<FragmentChangeNameBinding>(FragmentChangeNameBinding::inflate),
    OnBackPressedListener,
    ChangeNameView {

    private val presenter by moxyPresenter {
        ChangeNamePresenter(
            MessengerApp.instance.router,
            AuthRepositoryImpl(),
            UsersRepositoryImpl()
        )
    }
    private lateinit var handler: Handler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        setToolbarClicks()
    }

    override fun onStop() {
        requireActivity().hideKeyboard(binding.changeNameEditText)
        super.onStop()
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun renderUserLogin(login: String) {
        with(binding) {
            changeNameEditText.setText(login)
            requireActivity().showKeyBoard(binding.changeNameEditText)
            changeNameEditText.setSelection(changeNameEditText.length())
            listenTextChange()
        }
    }

    override fun showValidityLoginInfo(isValid: Boolean) = with(binding) {
        changeNameHint.visibility = View.VISIBLE
        when (isValid) {
            true -> {
                changeNameHint.text = resources.getString(R.string.valid_login)
                changeNameHint.setTextColor(resources.getColor(R.color.user_status_online))
            }
            false -> {
                changeNameHint.text = resources.getString(R.string.not_valid_login)
                changeNameHint.setTextColor(resources.getColor(androidx.appcompat.R.color.error_color_material_dark))
            }
        }
    }

    override fun highlightError() {
        with(binding) {
            changeNameHint.setBackgroundColor(resources.getColor(R.color.highlight_error))
            handler.postDelayed(
                {
                    changeNameHint.setBackgroundColor(resources.getColor(R.color.white))
                }, 200
            )
        }
    }

    override fun showSuccessToast() {
        Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
    }

    override fun setResultForSettingsFragment(newLogin: String) {
        parentFragmentManager.setFragmentResult(SettingsFragment.FRAGMENT_RESULT_KEY, Bundle().apply {
            putString(LOGIN_BUNDLE_KEY, newLogin)
        })
    }

    private fun setToolbarClicks() = with(binding) {
        changeNameToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.changeNameDone -> presenter.handleActionDoneEvent(changeNameEditText.text.toString())
            }
            true
        }
        changeNameToolbar.setNavigationOnClickListener {
            presenter.onBackPressed()
        }
    }

    private fun listenTextChange() {
        RxTextView.textChanges(binding.changeNameEditText)
            .debounce(800, TimeUnit.MILLISECONDS)
            .subscribe {
                presenter.checkValidityLogin(it.toString())
            }
        binding.changeNameEditText.addTextChangedListener { text ->
            text?.let {
                if (text.length < MIN_LOGIN_LETTERS) {
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
package com.kerencev.messenger.ui.main.settings

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentSettingsBinding
import com.kerencev.messenger.model.repository.impl.FirebaseAuthRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.main.activity.MainView
import moxy.ktx.moxyPresenter

private const val TAG = "SettingsFragment"

class SettingsFragment :
    ViewBindingFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate),
    OnBackPressedListener, SettingsView {

    private var mainActivity: MainView? = null
    private val presenter by moxyPresenter {
        SettingsPresenter(
            FirebaseAuthRepositoryImpl(),
            MessengerApp.instance.router
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = (activity as? MainView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarClicks()
    }

    override fun startLoginActivity() {
        mainActivity?.startLoginActivity()
    }

    override fun onBackPressed() = presenter.onBackPressed()

    private fun setToolbarClicks() {
        binding.settingsToolbar.setNavigationOnClickListener {
            presenter.onBackPressed()
        }
        binding.settingsToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.actionSignOut -> presenter.signOutWithFirebase()
            }
            true
        }
    }
}
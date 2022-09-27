package com.kerencev.messenger.ui.main.settings


import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R

import com.kerencev.messenger.databinding.FragmentSettingsBinding
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.FirebaseAuthRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.navigation.main.ChangeNameScreen
import com.kerencev.messenger.navigation.main.WallpapersScreen
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.main.activity.MainView
import com.kerencev.messenger.ui.main.settings.changename.LOGIN_BUNDLE_KEY
import com.kerencev.messenger.ui.main.settings.changename.LOGIN_RESULT_KEY
import com.kerencev.messenger.utils.showComingSoonSnack
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
        setAnotherClicks()
        listenNewLoginFromChangeNameFragment()
    }

    override fun startLoginActivity() {
        mainActivity?.startLoginActivity()
    }

    override fun renderUserInfo(user: User) {
        with(binding) {
            Glide.with(requireContext()).load(user.avatarUrl)
                .placeholder(R.drawable.ic_user_place_holder).into(ivSettingsAvatar)
            tvSettingsLogin.text = user.login
            tvSettingsEmail.text = user.email
            tvSettingsProfileLogin.text = user.login
            tvSettingsProfileEmail.text = user.email
        }
    }

    override fun listenNewLoginFromChangeNameFragment() {
        parentFragmentManager.setFragmentResultListener(
            LOGIN_RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val newLogin = bundle.getString(LOGIN_BUNDLE_KEY)
            newLogin?.let {
                with(binding) {
                    tvSettingsLogin.text = newLogin
                    tvSettingsProfileLogin.text = newLogin
                }
                mainActivity?.updateUserLogin(newLogin)
            }
        }
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

    private fun setAnotherClicks() = with(binding) {
        cardSettingsProfileBackground.setOnClickListener {
            presenter.navigateTo(WallpapersScreen)
        }
        cardSettingsProfileLogin.setOnClickListener {
            presenter.navigateTo(ChangeNameScreen)
        }
        cardSettingsProfileLanguage.setOnClickListener {
            root.showComingSoonSnack()
        }
        cardSettingsProfileAnimation.setOnClickListener {
            root.showComingSoonSnack()
        }
        cardSettingsProfileNotification.setOnClickListener {
            root.showComingSoonSnack()
        }
        cardSettingsProfileStickers.setOnClickListener {
            root.showComingSoonSnack()
        }
    }
}
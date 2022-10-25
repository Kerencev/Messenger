package com.kerencev.messenger.ui.login.signup

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.databinding.FragmentWelcomeBinding
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.navigation.login.WalkthroughsScreen
import com.kerencev.messenger.ui.base.ViewBindingFragment

class WelcomeFragment :
    ViewBindingFragment<FragmentWelcomeBinding>(FragmentWelcomeBinding::inflate),
    OnBackPressedListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnWelcome.setOnClickListener {
            MessengerApp.instance.router.navigateTo(WalkthroughsScreen)
        }
    }

    override fun onBackPressed(): Boolean {
        MessengerApp.instance.router.exit()
        return true
    }
}
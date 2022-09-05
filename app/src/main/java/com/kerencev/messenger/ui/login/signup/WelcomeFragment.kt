package com.kerencev.messenger.ui.login.signup

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.databinding.FragmentWelcomeBinding
import com.kerencev.messenger.navigation.FinishActivity
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment

class WelcomeFragment :
    ViewBindingFragment<FragmentWelcomeBinding>(FragmentWelcomeBinding::inflate),
    OnBackPressedListener {

    private var loginActivity: FinishActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginActivity = (activity as? FinishActivity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnWelcome.setOnClickListener {
            loginActivity?.startMainActivity()
        }
    }

    override fun onBackPressed(): Boolean {
        MessengerApp.instance.router.exit()
        return true
    }
}
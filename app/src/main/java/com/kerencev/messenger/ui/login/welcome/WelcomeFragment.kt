package com.kerencev.messenger.ui.login.welcome

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.databinding.FragmentWelcomeBinding
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.utils.app
import moxy.ktx.moxyPresenter

class WelcomeFragment :
    ViewBindingFragment<FragmentWelcomeBinding>(FragmentWelcomeBinding::inflate),
    OnBackPressedListener,
    WelcomeView {

    private val presenter by moxyPresenter {
        WelcomePresenter().apply { app.appComponent.inject(this) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnWelcome.setOnClickListener {
            navigateToWalkthroughsFragment()
        }
    }

    override fun navigateToWalkthroughsFragment() {
        presenter.navigateToWalkThroughsFragment()
    }

    override fun onBackPressed() = presenter.onBackPressed()
}
package com.kerencev.messenger.ui.login.walkthroughs

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.databinding.FragmentViewPagerWalkthroughsBinding
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.login.loginactivity.LoginActivityView

class ViewPagerWalkthroughsFragment :
    ViewBindingFragment<FragmentViewPagerWalkthroughsBinding>(FragmentViewPagerWalkthroughsBinding::inflate) {

    private var loginActivity: LoginActivityView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginActivity = (activity as? LoginActivityView)
        loginActivity?.hideStatusBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPagerWalkthroughs.adapter = ViewPagerWalkthroughsAdapter(
            fragment = this@ViewPagerWalkthroughsFragment,
            data = getWalkthrougsFragmentsList()
        )
    }

    private fun getWalkthrougsFragmentsList(): List<WalkthroughsFragment> {
        return listOf(
            WalkthroughsFragment1(),
            WalkthroughsFragment2(),
            WalkthroughsFragment3()
        )
    }

    override fun onDestroyView() {
        _binding = null
        loginActivity?.showStatusBar()
        super.onDestroyView()
    }
}
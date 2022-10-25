package com.kerencev.messenger.ui.login.walkthroughs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerWalkthroughsAdapter(fragment: Fragment, private val data: List<WalkthroughsFragment>) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return data.size
    }

    override fun createFragment(position: Int): Fragment {
        return data[position]
    }

}
package com.kerencev.messenger.ui.main.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.NestedScrollView
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentSettingsBinding
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import moxy.ktx.moxyPresenter
import kotlin.math.abs

private const val TAG = "SettingsFragment"

class SettingsFragment :
    ViewBindingFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate),
    OnBackPressedListener, SettingsView {

    private val presenter by moxyPresenter {
        SettingsPresenter(MessengerApp.instance.router)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
//            if (Math.abs(verticalOffset) == appBarLayout.totalScrollRange) {
//                binding.toolbar.setTitle(R.string.settings)
//            } else {
//                binding.toolbar.setTitle(R.string.search)
//            }
//        }

//        binding.settingsScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
//            if (scrollY > oldScrollY) {
//                Log.d(TAG, "Scroll DOWN")
//            }
//            if (scrollY < 100) {
//                Log.d(TAG, "Scroll UP")
//            }
//            if (scrollY == 0) {
//                Log.d(TAG, "TOP SCROLL")
//            }
//            if (scrollY == v.measuredHeight - v.getChildAt(0).measuredHeight) {
//                Log.d(TAG, "BOTTOM SCROLL")
//            }
//        })
    }

    override fun onBackPressed() = presenter.onBackPressed()
}
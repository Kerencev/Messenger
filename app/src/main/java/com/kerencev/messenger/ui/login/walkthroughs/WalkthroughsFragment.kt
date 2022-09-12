package com.kerencev.messenger.ui.login.walkthroughs

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.kerencev.messenger.navigation.FinishActivity

open class WalkthroughsFragment : Fragment(){

    private var finishActivity: FinishActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finishActivity = (activity as? FinishActivity)
    }

    fun onActionSkipClick() {
        finishActivity?.startMainActivity()
    }
}
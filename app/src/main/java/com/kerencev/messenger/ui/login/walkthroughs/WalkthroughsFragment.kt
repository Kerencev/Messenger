package com.kerencev.messenger.ui.login.walkthroughs

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.kerencev.messenger.ui.login.loginactivity.LoginActivityView

open class WalkthroughsFragment : Fragment(){

    private var finishActivity: LoginActivityView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finishActivity = (activity as? LoginActivityView)
    }

    fun onActionSkipClick() {
        finishActivity?.startMainActivity()
    }
}
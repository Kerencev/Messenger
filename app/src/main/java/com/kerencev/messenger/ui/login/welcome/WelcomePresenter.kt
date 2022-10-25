package com.kerencev.messenger.ui.login.welcome

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.navigation.login.WalkthroughsScreen
import moxy.MvpPresenter
import javax.inject.Inject

class WelcomePresenter : MvpPresenter<WelcomeView>() {

    @Inject
    lateinit var router: Router

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }

    fun navigateToWalkThroughsFragment() {
        router.navigateTo(WalkthroughsScreen)
    }
}
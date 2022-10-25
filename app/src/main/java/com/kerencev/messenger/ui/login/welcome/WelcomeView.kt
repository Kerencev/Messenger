package com.kerencev.messenger.ui.login.welcome

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface WelcomeView : MvpView {
    fun navigateToWalkthroughsFragment()
}
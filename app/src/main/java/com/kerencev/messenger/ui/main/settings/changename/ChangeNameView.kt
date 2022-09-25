package com.kerencev.messenger.ui.main.settings.changename

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ChangeNameView : MvpView {
    fun renderUserLogin(login: String)
}
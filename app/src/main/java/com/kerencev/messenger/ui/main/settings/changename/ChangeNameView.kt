package com.kerencev.messenger.ui.main.settings.changename

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ChangeNameView : MvpView {
    fun renderUserLogin(login: String)
    fun showValidityLoginInfo(isValid: Boolean)
    fun highlightError()
    fun showSuccessToast()
    fun setResultForSettingsFragment(newLogin: String)
}
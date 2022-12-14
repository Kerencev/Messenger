package com.kerencev.messenger.ui.login.signin

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface SignInView : MvpView {
    fun showEmptyFieldsMessage()
    fun showErrorMessage()
    fun startMainActivity()
}
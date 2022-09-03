package com.kerencev.messenger.ui.login.signup

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface SignUpView : MvpView {
    fun showEmptyFieldsMessage()
    fun showNotCorrectPasswordMessage()
    fun showErrorMessage()
    fun showProgressBar()
    fun hideProgressBar()
}
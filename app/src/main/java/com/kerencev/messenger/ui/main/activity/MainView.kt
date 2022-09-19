package com.kerencev.messenger.ui.main.activity

import androidx.appcompat.widget.Toolbar
import com.kerencev.messenger.model.entities.User
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface MainView : MvpView {
    fun startLoginActivity()
    fun setUserData(user: User)
    fun setToolbar(toolbar: Toolbar)
    fun hideStatusBar()
    fun showStatusBar()
}
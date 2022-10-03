package com.kerencev.messenger.ui.main.settings

import com.kerencev.messenger.model.entities.User
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface SettingsView : MvpView {
    fun startLoginActivity()
    fun renderUserInfo(user: User)
    fun registerFragmentResultListener()
    @StateStrategyType(SkipStrategy::class)
    fun showChoosePhotoDialog(listOfAllImages: List<String>)
}
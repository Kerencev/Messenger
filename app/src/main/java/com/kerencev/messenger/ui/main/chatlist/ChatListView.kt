package com.kerencev.messenger.ui.main.chatlist

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ChatListView : MvpView {
    fun startLoginActivity()
}
package com.kerencev.messenger.ui.main.newmessage

import com.kerencev.messenger.model.entities.User
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface NewMessageView : MvpView {
    fun initList(listOfUsers: List<User>)
}
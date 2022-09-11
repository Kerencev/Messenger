package com.kerencev.messenger.ui.main.chatlist

import com.kerencev.messenger.model.entities.LatestMessage
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import java.util.HashMap

@StateStrategyType(AddToEndSingleStrategy::class)
interface ChatListView : MvpView {
    fun startLoginActivity()
    fun refreshRecyclerView(data: List<LatestMessage>)
}
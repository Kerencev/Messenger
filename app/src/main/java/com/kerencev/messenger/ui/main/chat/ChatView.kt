package com.kerencev.messenger.ui.main.chat

import com.kerencev.messenger.model.entities.ChatMessage
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ChatView : MvpView {
    fun setCurrentUserId(userId: String)
    fun loadUserAvatar()
    fun addMessage(chatMessage: ChatMessage)
    fun setAdapterData(data: List<ChatMessage>)
}
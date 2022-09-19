package com.kerencev.messenger.ui.main.chat

import com.kerencev.messenger.model.entities.ChatMessage
import moxy.MvpView
import moxy.viewstate.strategy.*

@StateStrategyType(AddToEndSingleStrategy::class)
interface ChatView : MvpView {
    fun loadUserAvatar()
    fun addMessage(chatMessage: ChatMessage)
    fun setAdapterData(data: List<ChatMessage>)
    fun setCurrentWallpaper(wallpaper: String)
}
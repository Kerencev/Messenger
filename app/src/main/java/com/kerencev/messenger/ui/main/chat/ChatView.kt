package com.kerencev.messenger.ui.main.chat

import com.kerencev.messenger.model.entities.ChatMessage
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ChatView : MvpView {
    fun addMessage(chatMessage: ChatMessage)
    fun setAdapterData(data: List<ChatMessage>)
    fun setCurrentWallpaper(wallpaper: String)
    fun updateChatPartnerStatus(status: String)
    fun updateChatPartnerLogin(login: String)
    fun updateChatPartnerAvatar(avatarUrl: String?)
    fun setChatPartnerIsTyping(isTyping: Boolean)
    @StateStrategyType(SkipStrategy::class)
    fun showVoiceRecordInfo()
}
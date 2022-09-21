package com.kerencev.messenger.utils

import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import java.util.*

object ChatMessageMapper {

    private const val TAG = "ChatMessageMapper"

    fun mapToChatMessage(message: String, user: User, chatPartner: User): ChatMessage {
        val time = System.currentTimeMillis()
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            message = message,
            fromId = user.uid,
            toId = chatPartner.uid,
            timesTamp = time,
            countOfUnread = 0,
            chatPartnerId = chatPartner.uid,
            chatPartnerLogin = chatPartner.login,
            chatPartnerEmail = chatPartner.email,
            chatPartnerWasOnline = chatPartner.wasOnline,
            chatPartnerIsOnline = true,
            chatPartnerAvatarUrl = chatPartner.avatarUrl
        )
    }

    fun mapToLatestMessageForChatPartner(countOfUnread: Long, chatMessage: ChatMessage, chatPartner: User): ChatMessage {
        return ChatMessage(
            id = chatMessage.id,
            message = chatMessage.message,
            fromId = chatMessage.fromId,
            toId = chatMessage.toId,
            timesTamp = chatMessage.timesTamp,
            countOfUnread = countOfUnread,
            chatPartnerId = chatPartner.uid,
            chatPartnerLogin = chatPartner.login,
            chatPartnerEmail = chatPartner.email,
            chatPartnerWasOnline = chatPartner.wasOnline,
            chatPartnerIsOnline = true,
            chatPartnerAvatarUrl = chatPartner.avatarUrl
        )
    }
}
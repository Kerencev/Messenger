package com.kerencev.messenger.utils

import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import java.util.*

object ChatMessageMapper {

    fun mapToChatMessageForUserNode(message: String, user: User, chatPartner: User): ChatMessage {
        val time = System.currentTimeMillis()
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            message = message,
            fromId = user.uid,
            toId = chatPartner.uid,
            timesTamp = time,
            countOfUnread = 0,
            chatPartnerId = chatPartner.uid,
            chatPartnerNotificationId = chatPartner.notificationId,
            chatPartnerLogin = chatPartner.login,
            chatPartnerEmail = chatPartner.email,
            chatPartnerWasOnline = chatPartner.wasOnline,
            chatPartnerIsOnline = true,
            chatPartnerIsTyping = false,
            chatPartnerAvatarUrl = chatPartner.avatarUrl
        )
    }

    fun mapToChatMessageForPartnerNode(message: String, user: User, chatPartner: User): ChatMessage {
        val time = System.currentTimeMillis()
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            message = message,
            fromId = user.uid,
            toId = chatPartner.uid,
            timesTamp = time,
            countOfUnread = 0,
            chatPartnerId = user.uid,
            chatPartnerNotificationId = user.notificationId,
            chatPartnerLogin = user.login,
            chatPartnerEmail = user.email,
            chatPartnerWasOnline = user.wasOnline,
            chatPartnerIsOnline = true,
            chatPartnerIsTyping = false,
            chatPartnerAvatarUrl = user.avatarUrl
        )
    }

    fun mapToLatestMessageForChatPartner(currentUser: User, chatPartner: User, message: String, countOfUnread: Long): ChatMessage {
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            message = message,
            fromId = currentUser.uid,
            toId = chatPartner.uid,
            timesTamp = System.currentTimeMillis(),
            countOfUnread = countOfUnread,
            chatPartnerId = currentUser.uid,
            chatPartnerNotificationId = currentUser.notificationId,
            chatPartnerLogin = currentUser.login,
            chatPartnerEmail = currentUser.email,
            chatPartnerWasOnline = currentUser.wasOnline,
            chatPartnerIsOnline = true,
            chatPartnerIsTyping = false,
            chatPartnerAvatarUrl = currentUser.avatarUrl
        )
    }
}
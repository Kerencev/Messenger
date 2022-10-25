package com.kerencev.messenger.utils

import com.kerencev.messenger.data.remote.dto.NotificationData
import com.kerencev.messenger.data.remote.dto.PushNotification
import com.kerencev.messenger.model.entities.User

object PushMapper {
    fun mapToPushNotification(
        listOfMessages: List<String>,
        currentUser: User,
        partnerToken: String
    ): PushNotification {
        var message = ""
        for (i in listOfMessages.indices) {
            message += if (i == listOfMessages.size - 1) {
                listOfMessages[i]
            } else {
                "${listOfMessages[i]}\n"
            }
        }
        val notificationData = NotificationData(
            message = message,
            chatPartnerId = currentUser.uid,
            notificationId = currentUser.notificationId.toString(),
            chatPartnerLogin = currentUser.login,
            chatPartnerEmail = currentUser.email,
            chatPartnerAvatarUrl = currentUser.avatarUrl
        )
        return PushNotification(
            to = partnerToken,
            data = notificationData
        )
    }
}
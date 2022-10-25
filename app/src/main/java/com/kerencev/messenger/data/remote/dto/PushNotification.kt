package com.kerencev.messenger.data.remote.dto

data class PushNotification(
    val data: NotificationData,
    val to: String
)
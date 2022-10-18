package com.kerencev.messenger.data.remote.dto

data class NotificationData(
    val message: String,
    val chatPartnerId: String,
    val notificationId: String,
    val chatPartnerLogin: String,
    val chatPartnerEmail: String,
    val chatPartnerAvatarUrl: String?
)
package com.kerencev.messenger.model.entities

data class ChatMessage(
    val id: String,
    val message: String,
    val fromId: String,
    val toId: String,
    val timesTamp: Long,
    val countOfUnread: Long,
    val chatPartnerId: String,
    val chatPartnerLogin: String,
    val chatPartnerEmail: String,
    val ChatPartnerAvatarUrl: String?
) {
    constructor() : this("", "", "", "", -1, 0, "", "", "", null)
}

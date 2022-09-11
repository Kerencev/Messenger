package com.kerencev.messenger.model.entities

data class LatestMessage(
    val id: String,
    val message: String,
    val fromId: String,
    val toId: String,
    val timesTamp: Long,
    var countOfUnread: Long,
    var chatPartnerId: String,
    var chatPartnerLogin: String,
    var chatPartnerEmail: String,
    var ChatPartnerAvatarUrl: String?
) {
    constructor() : this("", "", "", "", -1, 0, "", "", "", null)
}

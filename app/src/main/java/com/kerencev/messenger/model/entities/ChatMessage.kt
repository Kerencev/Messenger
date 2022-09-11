package com.kerencev.messenger.model.entities

data class ChatMessage(
    val id: String,
    val message: String,
    val fromId: String,
    val toId: String,
    val timesTamp: Long
) {

    constructor(): this("", "", "", "", -1)
}

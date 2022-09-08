package com.kerencev.messenger.model.entities

data class ChatMessage(
    val id: String,
    val message: String,
    val fromId: String,
    val toId: String,
    val time: String
) {

    constructor(): this("", "", "", "", "")
}

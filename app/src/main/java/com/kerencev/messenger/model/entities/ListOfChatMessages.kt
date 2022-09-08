package com.kerencev.messenger.model.entities

data class ListOfChatMessages(
    val data: List<ChatMessage>
) {
    constructor() : this(emptyList())
}
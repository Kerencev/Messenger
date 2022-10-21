package com.kerencev.messenger.model.repository

import com.kerencev.messenger.model.entities.ChatMessage
import io.reactivex.rxjava3.core.Observable

interface LatestMessagesRepository {
    fun listenForLatestMessages(): Observable<ChatMessage>
    fun updateAllLatestMessages(): Observable<List<ChatMessage>>
}
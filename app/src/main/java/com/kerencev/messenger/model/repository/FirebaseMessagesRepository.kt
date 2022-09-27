package com.kerencev.messenger.model.repository

import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.utils.StatusOfSendingMessage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface FirebaseMessagesRepository {
    fun listenForNewMessages(fromId: String, toId: String): Observable<ChatMessage>
    fun getAllMessages(fromId: String, toId: String): Single<Pair<List<ChatMessage>, Int>>
    fun listenForLatestMessages(): Observable<ChatMessage>
    fun resetUnreadMessages(toId: String, fromId: String): Completable
    fun getCountOfUnreadMessages(toId: String, fromId: String): Single<Long>
    fun saveMessageForAllNodes(
        message: String,
        user: User,
        chatPartner: User
    ): Observable<StatusOfSendingMessage>

    fun updateAllLatestMessages(): Observable<List<ChatMessage>>
    fun updateChatPartnerStatus(chatPartnerId: String): Observable<Long>
    fun updateUserTypingStatus(
        chatPartnerId: String,
        userId: String,
        isTyping: Boolean
    ): Completable

    fun listenForChatPartnerIsTyping(userId: String, chatPartnerId: String): Observable<Boolean>
}
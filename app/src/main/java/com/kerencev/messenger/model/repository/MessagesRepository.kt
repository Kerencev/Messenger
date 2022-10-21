package com.kerencev.messenger.model.repository

import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.utils.StatusOfSendingMessage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface MessagesRepository {
    fun getCurrentUser(): Single<User>
    fun listenForNewMessages(toId: String): Observable<ChatMessage>
    fun getAllMessages(toId: String): Single<Pair<List<ChatMessage>, Int>>
    fun resetUnreadMessages(toId: String): Completable
    fun getCountOfUnreadMessages(toId: String, fromId: String): Single<Long>
    fun saveMessageForAllNodes(
        message: String,
        currentUser: User,
        chatPartner: User
    ): Observable<StatusOfSendingMessage>
    fun sendPushToChatPartner(message: String, user: User, chatPartner: User): Completable
    fun updateChatPartnerInfo(chatPartnerId: String): Observable<User>
    fun updateUserTypingStatus(
        chatPartnerId: String,
        isTyping: Boolean
    ): Completable

    fun listenForChatPartnerIsTyping(chatPartnerId: String): Observable<Boolean>
}
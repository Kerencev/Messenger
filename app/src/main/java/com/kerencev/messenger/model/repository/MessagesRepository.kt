package com.kerencev.messenger.model.repository

import com.kerencev.messenger.data.remote.dto.PushNotification
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface MessagesRepository {

    fun getCurrentUser(): Single<User>

    fun listenForNewMessages(toId: String): Observable<ChatMessage>

    fun getAllMessages(toId: String): Single<Pair<List<ChatMessage>, Int>>

    fun resetUnreadMessages(toId: String): Completable

    fun saveMessageToUserMessagesNode(
        message: String,
        currentUser: User,
        chatPartner: User
    ): Completable

    fun saveMessageToPartnerMessagesNode(
        message: String,
        currentUser: User,
        chatPartner: User
    ): Completable

    fun saveMessageToUserLatestMessagesNode(
        message: String,
        currentUser: User,
        chatPartner: User
    ): Completable

    fun saveMessageToPartnerLatestMessagesNode(
        message: String,
        currentUser: User,
        chatPartner: User,
        countOfUnread: Long
    ): Completable

    fun getCountOfUnreadMessages(currentUserId: String, chatPartnerId: String): Single<Long>

    fun getTokenById(userId: String): Single<String>

    fun getUnreadMessages(
        currentUser: User,
        chatPartner: User,
        countOfUnread: Long
    ): Single<List<String>>

    fun sendPushToChatPartner(pushNotification: PushNotification): Completable

    fun updateChatPartnerInfo(chatPartnerId: String): Observable<User>

    fun updateUserTypingStatus(
        chatPartnerId: String,
        isTyping: Boolean
    ): Completable

    fun listenForChatPartnerIsTyping(chatPartnerId: String): Observable<Boolean>
}
package com.kerencev.messenger.model.repository

import com.kerencev.messenger.model.entities.ChatMessage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface FirebaseMessagesRepository {
    fun listenForNewMessages(fromId: String, toId: String): Observable<ChatMessage>
    fun getAllMessages(fromId: String, toId: String): Single<List<ChatMessage>>
    fun listenForLatestMessages(): Observable<ChatMessage>
    fun resetUnreadMessages(toId: String, fromId: String): Completable
    fun saveMessageFromId(chatMessage: ChatMessage): Completable
    fun saveMessageToId(chatMessage: ChatMessage): Completable
    fun saveLatestMessageFromId(chatMessage: ChatMessage): Single<ChatMessage>
    fun saveLatestMessageToId(chatMessage: ChatMessage): Completable
    fun getCountOfUnreadMessages(toId: String, fromId: String): Single<Long>
}
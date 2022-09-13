package com.kerencev.messenger.model.repository

import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.LatestMessage
import com.kerencev.messenger.model.entities.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface FirebaseRepository {
    fun verifyUserIsLoggedIn(): Single<Boolean>
    fun createUserWithEmailAndPassword(email: String, password: String): Completable
    fun signInWithEmailAndPassword(email: String, password: String): Completable
    fun saveUserToFirebaseDatabase(login: String, email: String): Completable
    fun signOut(): Completable
    fun getAllUsers(): Single<List<User>>
    fun getCurrentUserId(): Single<String>
    fun saveMessageToFireBase(message: String, fromId: String, toId: String): Completable
    fun listenForNewMessages(fromId: String, toId: String): Observable<ChatMessage>
    fun getAllMessages(fromId: String, toId: String): Single<List<ChatMessage>>
    fun listenForLatestMessages(): Observable<LatestMessage>
    fun resetUnreadMessages(toId: String, fromId: String): Completable
}
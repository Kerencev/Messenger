package com.kerencev.messenger.model.repository

import com.kerencev.messenger.model.entities.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface FirebaseAuthRepository {
    fun verifyUserIsLoggedIn(): Single<String>
    fun createUserWithEmailAndPassword(email: String, password: String): Completable
    fun signInWithEmailAndPassword(email: String, password: String): Completable
    fun saveUserToFirebaseDatabase(login: String, email: String): Completable
    fun signOut(): Completable
    fun getUserById(id: String): Single<User>
    fun saveUserStatus(userId: String, status: String): Completable
}
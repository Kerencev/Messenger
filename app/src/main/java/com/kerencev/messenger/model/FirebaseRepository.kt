package com.kerencev.messenger.model

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface FirebaseRepository {
    fun verifyUserIsLoggedIn(): Single<Boolean>
    fun createUserWithEmailAndPassword(email: String, password: String): Completable
    fun signInWithEmailAndPassword(email: String, password: String): Completable
    fun saveUserToFirebaseDatabase(login: String, email: String): Completable
    fun signOut(): Completable
}
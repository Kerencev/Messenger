package com.kerencev.messenger.model

import io.reactivex.rxjava3.core.Completable

interface FirebaseRepository {
    fun createUserWithEmailAndPassword(email: String, password: String): Completable
    fun signInWithEmailAndPassword(email: String, password: String): Completable
    fun saveUserToFirebaseDatabase(login: String, email: String): Completable
}
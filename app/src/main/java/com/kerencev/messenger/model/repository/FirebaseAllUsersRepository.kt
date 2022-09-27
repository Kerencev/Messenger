package com.kerencev.messenger.model.repository

import com.kerencev.messenger.model.entities.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface FirebaseAllUsersRepository {
    fun getAllUsers(): Single<List<User>>
    fun checkValidityLogin(text: String, listOfUsers: List<User>): Single<Boolean>
    fun updateUserLogin(uid: String, newLogin: String): Completable
}
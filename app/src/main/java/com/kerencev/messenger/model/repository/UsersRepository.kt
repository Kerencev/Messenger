package com.kerencev.messenger.model.repository

import com.kerencev.messenger.model.entities.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface UsersRepository {
    fun getAllUsers(): Single<List<User>>
    fun updateUserLoginForUsersNode(newLogin: String): Completable
    fun updateUserLoginForAllChatPartners(newLogin: String): Completable
}
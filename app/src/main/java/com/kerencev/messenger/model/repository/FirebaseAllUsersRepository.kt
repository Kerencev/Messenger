package com.kerencev.messenger.model.repository

import com.kerencev.messenger.model.entities.User
import io.reactivex.rxjava3.core.Single

interface FirebaseAllUsersRepository {
    fun getAllUsers(): Single<List<User>>
}
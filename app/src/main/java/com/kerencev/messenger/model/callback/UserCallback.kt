package com.kerencev.messenger.model.callback

import com.kerencev.messenger.model.entities.User

interface UserCallback {
    fun onSuccess(user: User)
    fun onError(e: Exception)
}
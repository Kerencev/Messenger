package com.kerencev.messenger.model.callback

interface CountOfUnreadCallback {
    fun onSuccess(count: Long)
    fun onError(e: Exception)
}
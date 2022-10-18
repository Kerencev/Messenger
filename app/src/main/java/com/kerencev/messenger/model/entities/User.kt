package com.kerencev.messenger.model.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String,
    val notificationId: Int,
    var login: String,
    val email: String,
    var wasOnline: Long,
    var status: String,
    val avatarUrl: String?
) : Parcelable {
    constructor() : this("", 0, "", "", -1, "", null)
}


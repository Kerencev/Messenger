package com.kerencev.messenger.model.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String,
    val login: String,
    val email: String,
    var wasOnline: Long,
    val avatarUrl: String?
) : Parcelable {
    constructor() : this("", "", "", -1, null)
}


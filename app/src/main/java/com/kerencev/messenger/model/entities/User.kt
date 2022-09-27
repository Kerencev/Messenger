package com.kerencev.messenger.model.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String,
    var login: String,
    val email: String,
    var wasOnline: Long,
    var status: String,
    val avatarUrl: String?
) : Parcelable {
    constructor() : this("", "", "", -1, "", null)
}


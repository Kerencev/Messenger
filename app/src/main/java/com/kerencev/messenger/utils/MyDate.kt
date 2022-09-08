package com.kerencev.messenger.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object MyDate {
    @SuppressLint("SimpleDateFormat")
    fun getTime(timestamp: Long): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        val date = Date(timestamp)
        return simpleDateFormat.format(date)
    }
}
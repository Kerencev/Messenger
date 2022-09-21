package com.kerencev.messenger.utils

import android.annotation.SuppressLint
import com.kerencev.messenger.services.StatusWorkManager
import java.text.SimpleDateFormat
import java.util.*

const val STATUS_ONLINE = "Онлайн"
private const val DAY_MILLISECONDS = 86400000

object MyDate {
    @SuppressLint("SimpleDateFormat")
    fun getTime(timestamp: Long): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        val date = Date(timestamp)
        return simpleDateFormat.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getChatPartnerStatus(wasOnline: Long): String {
        val currentTime = System.currentTimeMillis()
        val dateWasOnline = Date(wasOnline)
        return if (currentTime - wasOnline <= StatusWorkManager.LIMIT) {
            STATUS_ONLINE
        } else if (currentTime - wasOnline <= DAY_MILLISECONDS) {
            val simpleDateFormat = SimpleDateFormat("был(а) в HH:mm")
            simpleDateFormat.format(dateWasOnline)
        } else {
            val currentMont = getCurrentMont(dateWasOnline)
            val simpleDateFormat = SimpleDateFormat("был(а) dd $currentMont в HH:mm")
            simpleDateFormat.format(dateWasOnline)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentMont(dateWasOnline: Date): String {
        val simpleDateFormat = SimpleDateFormat("MM")
        return when (simpleDateFormat.format(dateWasOnline)) {
            "01" -> "янв."
            "02" -> "фев."
            "03" -> "марта"
            "04" -> "апр."
            "05" -> "мая"
            "06" -> "июня"
            "07" -> "июля"
            "08" -> "авг."
            "09" -> "сент."
            "10" -> "окт."
            "11" -> "ноя."
            "12" -> "дек."
            else -> ""
        }
    }
}
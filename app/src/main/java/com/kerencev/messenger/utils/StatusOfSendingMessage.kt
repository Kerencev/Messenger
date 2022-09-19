package com.kerencev.messenger.utils

sealed class StatusOfSendingMessage {
    object Status1 : StatusOfSendingMessage()
    object Status2 : StatusOfSendingMessage()
    object Status3 : StatusOfSendingMessage()
    object Status4 : StatusOfSendingMessage()
}

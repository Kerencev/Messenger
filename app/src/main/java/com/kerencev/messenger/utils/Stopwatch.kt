package com.kerencev.messenger.utils

class Stopwatch {
    private var seconds: Int = 0
    private var sec: Int = 0
    private var min: Int = 0

    fun getTime(): String {
        min = seconds % 3600 / 60
        sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
    }

    fun increment() {
        seconds++
    }

    fun resetTimer() {
        seconds = 0
        sec = 0
        min = 0
    }
}
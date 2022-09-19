package com.kerencev.messenger.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

private const val TAG = "TestService"

class TestService : Service(){

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, TestService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TestService::class.java)
            context.stopService(intent)
        }
    }
}
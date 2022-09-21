package com.kerencev.messenger.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kerencev.messenger.MessengerApp

/**
 * Service for updating information on the server when the user was online
 */
class StatusWorkManager(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        Thread {
            val user = MessengerApp.instance.user
            val userStatusRef =
                FirebaseDatabase.getInstance().getReference("/users/${user.uid}/wasOnline")
            val latestMessagesRef =
                FirebaseDatabase.getInstance().getReference("/latest-messages/${user.uid}")

            while (FirebaseAuth.getInstance().currentUser != null) {
                //updating the time when the user was online in the users node
                val timestamp = System.currentTimeMillis()
                userStatusRef.setValue(timestamp)

                //updating the time when the user was online in the latest-messages node for all chat partners
                latestMessagesRef.get()
                    .addOnSuccessListener { chatPartners ->
                        chatPartners.children.forEach { chatPartner ->
                            val chatPartnerLatestMessageRef = FirebaseDatabase.getInstance()
                                .getReference("/latest-messages/${chatPartner.key.toString()}/${user.uid}/chatPartnerWasOnline")
                            chatPartnerLatestMessageRef.setValue(timestamp)
                        }
                    }

                //updating local user info
                user.wasOnline = timestamp
                Thread.sleep(UPDATE_PERIOD)
            }
        }.start()
        return Result.success()
    }

    companion object {
        const val UPDATE_PERIOD = 10000L
        const val LIMIT = 15000L
    }
}
package com.kerencev.messenger.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Service for updating information on the server when the user was online
 */
class StatusWorkManager(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private var isCancelled = false

    override fun onStopped() {
        isCancelled = true
        super.onStopped()
    }

    override fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()
        val userStatusRef =
            FirebaseDatabase.getInstance().getReference("/users/$userId/wasOnline")
        val latestMessagesRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$userId")

        while (!isCancelled) {
            //updating the time when the user was online in the users node
            val timestamp = System.currentTimeMillis()
            userStatusRef.setValue(timestamp)

            //updating the time when the user was online in the latest-messages node for all chat partners
            latestMessagesRef.get()
                .addOnSuccessListener { chatPartners ->
                    chatPartners.children.forEach { chatPartner ->
                        val chatPartnerLatestMessageRef = FirebaseDatabase.getInstance()
                            .getReference("/latest-messages/${chatPartner.key.toString()}/$userId/chatPartnerWasOnline")
                        chatPartnerLatestMessageRef.setValue(timestamp)
                    }
                }
            Thread.sleep(UPDATE_PERIOD)
        }
        return Result.success()
    }

    companion object {
        const val UPDATE_PERIOD = 10000L
        const val LIMIT = 15000L
    }
}
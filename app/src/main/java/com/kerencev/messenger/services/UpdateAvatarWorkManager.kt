package com.kerencev.messenger.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Work manager to update the avatar url for all chat partners
 */
class UpdateAvatarWorkManager(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        Thread {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val userRef = FirebaseDatabase.getInstance().getReference("/users/$userId/avatarUrl")
            userRef.get()
                .addOnSuccessListener {
                    val userAvatar = it.value
                    val latestMessagesRef =
                        FirebaseDatabase.getInstance().getReference("/latest-messages/$userId")

                    //updating the avatar for all chat partners
                    latestMessagesRef.get()
                        .addOnSuccessListener { chatPartners ->
                            chatPartners.children.forEach { chatPartner ->
                                val chatPartnerLatestMessageRef = FirebaseDatabase.getInstance()
                                    .getReference("/latest-messages/${chatPartner.key.toString()}/$userId/chatPartnerAvatarUrl")
                                chatPartnerLatestMessageRef.setValue(userAvatar)
                            }
                        }
                }
        }.start()
        return Result.success()
    }
}
package com.kerencev.messenger.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kerencev.messenger.utils.FirebaseConstants.Companion.AVATAR_URL
import com.kerencev.messenger.utils.FirebaseConstants.Companion.CHAT_PARTNER_AVATAR_URL
import com.kerencev.messenger.utils.FirebaseConstants.Companion.LATEST_MESSAGES
import com.kerencev.messenger.utils.FirebaseConstants.Companion.USERS

/**
 * Work manager to update the avatar url for all chat partners
 */
class UpdateAvatarWorkManager(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        Thread {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val userRef = FirebaseDatabase.getInstance().getReference("/$USERS/$userId/$AVATAR_URL")
            userRef.get()
                .addOnSuccessListener {
                    val userAvatar = it.value
                    val latestMessagesRef =
                        FirebaseDatabase.getInstance().getReference("/$LATEST_MESSAGES/$userId")

                    //updating the avatar for all chat partners
                    latestMessagesRef.get()
                        .addOnSuccessListener { chatPartners ->
                            chatPartners.children.forEach { chatPartner ->
                                val chatPartnerLatestMessageRef = FirebaseDatabase.getInstance()
                                    .getReference("/$LATEST_MESSAGES/${chatPartner.key.toString()}/$userId/$CHAT_PARTNER_AVATAR_URL")
                                chatPartnerLatestMessageRef.setValue(userAvatar)
                            }
                        }
                }
        }.start()
        return Result.success()
    }
}
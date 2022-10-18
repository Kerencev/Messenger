package com.kerencev.messenger.services

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kerencev.messenger.ui.main.activity.MainActivity
import com.kerencev.messenger.utils.FirebaseConstants.Companion.CHAT_PARTNER_IS_ONLINE
import com.kerencev.messenger.utils.FirebaseConstants.Companion.CHAT_PARTNER_WAS_ONLINE
import com.kerencev.messenger.utils.FirebaseConstants.Companion.LATEST_MESSAGES
import com.kerencev.messenger.utils.FirebaseConstants.Companion.USERS
import com.kerencev.messenger.utils.FirebaseConstants.Companion.USER_STATUS
import com.kerencev.messenger.utils.FirebaseConstants.Companion.USER_STATUS_OFFLINE
import com.kerencev.messenger.utils.FirebaseConstants.Companion.USER_STATUS_ONLINE
import com.kerencev.messenger.utils.FirebaseConstants.Companion.USER_WAS_ONLINE
import com.kerencev.messenger.utils.log

@SuppressLint("SpecifyJobSchedulerIdRange")
class UserStatusJobService : JobService() {
    private var isJobCancelled = false

    override fun onStartJob(params: JobParameters?): Boolean {
        updateUserStatusWithFireBase(params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        isJobCancelled = true
        return true
    }

    private fun updateUserStatusWithFireBase(params: JobParameters?) {
        Thread {
            val userStatus = params?.extras?.getString(MainActivity.BUNDLE_KEY_JOB_STATUS)
                ?: USER_STATUS_OFFLINE
            val userIsOnline = (userStatus == USER_STATUS_ONLINE)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            userId?.let {
                val currentTime = System.currentTimeMillis()
                val userStatusRef =
                    FirebaseDatabase.getInstance().getReference("/$USERS/$userId/$USER_STATUS")
                val latestMessagesRef =
                    FirebaseDatabase.getInstance().getReference("/$LATEST_MESSAGES/$userId")
                val userWasOnlineRef =
                    FirebaseDatabase.getInstance().getReference("/$USERS/$userId/$USER_WAS_ONLINE")
                userStatusRef.setValue(userStatus)
                    .addOnSuccessListener {
                        userWasOnlineRef.setValue(currentTime)
                            .addOnSuccessListener {
                                latestMessagesRef.get()
                                    .addOnSuccessListener { chatPartners ->
                                        val countOfChatPartners = chatPartners.childrenCount.toInt()
                                        var counter = 0
                                        chatPartners.children.forEach { chatPartner ->
                                            val chatPartnerWasOnlineRef =
                                                FirebaseDatabase.getInstance()
                                                    .getReference("/$LATEST_MESSAGES/${chatPartner.key.toString()}/$userId/$CHAT_PARTNER_WAS_ONLINE")
                                            val chatPartnerIsOnlineRef =
                                                FirebaseDatabase.getInstance()
                                                    .getReference("/$LATEST_MESSAGES/${chatPartner.key.toString()}/$userId/$CHAT_PARTNER_IS_ONLINE")
                                            chatPartnerWasOnlineRef.setValue(currentTime)
                                                .addOnSuccessListener {
                                                    chatPartnerIsOnlineRef.setValue(userIsOnline)
                                                        .addOnSuccessListener {
                                                            counter++
                                                            if (counter == countOfChatPartners) {
                                                                log("Job finished")
                                                                jobFinished(params, false)
                                                            }
                                                        }
                                                }
                                        }
                                    }
                            }
                            .addOnFailureListener {
                                jobFinished(params, false)
                            }
                    }
                    .addOnFailureListener {
                        jobFinished(params, false)
                    }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy")
    }
}
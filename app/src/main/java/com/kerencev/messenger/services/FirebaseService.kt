package com.kerencev.messenger.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.ui.main.activity.MainActivity
import com.kerencev.messenger.utils.CircleImage

private const val CHANNEL_ID = "my_channel"

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val chatPartner = getChatPartner(message.data)
        val chatMessage = message.data["message"]

        if (MessengerApp.instance.chatPartner?.uid == chatPartner.uid) return

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager = notificationManager
        val notificationID = chatPartner.notificationId
        val intent = getIntent(chatPartner)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity(this, notificationID, intent, FLAG_MUTABLE)
        } else {
            getActivity(this, notificationID, intent, 0)
        }

        val remoteView = setUpRemoteView(chatPartner, chatMessage)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.push_small_icon)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteView)
            .setContentTitle(chatPartner.login)
            .setContentText(chatMessage)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

        notificationManager.notify(notificationID, notification)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            channel.canBubble()
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun getChatPartner(data: Map<String, String>): User {
        return User(
            uid = data["chatPartnerId"]!!,
            notificationId = data["notificationId"]!!.toInt(),
            login = data["chatPartnerLogin"]!!,
            email = data["chatPartnerEmail"]!!,
            wasOnline = -1,
            status = "",
            avatarUrl = data["chatPartnerAvatarUrl"]
        )
    }

    private fun getIntent(chatPartner: User): Intent {
        return Intent(this, MainActivity::class.java).apply {
            putExtra(PUSH_INTENT_PARTNER_ID, chatPartner.uid)
            putExtra(PUSH_INTENT_PARTNER_LOGIN, chatPartner.login)
            putExtra(PUSH_INTENT_PARTNER_EMAIL, chatPartner.email)
            putExtra(PUSH_INTENT_PARTNER_AVATAR, chatPartner.avatarUrl)
            putExtra(PUSH_INTENT_PARTNER_NOTIFICATION_ID, chatPartner.notificationId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    private fun setUpRemoteView(chatPartner: User, chatMessage: String?): RemoteViews {
        val remoteView = RemoteViews(packageName, R.layout.custom_notification_view)
        when (chatPartner.avatarUrl) {
            null -> {
                remoteView.setViewVisibility(R.id.notificationLetter, View.VISIBLE)
                remoteView.setTextViewText(
                    R.id.notificationLetter,
                    chatPartner.login.first().toString()
                )
            }
            else -> {
                remoteView.setViewVisibility(R.id.notificationAvatar, View.VISIBLE)
                val circleBitmap = CircleImage.getCircleBitmap(
                    context = this,
                    resources = resources,
                    imageUrl = chatPartner.avatarUrl
                )
                remoteView.setImageViewBitmap(
                    R.id.notificationAvatar,
                    circleBitmap
                )
            }
        }
        remoteView.setTextViewText(R.id.notificationLogin, chatPartner.login)
        remoteView.setTextViewText(R.id.notificationMessage, chatMessage)
        return remoteView
    }

    companion object {
        const val PUSH_INTENT_PARTNER_ID = "PUSH_INTENT_PARTNER_ID"
        const val PUSH_INTENT_PARTNER_LOGIN = "PUSH_INTENT_PARTNER_LOGIN"
        const val PUSH_INTENT_PARTNER_EMAIL = "PUSH_INTENT_PARTNER_EMAIL"
        const val PUSH_INTENT_PARTNER_AVATAR = "PUSH_INTENT_PARTNER_AVATAR"
        const val PUSH_INTENT_PARTNER_NOTIFICATION_ID = "PUSH_INTENT_PARTNER_NOTIFICATION_ID"
        var sharedPref: SharedPreferences? = null
        var notifManager: NotificationManager? = null
        var token: String?
            get() {
                return sharedPref?.getString("token", "")
            }
            set(value) {
                sharedPref?.edit()?.putString("token", value)?.apply()
            }
    }
}
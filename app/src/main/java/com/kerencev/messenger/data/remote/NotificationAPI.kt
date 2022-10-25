package com.kerencev.messenger.data.remote

import com.kerencev.messenger.data.remote.dto.PushNotification
import com.kerencev.messenger.utils.Constants.Companion.CONTENT_TYPE
import com.kerencev.messenger.utils.Constants.Companion.SERVER_KEY
import io.reactivex.rxjava3.core.Completable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    fun postNotification(
        @Body notification: PushNotification
    ): Completable
}
package com.kerencev.messenger.model.repository.impl

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.kerencev.messenger.data.remote.RetrofitInstance
import com.kerencev.messenger.data.remote.dto.NotificationData
import com.kerencev.messenger.data.remote.dto.PushNotification
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.FirebaseMessagesRepository
import com.kerencev.messenger.services.StatusWorkManager
import com.kerencev.messenger.utils.ChatMessageMapper
import com.kerencev.messenger.utils.MyDate
import com.kerencev.messenger.utils.StatusOfSendingMessage
import com.kerencev.messenger.utils.log
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class FirebaseMessagesRepositoryImpl : FirebaseMessagesRepository {

    override fun getCurrentUser(): Single<User> {
        return Single.create { emitter ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val userRef = FirebaseDatabase.getInstance().getReference("/users/$userId")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let { emitter.onSuccess(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    log(error.message)
                    emitter.onError(error.toException())
                }
            })
        }
    }

    override fun saveMessageForAllNodes(
        message: String,
        currenUser: User,
        chatPartner: User
    ): Observable<StatusOfSendingMessage> {
        return Observable.create { emitter ->
            val firebase = FirebaseDatabase.getInstance()
            val chatMessage = ChatMessageMapper.mapToChatMessage(message, currenUser, chatPartner)
            val referenceFromId =
                firebase.getReference("/user-messages/${chatMessage.fromId}/${chatMessage.toId}")
                    .push()
            val referenceToID =
                firebase.getReference("/user-messages/${chatMessage.toId}/${chatMessage.fromId}")
                    .push()
            val latestMessageRefFromId =
                firebase.getReference("/latest-messages/${chatMessage.fromId}/${chatMessage.toId}")
            val latestMessageRefToId =
                firebase.getReference("/latest-messages/${chatMessage.toId}/${chatMessage.fromId}")
            val countOfUnreadRef =
                firebase.getReference("/latest-messages/${chatMessage.toId}/${chatMessage.fromId}/countOfUnread")

            referenceFromId.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onNext(StatusOfSendingMessage.Status1)
                    referenceToID.setValue(chatMessage.copy(chatPartnerId = chatMessage.fromId))
                        .addOnSuccessListener {
                            emitter.onNext(StatusOfSendingMessage.Status2)
                            latestMessageRefFromId.setValue(chatMessage)
                                .addOnSuccessListener {
                                    emitter.onNext(StatusOfSendingMessage.Status3)
                                    countOfUnreadRef.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val latestMessageToId: ChatMessage
                                            when (val count: Long? = snapshot.value as Long?) {
                                                null -> {
                                                    latestMessageToId =
                                                        ChatMessageMapper.mapToLatestMessageForChatPartner(
                                                            1,
                                                            chatMessage,
                                                            currenUser
                                                        )
                                                }
                                                else -> {
                                                    latestMessageToId =
                                                        ChatMessageMapper.mapToLatestMessageForChatPartner(
                                                            count + 1,
                                                            chatMessage,
                                                            currenUser
                                                        )
                                                }
                                            }
                                            latestMessageRefToId.setValue(latestMessageToId)
                                                .addOnSuccessListener {
                                                    emitter.onNext(StatusOfSendingMessage.Status4)
                                                    emitter.onComplete()
                                                }
                                                .addOnFailureListener {
                                                    emitter.onError(it)
                                                }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            emitter.onError(error.toException())
                                        }
                                    })
                                }
                                .addOnFailureListener {
                                    emitter.onError(it)
                                }
                        }
                        .addOnFailureListener {
                            emitter.onError(it)
                        }
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun sendPushToChatPartner(
        message: String,
        user: User,
        chatPartner: User
    ): Completable {
        return Completable.create { emitter ->
            val chatPartnerTokenRef =
                FirebaseDatabase.getInstance().getReference("/users/${chatPartner.uid}/token")
            val countOfUnreadRef = FirebaseDatabase.getInstance()
                .getReference("/latest-messages/${chatPartner.uid}/${user.uid}/countOfUnread")
            val messagesRef = FirebaseDatabase.getInstance()
                .getReference("/user-messages/${user.uid}/${chatPartner.uid}")
            chatPartnerTokenRef.get()
                .addOnSuccessListener { tokenData ->
                    val recipientToken = tokenData.getValue<String>()
                    countOfUnreadRef.get()
                        .addOnSuccessListener { countOfUnreadData ->
                            val countOfUnread = countOfUnreadData.getValue<Long>() ?: 0
                            messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    var pushMessage = ""
                                    val countOfMessages = snapshot.children.count()
                                    for (i in countOfMessages - countOfUnread until countOfMessages) {
                                        val unreadMessage = snapshot.children.elementAt(i.toInt())
                                            .getValue(ChatMessage::class.java)
                                        val text = unreadMessage?.message
                                        pushMessage += if (i == countOfMessages.toLong() - 1) {
                                            "$text"
                                        } else {
                                            "$text\n"
                                        }
                                    }
                                    recipientToken?.let {
                                        if (recipientToken.isNotEmpty()) {
                                            val push = PushNotification(
                                                NotificationData(
                                                    message = pushMessage,
                                                    chatPartnerId = user.uid,
                                                    notificationId = user.notificationId.toString(),
                                                    chatPartnerLogin = user.login,
                                                    chatPartnerEmail = user.email,
                                                    chatPartnerAvatarUrl = user.avatarUrl
                                                ),
                                                recipientToken
                                            )
                                            RetrofitInstance.api.postNotification(push)
                                                .subscribeOn(Schedulers.io())
                                                .subscribe()
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        }
                }
        }
    }

    override fun getCountOfUnreadMessages(
        toId: String,
        fromId: String
    ): Single<Long> {
        return Single.create { emitter ->
            val countOfUnreadRef = FirebaseDatabase.getInstance()
                .getReference("/latest-messages/$toId/$fromId/countOfUnread")
            countOfUnreadRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    when (val count: Long? = snapshot.value as Long?) {
                        null -> emitter.onSuccess(0)
                        else -> emitter.onSuccess(count)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    emitter.onError(error.toException())
                }
            })
        }
    }

    override fun listenForNewMessages(toId: String): Observable<ChatMessage> {
        return Observable.create { emitter ->
            val fromId = FirebaseAuth.getInstance().currentUser?.uid
            val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatMessage = snapshot.getValue(ChatMessage::class.java)
                    chatMessage?.let {
                        emitter.onNext(it)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) =
                    Unit

                override fun onChildRemoved(snapshot: DataSnapshot) = Unit
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
                override fun onCancelled(error: DatabaseError) = Unit
            })
        }
    }

    override fun getAllMessages(
        toId: String
    ): Single<Pair<List<ChatMessage>, Int>> {
        return Single.create { emitter ->
            val fromId = FirebaseAuth.getInstance().currentUser?.uid
            val result = ArrayList<ChatMessage>()
            val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tempDate = ""
                    var dateCount = 0
                    snapshot.children.forEach {
                        val message = it.getValue(ChatMessage::class.java)
                        message?.let {
                            val date = MyDate.getDate(message.timesTamp)
                            if (date != tempDate) {
                                result.add(
                                    ChatMessage(message = date)
                                )
                                tempDate = date
                                dateCount++
                            }
                            result.add(message)
                        }
                    }
                    emitter.onSuccess(Pair(result, dateCount))
                }

                override fun onCancelled(error: DatabaseError) {
                    emitter.onError(error.toException())
                }
            })
        }
    }

    override fun resetUnreadMessages(toId: String): Completable {
        return Completable.create { emitter ->
            val fromId = FirebaseAuth.getInstance().currentUser?.uid
            val ref = FirebaseDatabase.getInstance()
                .getReference("/latest-messages/$fromId/$toId/countOfUnread")
            ref.setValue(0)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun listenForLatestMessages(): Observable<ChatMessage> {
        return Observable.create { emitter ->
            val fromId = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val latestMessage = snapshot.getValue(ChatMessage::class.java)
                    latestMessage?.let { message ->
                        message.chatPartnerIsOnline =
                            System.currentTimeMillis() - message.chatPartnerWasOnline <= StatusWorkManager.LIMIT
                        if (message.message.isNotEmpty()) {
                            emitter.onNext(message)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val latestMessage = snapshot.getValue(ChatMessage::class.java)
                    latestMessage?.let { message ->
                        message.chatPartnerIsOnline =
                            System.currentTimeMillis() - message.chatPartnerWasOnline <= StatusWorkManager.LIMIT
                        if (message.message.isNotEmpty()) {
                            emitter.onNext(message)
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) = Unit
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
                override fun onCancelled(error: DatabaseError) = Unit
            })
        }
    }

    /**
     * Function to update all latest messages with a period
     * to update chat partner status for all chat partners
     */
    override fun updateAllLatestMessages(): Observable<List<ChatMessage>> {
        return Observable.create { emitter ->
            val fromId = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
            val result = ArrayList<ChatMessage>()
            while (true) {
                Thread.sleep(StatusWorkManager.LIMIT)
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        result.clear()
                        snapshot.children.forEach { latestMessage ->
                            val message = latestMessage.getValue(ChatMessage::class.java)
                            message?.let {
                                message.chatPartnerIsOnline =
                                    System.currentTimeMillis() - message.chatPartnerWasOnline <= StatusWorkManager.LIMIT
                                if (message.message.isNotEmpty()) {
                                    result.add(message)
                                }
                            }
                        }
                        emitter.onNext(result)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        emitter.onError(error.toException())
                    }
                })
            }
        }
    }

    /**
     * We update the chat partner with the period exactly in the cycle
     * if the chat partner comes out, then after a certain time we will find out about it
     */
    override fun updateChatPartnerInfo(chatPartnerId: String): Observable<User> {
        return Observable.create { emitter ->
            val userIdRef = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            while (true) {
                userIdRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let { emitter.onNext(it) }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        emitter.onError(error.toException())
                    }
                })
                Thread.sleep(StatusWorkManager.UPDATE_PERIOD)
            }
        }
    }

    override fun updateUserTypingStatus(
        chatPartnerId: String,
        isTyping: Boolean
    ): Completable {
        return Completable.create {
            val fromId = FirebaseAuth.getInstance().currentUser?.uid
            //Update typing status for user ref
            val userRef = FirebaseDatabase.getInstance().getReference("/users/$fromId/typingFor")
            when (isTyping) {
                true -> userRef.setValue(chatPartnerId)
                false -> userRef.setValue("")
            }
            //Update typing status for chat partner's latest messages
            val latestRef = FirebaseDatabase.getInstance()
                .getReference("/latest-messages/$chatPartnerId/$fromId/chatPartnerIsTyping")
            latestRef.setValue(isTyping)
            it.onComplete()
        }
    }

    override fun listenForChatPartnerIsTyping(
        chatPartnerId: String
    ): Observable<Boolean> {
        return Observable.create { emitter ->
            val fromId = FirebaseAuth.getInstance().currentUser?.uid
            val ref = FirebaseDatabase.getInstance()
                .getReference("/users/$chatPartnerId/typingFor")
            ref.addValueEventListener(object : ValueEventListener {
                @SuppressLint("LongLogTag")
                override fun onDataChange(snapshot: DataSnapshot) {
                    when (snapshot.value as String?) {
                        fromId -> emitter.onNext(true)
                        else -> emitter.onNext(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    emitter.onError(error.toException())
                }
            })
        }
    }
}
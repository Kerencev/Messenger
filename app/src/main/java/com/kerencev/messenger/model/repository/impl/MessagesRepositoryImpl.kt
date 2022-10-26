package com.kerencev.messenger.model.repository.impl

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.kerencev.messenger.data.remote.NotificationAPI
import com.kerencev.messenger.data.remote.dto.PushNotification
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.MessagesRepository
import com.kerencev.messenger.services.StatusWorkManager
import com.kerencev.messenger.utils.ChatMessageMapper
import com.kerencev.messenger.utils.FirebaseConstants.Companion.COUNT_OF_UNREAD
import com.kerencev.messenger.utils.FirebaseConstants.Companion.LATEST_MESSAGES
import com.kerencev.messenger.utils.FirebaseConstants.Companion.TOKEN
import com.kerencev.messenger.utils.FirebaseConstants.Companion.USERS
import com.kerencev.messenger.utils.FirebaseConstants.Companion.USER_MESSAGES
import com.kerencev.messenger.utils.MyDate
import com.kerencev.messenger.utils.log
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class MessagesRepositoryImpl (private val notificationAPI: NotificationAPI) : MessagesRepository {

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

    override fun saveMessageToUserMessagesNode(
        message: String,
        currentUser: User,
        chatPartner: User
    ): Completable {
        return Completable.create { emitter ->
            val firebase = FirebaseDatabase.getInstance()
            val chatMessage =
                ChatMessageMapper.mapToChatMessageForUserNode(message, currentUser, chatPartner)
            val referenceFromId =
                firebase.getReference("/$USER_MESSAGES/${chatMessage.fromId}/${chatMessage.toId}")
                    .push()
            referenceFromId.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun saveMessageToPartnerMessagesNode(
        message: String,
        currentUser: User,
        chatPartner: User
    ): Completable {
        return Completable.create { emitter ->
            val firebase = FirebaseDatabase.getInstance()
            val chatMessage =
                ChatMessageMapper.mapToChatMessageForPartnerNode(message, currentUser, chatPartner)
            val referenceToID =
                firebase.getReference("/$USER_MESSAGES/${chatMessage.toId}/${chatMessage.fromId}")
                    .push()
            referenceToID.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun saveMessageToUserLatestMessagesNode(
        message: String,
        currentUser: User,
        chatPartner: User
    ): Completable {
        return Completable.create { emitter ->
            val firebase = FirebaseDatabase.getInstance()
            val chatMessage =
                ChatMessageMapper.mapToChatMessageForUserNode(message, currentUser, chatPartner)
            val latestMessageRefFromId =
                firebase.getReference("/latest-messages/${chatMessage.fromId}/${chatMessage.toId}")
            latestMessageRefFromId.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun saveMessageToPartnerLatestMessagesNode(
        message: String,
        currentUser: User,
        chatPartner: User,
        countOfUnread: Long
    ): Completable {
        return Completable.create { emitter ->
            val firebase = FirebaseDatabase.getInstance()
            val chatMessage =
                ChatMessageMapper.mapToLatestMessageForChatPartner(
                    currentUser = currentUser,
                    chatPartner = chatPartner,
                    message = message,
                    countOfUnread = countOfUnread
                )
            val latestMessageRefToId =
                firebase.getReference("/$LATEST_MESSAGES/${chatMessage.toId}/${chatMessage.fromId}")
            latestMessageRefToId.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun getCountOfUnreadMessages(
        currentUserId: String,
        chatPartnerId: String
    ): Single<Long> {
        return Single.create { emitter ->
            val countOfUnreadRef = FirebaseDatabase.getInstance()
                .getReference("/$LATEST_MESSAGES/$chatPartnerId/$currentUserId/$COUNT_OF_UNREAD")
            countOfUnreadRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count: Long? = snapshot.value as Long?
                    log(count.toString())
                    when (count) {
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

    override fun getTokenById(userId: String): Single<String> {
        return Single.create { emitter ->
            val tokenRef =
                FirebaseDatabase.getInstance().getReference("/$USERS/$userId/$TOKEN")
            tokenRef.get()
                .addOnSuccessListener { data ->
                    val token = data.getValue<String>()
                    token?.let { emitter.onSuccess(it) }
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun getUnreadMessages(
        currentUser: User,
        chatPartner: User,
        countOfUnread: Long
    ): Single<List<String>> {
        return Single.create { emitter ->
            val messagesRef = FirebaseDatabase.getInstance()
                .getReference("/$USER_MESSAGES/${currentUser.uid}/${chatPartner.uid}")
            messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listOfUnreadMessages = ArrayList<String>()
                    val countOfMessages = snapshot.children.count()
                    for (i in countOfMessages - countOfUnread until countOfMessages) {
                        val unreadMessage = snapshot.children.elementAt(i.toInt())
                            .getValue(ChatMessage::class.java)
                        val text = unreadMessage?.message
                        text?.let { listOfUnreadMessages.add(text) }
                    }
                    emitter.onSuccess(listOfUnreadMessages)
                }

                override fun onCancelled(error: DatabaseError) {
                    emitter.onError(error.toException())
                }
            })
        }
    }

    override fun sendPushToChatPartner(pushNotification: PushNotification): Completable {
        return notificationAPI.postNotification(pushNotification)
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
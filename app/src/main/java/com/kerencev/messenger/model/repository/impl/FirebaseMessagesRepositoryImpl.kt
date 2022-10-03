package com.kerencev.messenger.model.repository.impl

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.FirebaseMessagesRepository
import com.kerencev.messenger.services.StatusWorkManager
import com.kerencev.messenger.utils.ChatMessageMapper
import com.kerencev.messenger.utils.MyDate
import com.kerencev.messenger.utils.StatusOfSendingMessage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

private const val TAG = "FirebaseMessagesRepositoryImpl"

class FirebaseMessagesRepositoryImpl : FirebaseMessagesRepository {

    override fun saveMessageForAllNodes(
        message: String,
        user: User,
        chatPartner: User
    ): Observable<StatusOfSendingMessage> {
        return Observable.create { emitter ->
            val firebase = FirebaseDatabase.getInstance()
            val chatMessage = ChatMessageMapper.mapToChatMessage(message, user, chatPartner)
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
                    referenceToID.setValue(chatMessage)
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
                                                            user
                                                        )
                                                }
                                                else -> {
                                                    latestMessageToId =
                                                        ChatMessageMapper.mapToLatestMessageForChatPartner(
                                                            count + 1,
                                                            chatMessage,
                                                            user
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

    override fun listenForNewMessages(fromId: String, toId: String): Observable<ChatMessage> {
        return Observable.create { emitter ->
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

    override fun getAllMessages(fromId: String, toId: String): Single<Pair<List<ChatMessage>, Int>> {
        return Single.create { emitter ->
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

    override fun resetUnreadMessages(toId: String, fromId: String): Completable {
        return Completable.create { emitter ->
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

    override fun updateChatPartnerStatus(chatPartnerId: String): Observable<Long> {
        return Observable.create { emitter ->
            val userIdRef = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            while (true) {
                userIdRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let { emitter.onNext(it.wasOnline) }
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
        userId: String,
        isTyping: Boolean
    ): Completable {
        return Completable.create {
            //Update typing status for users ref
            val userRef = FirebaseDatabase.getInstance().getReference("/users/$userId/typingFor")
            when (isTyping) {
                true -> userRef.setValue(chatPartnerId)
                false -> userRef.setValue("")
            }
            //Update typing status for chat partner's latest messages
            val latestRef = FirebaseDatabase.getInstance()
                .getReference("/latest-messages/$chatPartnerId/$userId/chatPartnerIsTyping")
            latestRef.setValue(isTyping)
            it.onComplete()
        }
    }

    override fun listenForChatPartnerIsTyping(
        userId: String,
        chatPartnerId: String
    ): Observable<Boolean> {
        return Observable.create { emitter ->
            val ref = FirebaseDatabase.getInstance()
                .getReference("/users/$chatPartnerId/typingFor")
            ref.addValueEventListener(object : ValueEventListener {
                @SuppressLint("LongLogTag")
                override fun onDataChange(snapshot: DataSnapshot) {
                    when (snapshot.value as String?) {
                        userId -> emitter.onNext(true)
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
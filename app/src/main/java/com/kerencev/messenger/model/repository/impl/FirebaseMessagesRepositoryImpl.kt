package com.kerencev.messenger.model.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.repository.FirebaseMessagesRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class FirebaseMessagesRepositoryImpl : FirebaseMessagesRepository {

    override fun saveMessageFromId(
        chatMessage: ChatMessage
    ): Completable {
        return Completable.create { emitter ->
            val referenceFromId =
                FirebaseDatabase.getInstance()
                    .getReference("/user-messages/${chatMessage.fromId}/${chatMessage.toId}").push()
            referenceFromId.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun saveMessageToId(
        chatMessage: ChatMessage,
    ): Completable {
        return Completable.create { emitter ->
            val referenceToID =
                FirebaseDatabase.getInstance()
                    .getReference("/user-messages/${chatMessage.toId}/${chatMessage.fromId}").push()
            referenceToID.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun saveLatestMessageFromId(
        chatMessage: ChatMessage,
    ): Single<ChatMessage> {
        return Single.create { emitter ->
            val latestMessageRef =
                FirebaseDatabase
                    .getInstance()
                    .getReference("/latest-messages/${chatMessage.fromId}/${chatMessage.toId}")

            latestMessageRef.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onSuccess(chatMessage)
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun saveLatestMessageToId(
        chatMessage: ChatMessage
    ): Completable {
        return Completable.create { emitter ->
            val latestMessageRef =
                FirebaseDatabase.getInstance()
                    .getReference("/latest-messages/${chatMessage.toId}/${chatMessage.fromId}")
            latestMessageRef.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onComplete()
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

    override fun getAllMessages(fromId: String, toId: String): Single<List<ChatMessage>> {
        return Single.create { emitter ->
            val result = ArrayList<ChatMessage>()
            val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val message = it.getValue(ChatMessage::class.java)
                        message?.let { it1 -> result.add(it1) }
                    }
                    emitter.onSuccess(result)
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
                    latestMessage?.let { emitter.onNext(it) }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val latestMessage = snapshot.getValue(ChatMessage::class.java)
                    latestMessage?.let { emitter.onNext(it) }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) = Unit
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
                override fun onCancelled(error: DatabaseError) = Unit
            })
        }
    }
}
package com.kerencev.messenger.model.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.repository.LatestMessagesRepository
import com.kerencev.messenger.services.StatusWorkManager
import io.reactivex.rxjava3.core.Observable

class LatestMessagesRepositoryImpl : LatestMessagesRepository {

    /**
     * Listening to the additions or changes of latest messages with all chat partners
     */
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
     * to update chat partners info
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
}
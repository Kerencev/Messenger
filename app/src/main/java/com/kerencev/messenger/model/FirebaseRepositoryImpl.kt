package com.kerencev.messenger.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kerencev.messenger.model.callback.CountOfUnreadCallback
import com.kerencev.messenger.model.callback.IsSuccessCallBack
import com.kerencev.messenger.model.callback.UserCallback
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.LatestMessage
import com.kerencev.messenger.model.entities.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class FirebaseRepositoryImpl : FirebaseRepository {

    override fun verifyUserIsLoggedIn(): Single<Boolean> {
        return Single.create {
            val uid = FirebaseAuth.getInstance().uid
            if (uid == null) {
                it.onSuccess(false)
            } else {
                it.onSuccess(true)
            }
        }
    }

    override fun createUserWithEmailAndPassword(email: String, password: String): Completable {
        return Completable.create { emitter ->
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        emitter.onComplete()
                    } else {
                        emitter.onError(Exception())
                        Log.d(TAG, "Failed to create user: $it")
                    }
                }
                .addOnFailureListener {
                    emitter.onError(it)
                    Log.d(TAG, "Failed to create user: $it")
                }
        }
    }

    override fun signInWithEmailAndPassword(email: String, password: String): Completable {
        return Completable.create { emitter ->
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(Exception())
                }
        }
    }

    override fun saveUserToFirebaseDatabase(login: String, email: String): Completable {
        return Completable.create { emitter ->
            val uid = FirebaseAuth.getInstance().uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            val user = User(uid = uid, login = login, email = email, avatarUrl = null)
            ref.setValue(user)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun signOut(): Completable {
        return Completable.create {
            FirebaseAuth.getInstance().signOut()
            it.onComplete()
        }
    }

    override fun getAllUsers(): Single<List<User>> {
        return Single.create { emitter ->
            val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val ref = FirebaseDatabase.getInstance().getReference("/users")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = ArrayList<User>()
                    snapshot.children.forEach { dataSnapshot ->
                        val user = dataSnapshot.getValue(User::class.java)
                        user?.let {
                            //Don't add yourself to the users list
                            if (user.uid != currentUser) {
                                result.add(user)
                            }
                        }
                    }
                    emitter.onSuccess(result)
                }

                override fun onCancelled(error: DatabaseError) {
                    emitter.onError(Exception())
                }
            })
        }
    }

    override fun getCurrentUserId(): Single<String> {
        return Single.create { emitter ->
            val fromId = FirebaseAuth.getInstance().uid.toString()
            emitter.onSuccess(fromId)
        }
    }

    override fun saveMessageToFireBase(
        message: String,
        fromId: String,
        toId: String
    ): Completable {
        return Completable.create { emitter ->
            saveMessageForAllNodes(message, fromId, toId, object : IsSuccessCallBack {
                override fun onSuccess() {
                    emitter.onComplete()
                }

                override fun onFailure() {
                    emitter.onError(Exception())
                }
            })
        }
    }

    /**
     * Function to save message for all nodes of the FireBase Database built on callbacks
     * If it is not possible to save the message to at least one of the nodes, the callback will return onFailure
     */
    private fun saveMessageForAllNodes(
        message: String,
        fromId: String,
        toId: String,
        callback: IsSuccessCallBack
    ) {
        val time = System.currentTimeMillis()
        val referenceToId =
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val chatMessage = ChatMessage(
            referenceToId.key!!,
            message,
            fromId,
            toId,
            time,
        )
        referenceToId.setValue(chatMessage)
            .addOnSuccessListener {
                saveMessageToId(chatMessage, fromId, toId, object : IsSuccessCallBack {
                    override fun onSuccess() {
                        callback.onSuccess()
                    }

                    override fun onFailure() {
                        callback.onFailure()
                    }

                })
            }
            .addOnFailureListener {
                callback.onFailure()
            }
    }

    private fun saveMessageToId(
        chatMessage: ChatMessage,
        fromId: String,
        toId: String,
        callback: IsSuccessCallBack
    ) {
        val toReference =
            FirebaseDatabase.getInstance()
                .getReference("/user-messages/$toId/$fromId").push()
        toReference.setValue(chatMessage)
            .addOnSuccessListener {
                saveLatestMessageFromId(chatMessage, fromId, toId, object : IsSuccessCallBack {
                    override fun onSuccess() {
                        callback.onSuccess()
                    }

                    override fun onFailure() {
                        callback.onFailure()
                    }

                })
            }
            .addOnFailureListener {
                callback.onFailure()
            }
    }

    private fun saveLatestMessageFromId(
        chatMessage: ChatMessage,
        fromId: String,
        toId: String,
        callback: IsSuccessCallBack
    ) {
        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

        getUserById(toId, object : UserCallback {
            override fun onSuccess(user: User) {
                val latestMessage = LatestMessage(
                    id = chatMessage.id,
                    message = chatMessage.message,
                    fromId = chatMessage.fromId,
                    toId = chatMessage.toId,
                    timesTamp = chatMessage.timesTamp,
                    countOfUnread = 0,
                    chatPartnerId = user.uid,
                    chatPartnerLogin = user.login,
                    chatPartnerEmail = user.email,
                    user.avatarUrl
                )
                latestMessageRef.setValue(latestMessage)
                    .addOnSuccessListener {
                        saveLatestMessageToId(
                            latestMessage,
                            fromId,
                            toId,
                            object : IsSuccessCallBack {
                                override fun onSuccess() {
                                    callback.onSuccess()
                                }

                                override fun onFailure() {
                                    callback.onFailure()
                                }
                            })
                    }
                    .addOnFailureListener {
                        callback.onFailure()
                    }
            }

            override fun onError(e: Exception) {
                callback.onFailure()
            }
        })
    }

    private fun saveLatestMessageToId(
        latestMessage: LatestMessage,
        fromId: String,
        toId: String,
        callback: IsSuccessCallBack
    ) {
        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        getUserById(fromId, object : UserCallback {
            override fun onSuccess(user: User) {
                getCountOfUnreadMessages(toId, fromId, object : CountOfUnreadCallback {
                    override fun onSuccess(count: Long) {
                        latestMessage.chatPartnerId = user.uid
                        latestMessage.chatPartnerLogin = user.login
                        latestMessage.chatPartnerEmail = user.email
                        latestMessage.ChatPartnerAvatarUrl = user.avatarUrl
                        latestMessage.countOfUnread = count
                        latestMessageRef.setValue(latestMessage)
                            .addOnSuccessListener {
                                callback.onSuccess()
                            }
                            .addOnFailureListener {
                                callback.onFailure()
                            }
                    }

                    override fun onError(e: Exception) {
                        callback.onFailure()
                    }

                })
            }

            override fun onError(e: Exception) {
                callback.onFailure()
            }
        })
    }

    private fun getCountOfUnreadMessages(toId: String, fromId: String, callback: CountOfUnreadCallback) {
        val test = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId/countOfUnread")
        test.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count: Long? = snapshot.value as Long?
                when(count) {
                    null -> callback.onSuccess(1)
                    else -> callback.onSuccess(++count)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }

        })
    }

    private fun getUserById(id: String, callback: UserCallback) {
        val chatPartner = FirebaseDatabase.getInstance().getReference("/users/$id")
        chatPartner.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let { callback.onSuccess(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }
        })
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

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit
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
                    emitter.onError(Exception())
                }
            })
        }
    }

    override fun resetUnreadMessages(toId: String, fromId: String): Completable {
        return Completable.create { emitter ->
            val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId/countOfUnread")
            ref.setValue(0)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun listenForLatestMessages(): Observable<LatestMessage> {
        return Observable.create { emitter ->
            val fromId = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val latestMessage = snapshot.getValue(LatestMessage::class.java)
                    latestMessage?.let { emitter.onNext(it) }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val latestMessage = snapshot.getValue(LatestMessage::class.java)
                    latestMessage?.let { emitter.onNext(it) }
                }
                override fun onChildRemoved(snapshot: DataSnapshot) = Unit
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
                override fun onCancelled(error: DatabaseError) = Unit
            })
        }
    }

    companion object {
        private const val TAG = "FirebaseRepositoryImpl"
    }
}
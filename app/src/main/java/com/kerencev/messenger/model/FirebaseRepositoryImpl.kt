package com.kerencev.messenger.model

import android.util.Log
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kerencev.messenger.R
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.utils.MyDate
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

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

    override fun saveMessageFromId(
        message: String,
        fromId: String,
        toId: String
    ): Single<ChatMessage> {
        return Single.create { emitter ->
            val time = MyDate.getTime(System.currentTimeMillis())
            Log.d(TAG, "time: $time")
            val reference =
                FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
            val chatMessage = ChatMessage(
                reference.key!!,
                message,
                fromId,
                toId,
                time,
            )
            reference.setValue(chatMessage)
                .addOnSuccessListener {
                    emitter.onSuccess(chatMessage)
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun saveMessageToId(message: ChatMessage): Completable {
        return Completable.create { emitter ->
            val toReference =
                FirebaseDatabase.getInstance()
                    .getReference("/user-messages/${message.toId}/${message.fromId}").push()
            toReference.setValue(message)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
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

    companion object {
        private const val TAG = "FirebaseRepositoryImpl"
    }
}
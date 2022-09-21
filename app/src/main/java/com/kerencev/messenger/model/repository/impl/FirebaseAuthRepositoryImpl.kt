package com.kerencev.messenger.model.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.FirebaseAuthRepository
import com.kerencev.messenger.services.StatusWorkManager
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

private const val TAG = "FirebaseAuthRepositoryImpl"

class FirebaseAuthRepositoryImpl : FirebaseAuthRepository {
    override fun verifyUserIsLoggedIn(): Single<String> {
        return Single.create { emitter ->
            val uid = FirebaseAuth.getInstance().uid
            if (uid == null) {
                emitter.onSuccess("")
            } else {
                emitter.onSuccess(uid.toString())
            }
        }
    }

    override fun createUserWithEmailAndPassword(email: String, password: String): Completable {
        return Completable.create { emitter ->
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { createUserTask ->
                    if (createUserTask.isSuccessful) {
                        emitter.onComplete()
                    } else {
                        createUserTask.exception?.let { emitter.onError(it) }
                    }
                }
                .addOnFailureListener {
                    emitter.onError(it)
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
            val user =
                User(
                    uid = uid,
                    login = login,
                    email = email,
                    wasOnline = -1,
                    status = "",
                    avatarUrl = null
                )
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

    override fun getUserById(id: String): Single<User> {
        return Single.create { emitter ->
            val userIdRef = FirebaseDatabase.getInstance().getReference("/users/$id")
            userIdRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let { emitter.onSuccess(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    emitter.onError(error.toException())
                }
            })
            Thread.sleep(StatusWorkManager.UPDATE_PERIOD)
        }
    }
}
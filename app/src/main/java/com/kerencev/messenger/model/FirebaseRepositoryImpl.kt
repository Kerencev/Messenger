package com.kerencev.messenger.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kerencev.messenger.model.entities.User
import io.reactivex.rxjava3.core.Completable
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
            val user = User(uid = uid, login = login, email = email)
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

    companion object {
        private const val TAG = "FirebaseRepositoryImpl"
    }
}
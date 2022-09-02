package com.kerencev.messenger.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Completable

class FirebaseRepositoryImpl : FirebaseRepository {
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

    companion object {
        private const val TAG = "FirebaseRepositoryImpl"
    }
}
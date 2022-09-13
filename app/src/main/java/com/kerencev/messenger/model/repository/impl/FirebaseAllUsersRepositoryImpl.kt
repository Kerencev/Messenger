package com.kerencev.messenger.model.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.FirebaseAllUsersRepository
import io.reactivex.rxjava3.core.Single

class FirebaseAllUsersRepositoryImpl : FirebaseAllUsersRepository {
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
}
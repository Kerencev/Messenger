package com.kerencev.messenger.model.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.UsersRepository
import com.kerencev.messenger.utils.FirebaseConstants
import com.kerencev.messenger.utils.FirebaseConstants.Companion.CHAT_PARTNER_AVATAR_URL
import com.kerencev.messenger.utils.FirebaseConstants.Companion.CHAT_PARTNER_LOGIN
import com.kerencev.messenger.utils.FirebaseConstants.Companion.LATEST_MESSAGES
import com.kerencev.messenger.utils.FirebaseConstants.Companion.LOGIN
import com.kerencev.messenger.utils.FirebaseConstants.Companion.USERS
import com.kerencev.messenger.utils.MyDate
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class UsersRepositoryImpl : UsersRepository {
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
                                user.status = MyDate.getChatPartnerStatus(user.wasOnline)
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

    override fun updateUserLoginForUsersNode(newLogin: String): Completable {
        return Completable.create { emitter ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val userRef = FirebaseDatabase.getInstance().getReference("/$USERS/$userId/$LOGIN")
            userRef.setValue(newLogin)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun updateUserLoginForAllChatPartners(newLogin: String): Completable {
        return Completable.create { emitter ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val latestMessagesRef =
                FirebaseDatabase.getInstance()
                    .getReference("/${LATEST_MESSAGES}/$userId")
            latestMessagesRef.get()
                .addOnSuccessListener { chatPartners ->
                    chatPartners.children.forEach { chatPartner ->
                        val chatPartnerLatestMessageRef = FirebaseDatabase.getInstance()
                            .getReference("/${LATEST_MESSAGES}/${chatPartner.key.toString()}/$userId/${CHAT_PARTNER_LOGIN}")
                        chatPartnerLatestMessageRef.setValue(newLogin)
                            .addOnSuccessListener { emitter.onComplete() }
                            .addOnFailureListener { emitter.onError(it) }
                    }
                }
                .addOnFailureListener { emitter.onError(it) }
        }
    }
}
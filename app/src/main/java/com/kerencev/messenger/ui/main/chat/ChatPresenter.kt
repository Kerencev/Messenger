package com.kerencev.messenger.ui.main.chat

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.FirebaseRepository
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault

private const val TAG = "ChatPresenter"

class ChatPresenter(
    private val repository: FirebaseRepository,
    private val router: Router
) : BasePresenter<ChatView>(
    router
) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getCurrentUserIdFromFirebase()
        viewState.loadUserAvatar()
    }

    private fun getCurrentUserIdFromFirebase() {
        repository.getCurrentUserId()
            .subscribeByDefault()
            .subscribe(
                { userId ->
                    viewState.setCurrentUserId(userId)
                },
                {
                    Log.d(TAG, "Failed to get current user id from Firebase")
                }
            ).disposeBy(bag)
    }

    //TODO: Add status of sending message with Diffutil
    fun performSendMessages(message: String, fromId: String, toId: String) {
        if (message.isEmpty()) return
        repository.saveMessageToFireBase(message, fromId, toId)
            .subscribeByDefault()
            .subscribe(
                {

                },
                {
                    Log.d(TAG, "Failed to save message from id to the Firebase")
                }
            ).disposeBy(bag)
    }

    fun loadAllMessagesFromFirebase(fromId: String, toId: String) {
        repository.getAllMessages(fromId, toId)
            .subscribeByDefault()
            .subscribe(
                { data ->
                    viewState.setAdapterData(data)
                    resetUnreadMessagesWithFirebase(toId, fromId)
                    listenForNewMessagesFromFirebase(fromId, toId, data.size.toLong())
                },
                {
                    Log.d(TAG, "Failed to load all messages from Firebase")
                }
            ).disposeBy(bag)
    }

    fun resetUnreadMessagesWithFirebase(toId: String, fromId: String) {
        repository.resetUnreadMessages(toId, fromId)
            .subscribeByDefault()
            .subscribe()
    }

    private fun listenForNewMessagesFromFirebase(fromId: String, toId: String, skipCount: Long) {
        repository.listenForNewMessages(fromId, toId)
            .subscribeByDefault()
            //Skip already uploaded messages
            .skip(skipCount)
            .subscribe(
                {
                    viewState.addMessage(it)
                },
                {
                    Log.d(TAG, "Failed to load new message from Firebase")
                }
            ).disposeBy(bag)
    }
}
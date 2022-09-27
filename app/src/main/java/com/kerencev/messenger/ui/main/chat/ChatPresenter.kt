package com.kerencev.messenger.ui.main.chat

import android.content.Context
import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.FirebaseMessagesRepository
import com.kerencev.messenger.model.repository.WallpapersRepository
import com.kerencev.messenger.navigation.main.WallpapersScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.*

private const val TAG = "ChatPresenter"

class ChatPresenter(
    private val repository: FirebaseMessagesRepository,
    private val wallPaperRepository: WallpapersRepository,
    private val router: Router
) : BasePresenter<ChatView>(
    router
) {

    private var chatPartnerWasOnline: Long = -1

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.loadUserAvatar()
    }

    //TODO: Add status of sending message with DiffUtil
    fun performSendMessages(message: String, user: User, chatPartner: User) {
        if (message.isEmpty()) return
        chatPartner.wasOnline = chatPartnerWasOnline
        repository.saveMessageForAllNodes(message, user, chatPartner)
            .subscribeByDefault()
            .subscribe(
                { statusOfSending ->
                    when (statusOfSending) {
                        is StatusOfSendingMessage.Status1 -> {
                        }
                        is StatusOfSendingMessage.Status2 -> {
                        }
                        is StatusOfSendingMessage.Status3 -> {
                        }
                        is StatusOfSendingMessage.Status4 -> {
                        }
                    }
                },
                {
                    Log.d(TAG, "Failed to save message for all nodes")
                }
            )
        //Don't dispose because user can close the fragment but message needs to be delivered
    }

    fun updateChatPartnerStatus(chatPartner: User) {
        repository.updateChatPartnerStatus(chatPartner.uid)
            .subscribeByDefault()
            .subscribe { time ->
                chatPartnerWasOnline = time
                viewState.setToolbarStatus(MyDate.getChatPartnerStatus(time))
            }.disposeBy(bag)
    }

    fun loadAllMessagesFromFirebase(fromId: String, toId: String) {
        repository.getAllMessages(fromId, toId)
            .subscribeByDefault()
            .subscribe(
                { result ->
                    val data = result.first
                    val dateCounts = result.second
                    val skipCount = (data.size - dateCounts).toLong()
                    viewState.setAdapterData(data)
                    if (data.isNotEmpty()) {
                        resetUnreadMessagesWithFirebase(toId, fromId)
                    }
                    listenForNewMessagesFromFirebase(fromId, toId, skipCount)
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
            .disposeBy(bag)
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

    fun clearDisposableBag() {
        bag.clear()
    }

    fun getCurrentWallpaper(context: Context) {
        val wallpaper = wallPaperRepository.getWallpaper(context)
        viewState.setCurrentWallpaper(wallpaper)
    }

    fun navigateToWallpaperFragment() {
        router.navigateTo(WallpapersScreen)
    }

    fun updateUserTypingStatusWithFirebase(chatPartnerId: String, userId: String, isTyping: Boolean) {
        repository.updateUserTypingStatus(chatPartnerId, userId, isTyping)
            .subscribeByDefault()
            .subscribe()
            .disposeBy(bag)
    }

    fun listenForChatPartnerIsTyping(userId: String, chatPartnerId: String) {
        repository.listenForChatPartnerIsTyping(userId, chatPartnerId)
            .subscribeByDefault()
            .subscribe{ isTyping ->
                viewState.setChatPartnerIsTyping(isTyping)
            }
            .disposeBy(bag)
    }
}
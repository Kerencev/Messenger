package com.kerencev.messenger.ui.main.chat

import android.content.Context
import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.FirebaseMessagesRepository
import com.kerencev.messenger.model.repository.WallpapersRepository
import com.kerencev.messenger.navigation.main.WallpapersScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.MyDate
import com.kerencev.messenger.utils.StatusOfSendingMessage
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault

private const val TAG = "ChatPresenter"

class ChatPresenter(
    private val repository: FirebaseMessagesRepository,
    private val wallPaperRepository: WallpapersRepository,
    private val router: Router
) : BasePresenter<ChatView>(
    router
) {

    private var partnerWasOnline: Long = -1

    //TODO: Add status of sending message with DiffUtil
    fun performSendMessages(message: String, chatPartner: User) {
        if (message.isEmpty()) return
        chatPartner.wasOnline = partnerWasOnline
        repository.getCurrentUser()
            .subscribeByDefault()
            .subscribe(
                { currentUser ->
                    repository.saveMessageForAllNodes(message, currentUser, chatPartner)
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
                                        repository.sendPushToChatPartner(
                                            message,
                                            currentUser,
                                            chatPartner
                                        )
                                            .subscribeByDefault()
                                            .subscribe()
                                    }
                                }
                            },
                            {
                                Log.d(TAG, "Failed to save message for all nodes")
                            }
                        )
                },
                {
                    Log.d(TAG, it.message.toString())
                }
            )
        //Don't dispose because user can close the fragment but message needs to be delivered
    }

    fun updateChatPartnerInfo(chatPartner: User) {
        repository.updateChatPartnerInfo(chatPartner.uid)
            .subscribeByDefault()
            .subscribe { partner ->
                partnerWasOnline = partner.wasOnline
                viewState.updateChatPartnerStatus(status = MyDate.getChatPartnerStatus(partner.wasOnline))
                viewState.updateChatPartnerLogin(login = partner.login)
                viewState.updateChatPartnerAvatar(avatarUrl = partner.avatarUrl)
            }.disposeBy(bag)
    }

    fun loadAllMessagesFromFirebase(toId: String) {
        repository.getAllMessages(toId)
            .subscribeByDefault()
            .subscribe(
                { result ->
                    val data = result.first
                    val dateCounts = result.second
                    val skipCount = (data.size - dateCounts).toLong()
                    viewState.setAdapterData(data)
                    if (data.isNotEmpty()) {
                        resetUnreadMessagesWithFirebase(toId)
                    }
                    listenForNewMessagesFromFirebase(toId, skipCount)
                },
                {
                    Log.d(TAG, "Failed to load all messages from Firebase")
                }
            ).disposeBy(bag)
    }

    fun resetUnreadMessagesWithFirebase(toId: String) {
        repository.resetUnreadMessages(toId)
            .subscribeByDefault()
            .subscribe()
            .disposeBy(bag)
    }

    private fun listenForNewMessagesFromFirebase(toId: String, skipCount: Long) {
        repository.listenForNewMessages(toId)
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

    fun updateUserTypingStatusWithFirebase(chatPartnerId: String, isTyping: Boolean) {
        repository.updateUserTypingStatus(chatPartnerId, isTyping)
            .subscribeByDefault()
            .subscribe()
            .disposeBy(bag)
    }

    fun listenForChatPartnerIsTyping(chatPartnerId: String) {
        repository.listenForChatPartnerIsTyping(chatPartnerId)
            .subscribeByDefault()
            .subscribe { isTyping ->
                viewState.setChatPartnerIsTyping(isTyping)
            }
            .disposeBy(bag)
    }
}
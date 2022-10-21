package com.kerencev.messenger.ui.main.chat

import android.content.Context
import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.MessagesRepository
import com.kerencev.messenger.model.repository.WallpapersRepository
import com.kerencev.messenger.navigation.main.WallpapersScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.*
import io.reactivex.rxjava3.core.Completable

private const val TAG = "ChatPresenter"

class ChatPresenter(
    private val repository: MessagesRepository,
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
            .flatMap { currentUser ->
                repository.getCountOfUnreadMessages(currentUser.uid, chatPartner.uid)
                    .flatMap { countOfUnread ->
                        Completable.concat(
                            listOf(
                                repository.saveMessageToUserMessagesNode(
                                    message = message,
                                    currentUser = currentUser,
                                    chatPartner = chatPartner
                                ),
                                repository.saveMessageToPartnerMessagesNode(
                                    message = message,
                                    currentUser = currentUser,
                                    chatPartner = chatPartner
                                ),
                                repository.saveMessageToUserLatestMessagesNode(
                                    message = message,
                                    currentUser = currentUser,
                                    chatPartner = chatPartner
                                ),
                                repository.saveMessageToPartnerLatestMessagesNode(
                                    message = message,
                                    currentUser = currentUser,
                                    chatPartner = chatPartner,
                                    countOfUnread = countOfUnread + 1
                                )
                            )
                        ).toSingle {
                            Triple(
                                first = countOfUnread + 1,
                                second = currentUser,
                                third = chatPartner
                            )
                        }
                    }
            }
            .subscribe(
                {
                    sendPushNotificationToChatPartner(
                        countOfUnread = it.first,
                        currentUser = it.second,
                        chatPartner = it.third
                    )
                },
                {
                    log(it.stackTraceToString())
                }
            ).disposeBy(bag)
    }

    private fun sendPushNotificationToChatPartner(
        countOfUnread: Long,
        currentUser: User,
        chatPartner: User
    ) {
        repository.getTokenById(chatPartner.uid)
            .flatMap { recipientToken ->
                repository.getUnreadMessages(
                    currentUser = currentUser,
                    chatPartner = chatPartner,
                    countOfUnread = countOfUnread
                )
                    .map { listOfUnreadMessages ->
                        PushMapper.mapToPushNotification(
                            listOfMessages = listOfUnreadMessages,
                            currentUser = currentUser,
                            partnerToken = recipientToken
                        )
                    }
                    .flatMap { pushNotification ->
                        repository.sendPushToChatPartner(pushNotification).toSingle {}
                    }
            }
            .subscribeByDefault()
            .subscribe(
                {

                },
                {
                    log(it.stackTraceToString())
                }
            ).disposeBy(bag)
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
package com.kerencev.messenger.ui.main.chat

import android.content.Context
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.MediaStoreRepository
import com.kerencev.messenger.model.repository.MessagesRepository
import com.kerencev.messenger.model.repository.WallpapersRepository
import com.kerencev.messenger.navigation.main.WallpapersScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.*
import com.kerencev.messenger.utils.player.Player
import com.kerencev.messenger.utils.record.Recorder
import com.kerencev.messenger.utils.vibration.Vibration
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import javax.inject.Inject


class ChatPresenter : BasePresenter<ChatView>() {

    @Inject
    lateinit var messagesRepository: MessagesRepository

    @Inject
    lateinit var mediaStoreRepository: MediaStoreRepository

    @Inject
    lateinit var wallPaperRepository: WallpapersRepository

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var recorder: Recorder

    @Inject
    lateinit var player: Player

    private var partnerWasOnline: Long = -1

    //TODO: Add status of sending message with DiffUtil
    fun performSendMessages(message: String, chatPartner: User) {
        if (message.isEmpty()) return
        chatPartner.wasOnline = partnerWasOnline
        messagesRepository.getCurrentUser()
            .flatMap { currentUser ->
                messagesRepository.getCountOfUnreadMessages(currentUser.uid, chatPartner.uid)
                    .flatMap { countOfUnread ->
                        Completable.concat(
                            listOf(
                                messagesRepository.saveMessageToUserMessagesNode(
                                    message = message,
                                    currentUser = currentUser,
                                    chatPartner = chatPartner
                                ),
                                messagesRepository.saveMessageToPartnerMessagesNode(
                                    message = message,
                                    currentUser = currentUser,
                                    chatPartner = chatPartner
                                ),
                                messagesRepository.saveMessageToUserLatestMessagesNode(
                                    message = message,
                                    currentUser = currentUser,
                                    chatPartner = chatPartner
                                ),
                                messagesRepository.saveMessageToPartnerLatestMessagesNode(
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
        messagesRepository.getTokenById(chatPartner.uid)
            .flatMap { recipientToken ->
                messagesRepository.getUnreadMessages(
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
                        messagesRepository.sendPushToChatPartner(pushNotification).toSingle {}
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
        messagesRepository.updateChatPartnerInfo(chatPartner.uid)
            .subscribeByDefault()
            .subscribe(
                { partner ->
                    partnerWasOnline = partner.wasOnline
                    viewState.updateChatPartnerStatus(status = MyDate.getChatPartnerStatus(partner.wasOnline))
                    viewState.updateChatPartnerLogin(login = partner.login)
                    viewState.updateChatPartnerAvatar(avatarUrl = partner.avatarUrl)
                },
                {
                    log(it.stackTraceToString())
                }
            ).disposeBy(bag)
    }

    fun loadAllMessagesFromFirebase(toId: String) {
        messagesRepository.getAllMessages(toId)
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
                    log(it.stackTraceToString())
                }
            ).disposeBy(bag)
    }

    fun resetUnreadMessagesWithFirebase(toId: String) {
        messagesRepository.resetUnreadMessages(toId)
            .subscribeByDefault()
            .doOnError {
                log(it.stackTraceToString())
            }
            .subscribe()
            .disposeBy(bag)
    }

    private fun listenForNewMessagesFromFirebase(toId: String, skipCount: Long) {
        messagesRepository.listenForNewMessages(toId)
            .subscribeByDefault()
            //Skip already uploaded messages
            .skip(skipCount)
            .subscribe(
                {
                    viewState.addMessage(it)
                },
                {
                    log(it.stackTraceToString())
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
        messagesRepository.updateUserTypingStatus(chatPartnerId, isTyping)
            .subscribeByDefault()
            .subscribe()
            .disposeBy(bag)
    }

    fun listenForChatPartnerIsTyping(chatPartnerId: String) {
        messagesRepository.listenForChatPartnerIsTyping(chatPartnerId)
            .subscribeByDefault()
            .subscribe { isTyping ->
                viewState.setChatPartnerIsTyping(isTyping)
            }.disposeBy(bag)
    }

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }

    fun uploadFileToFirebase(file: File) {
        mediaStoreRepository.saveFileToFirebaseStorage(file)
            .subscribeByDefault()
            .subscribe { fileUrl ->

            }.disposeBy(bag)
    }

    fun startVoiceRecord() {
        recorder.startRecord()
            .subscribeByDefault()
            .doOnError {
                viewState.showVoiceRecordInfo()
            }
            .subscribe()
            .disposeBy(bag)
    }

    fun saveVoiceRecord() {
        recorder.stopRecord()
            .subscribeByDefault()
            .subscribe(
                { file ->
//                    uploadFileToFirebase(file = file)
                    player.play(file)
                        .subscribeByDefault()
                        .subscribe()
                },
                {
                    viewState.showVoiceRecordInfo()
                }
            ).disposeBy(bag)
    }

    fun deleteVoiceRecord() {
        recorder.deleteRecord()
            .subscribeByDefault()
            .doOnError {
                viewState.showVoiceRecordInfo()
            }
            .subscribe()
            .disposeBy(bag)
    }
}
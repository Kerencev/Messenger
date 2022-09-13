package com.kerencev.messenger.ui.main.chat

import android.content.Context
import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.FirebaseMessagesRepository
import com.kerencev.messenger.model.repository.WallpapersRepository
import com.kerencev.messenger.navigation.main.WallpapersScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.ChatMessageMapper
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

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.loadUserAvatar()
    }

    //TODO: Add status of sending message with Diffutil
    fun performSendMessages(message: String, user: User, chatPartner: User) {
        if (message.isEmpty()) return
        val chatMessage = ChatMessageMapper.mapToChatMessage(message, user, chatPartner)
        repository.saveMessageFromId(chatMessage)
            .subscribeByDefault()
            .subscribe(
                {
                    Log.d(TAG, "Save message from id")
                    repository.saveMessageToId(chatMessage)
                        .subscribeByDefault()
                        .subscribe(
                            {
                                Log.d(TAG, "Save message to id")
                                repository.saveLatestMessageFromId(chatMessage)
                                    .subscribeByDefault()
                                    .subscribe(
                                        { chatMessage ->
                                            Log.d(TAG, "Save latest message from id")
                                            repository.getCountOfUnreadMessages(
                                                chatMessage.toId,
                                                chatMessage.fromId
                                            )
                                                .subscribeByDefault()
                                                .subscribe(
                                                    { countOfUnread ->
                                                        Log.d(TAG, "Count of unread is: $countOfUnread")
                                                        val chatMessageForChatPartner =
                                                            ChatMessageMapper
                                                                .mapToLatestMessageForChatPartner(
                                                                    countOfUnread + 1,
                                                                    chatMessage, user
                                                                )
                                                        repository.saveLatestMessageToId(
                                                            chatMessageForChatPartner
                                                        )
                                                            .subscribeByDefault()
                                                            .subscribe(
                                                                {
                                                                    Log.d(TAG, "Save latest message to id")
                                                                },
                                                                {

                                                                }
                                                            ).disposeBy(bag)
                                                    },
                                                    {

                                                    }
                                                ).disposeBy(bag)
                                        },
                                        {

                                        }
                                    ).disposeBy(bag)
                            },
                            {

                            }
                        ).disposeBy(bag)

                },
                {

                }
            ).disposeBy(bag)
    }

    fun loadAllMessagesFromFirebase(fromId: String, toId: String) {
        repository.getAllMessages(fromId, toId)
            .subscribeByDefault()
            .subscribe(
                { data ->
                    viewState.setAdapterData(data)
                    if (data.isNotEmpty()) {
                        resetUnreadMessagesWithFirebase(toId, fromId)
                    }
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

    fun getCurrentWallpaper(context: Context) {
        val wallpaper = wallPaperRepository.getWallpaper(context)
        viewState.setCurrentWallpaper(wallpaper)
    }

    fun navigateToWallpaperFragment() {
        router.navigateTo(WallpapersScreen)
    }
}
package com.kerencev.messenger.ui.main.chatlist

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.LatestMessagesRepository
import com.kerencev.messenger.navigation.main.ChatScreen
import com.kerencev.messenger.navigation.main.NewMessageScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.SortChatListData
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.log
import com.kerencev.messenger.utils.subscribeByDefault
import javax.inject.Inject

class ChatListPresenter() : BasePresenter<ChatListView>() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var repository: LatestMessagesRepository

    private val sortChatList = SortChatListData()

    fun listenForLatestMessagesFromFireBase() {
        repository.listenForLatestMessages()
            .subscribeByDefault()
            .subscribe(
                {
                    val sortedData = sortChatList.getSortedData(it)
                    viewState.updateAdapterData(sortedData)
                },
                {
                    log(it.stackTraceToString())
                }
            ).disposeBy(bag)
    }

    fun updateAllLatestMessages() {
        repository.updateAllLatestMessages()
            .subscribeByDefault()
            .subscribe(
                {
                    val sortedData = sortChatList.getSortedData(it)
                    viewState.updateAdapterData(sortedData)
                },
                { throwable ->
                    throwable.message?.let { Log.d(TAG, it) }
                }
            ).disposeBy(bag)
    }

    fun navigateToNewMessageFragment() {
        router.navigateTo(NewMessageScreen)
    }

    fun navigateToChatFragment(user: User) {
        router.navigateTo(ChatScreen(user))
    }

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }

    companion object {
        private const val TAG = "ChatListPresenter"
    }
}
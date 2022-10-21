package com.kerencev.messenger.ui.main.chatlist

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.LatestMessagesRepository
import com.kerencev.messenger.navigation.main.ChatScreen
import com.kerencev.messenger.navigation.main.NewMessageScreen
import com.kerencev.messenger.utils.SortChatListData
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter

class ChatListPresenter(
    private val router: Router,
    private val repository: LatestMessagesRepository
) : MvpPresenter<ChatListView>() {

    private val bag = CompositeDisposable()
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
                    Log.d(TAG, "Failed to listen for latest messages from FireBase")
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

    override fun onDestroy() {
        bag.dispose()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ChatListPresenter"
    }
}
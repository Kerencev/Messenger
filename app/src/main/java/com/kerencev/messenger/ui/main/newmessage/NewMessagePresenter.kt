package com.kerencev.messenger.ui.main.newmessage

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.UsersRepository
import com.kerencev.messenger.navigation.main.ChatScreen
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter

class NewMessagePresenter(
    private val router: Router,
    private val repository: UsersRepository
) : MvpPresenter<NewMessageView>() {

    private val bag = CompositeDisposable()

    fun getAllUsersWithFirestore() {
        repository.getAllUsers()
            .subscribeByDefault()
            .subscribe(
                {
                    viewState.initList(it)
                },
                {
                    Log.d(TAG, "Failed to get all users from Firebase")
                }
            ).disposeBy(bag)
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
        private const val TAG = "NewMessagePresenter"
    }
}
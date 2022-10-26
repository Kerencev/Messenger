package com.kerencev.messenger.ui.main.newmessage

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.UsersRepository
import com.kerencev.messenger.navigation.main.ChatScreen
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault
import javax.inject.Inject

class NewMessagePresenter() : BasePresenter<NewMessageView>() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var repository: UsersRepository

    fun getAllUsersWithFirestore() {
        repository.getAllUsers()
            .subscribeByDefault()
            .subscribe { usersList ->
                viewState.initList(usersList)
            }.disposeBy(bag)
    }

    fun navigateToChatFragment(user: User) {
        router.navigateTo(ChatScreen(user))
    }

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }
}
package com.kerencev.messenger.ui.main.newmessage

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.FirebaseRepository
import moxy.MvpPresenter

class NewMessagePresenter(
    private val router: Router,
    private val repository: FirebaseRepository
) : MvpPresenter<NewMessageView>() {

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }
}
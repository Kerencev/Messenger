package com.kerencev.messenger.presenters.main

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.navigation.main.ChatListScreen
import com.kerencev.messenger.ui.main.MainView
import moxy.MvpPresenter

class MainPresenter(
    private val router: Router
) : MvpPresenter<MainView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        router.replaceScreen(ChatListScreen)
    }

    fun onBackPressed() {
        router.exit()
    }
}
package com.kerencev.messenger.ui.main.newmessage

import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.databinding.FragmentNewMessageBinding
import com.kerencev.messenger.model.FirebaseRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import moxy.ktx.moxyPresenter

class NewMessageFragment :
    ViewBindingFragment<FragmentNewMessageBinding>(FragmentNewMessageBinding::inflate),
    NewMessageView, OnBackPressedListener {

    private val presenter: NewMessagePresenter by moxyPresenter {
        NewMessagePresenter(MessengerApp.instance.router, FirebaseRepositoryImpl())
    }

    override fun onBackPressed() = presenter.onBackPressed()
}
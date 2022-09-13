package com.kerencev.messenger.ui.main.newmessage

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.databinding.FragmentNewMessageBinding
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.FirebaseRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import moxy.ktx.moxyPresenter

class NewMessageFragment :
    ViewBindingFragment<FragmentNewMessageBinding>(FragmentNewMessageBinding::inflate),
    NewMessageView, OnBackPressedListener {

    private val presenter: NewMessagePresenter by moxyPresenter {
        NewMessagePresenter(MessengerApp.instance.router, FirebaseRepositoryImpl())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.getAllUsersWithFirestore()
        with(binding) {
            newMessageToolbar.setNavigationOnClickListener {
                presenter.onBackPressed()
            }
        }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun initList(listOfUsers: List<User>) = with(binding) {
        val adapter = UsersListAdapter(object : OnUserClickListener {
            override fun onItemClick(user: User) {
                presenter.navigateToChatFragment(user)
            }
        })
        newMessageUsersRv.adapter = adapter
        adapter.setData(listOfUsers)
    }

    companion object {
        private const val TAG = "NewMessageFragment"
    }
}
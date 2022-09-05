package com.kerencev.messenger.ui.main.chatlist

import android.os.Bundle
import android.view.View
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.databinding.FragmentChatListBinding
import com.kerencev.messenger.model.FirebaseRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.main.activity.MainView
import moxy.ktx.moxyPresenter

class ChatListFragment :
    ViewBindingFragment<FragmentChatListBinding>(FragmentChatListBinding::inflate),
    ChatListView,
    OnBackPressedListener {

    private var mainActivity: MainView? = null

    private val presenter: ChatListPresenter by moxyPresenter {
        ChatListPresenter(MessengerApp.instance.router, FirebaseRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = (activity as? MainView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
//            The solution is to make the icons visible
            chatListBottomNavigation.itemIconTintList = null;
            btnSignOut.setOnClickListener {
                presenter.signOutWithFirebaseAuth()
            }
            chatListFabNewMessage.setOnClickListener {
                presenter.navigateToNewMessageFragment()
            }
            chatListToolbar.setNavigationOnClickListener {
                presenter.onBackPressed()
            }
        }
    }

    override fun startLoginActivity() {
        mainActivity?.startLoginActivity()
    }

    override fun onBackPressed() = presenter.onBackPressed()
}
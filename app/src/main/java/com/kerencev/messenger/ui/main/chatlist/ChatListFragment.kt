package com.kerencev.messenger.ui.main.chatlist

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.databinding.FragmentChatListBinding
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.FirebaseAuthRepositoryImpl
import com.kerencev.messenger.model.repository.impl.FirebaseMessagesRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.main.activity.MainView
import com.kerencev.messenger.ui.main.chatlist.recycler.ChatListAdapter
import com.kerencev.messenger.ui.main.chatlist.recycler.OnItemClick
import moxy.ktx.moxyPresenter

//TODO: Add Room Cache
class ChatListFragment :
    ViewBindingFragment<FragmentChatListBinding>(FragmentChatListBinding::inflate),
    ChatListView,
    OnBackPressedListener {

    private var mainActivity: MainView? = null
    private val presenter: ChatListPresenter by moxyPresenter {
        ChatListPresenter(
            MessengerApp.instance.router,
            FirebaseAuthRepositoryImpl(),
            FirebaseMessagesRepositoryImpl()
        )
    }
    private val adapter = ChatListAdapter(object : OnItemClick {
        override fun onClick(user: User) {
            presenter.navigateToChatFragment(user)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = (activity as? MainView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity?.setToolbar(binding.chatListToolbar)
        setToolbarOptionsItemClick()
        with(binding) {
            //The solution is to make the icons visible
            chatListBottomNavigation.itemIconTintList = null
            chatListFabNewMessage.setOnClickListener {
                presenter.navigateToNewMessageFragment()
            }
            chatListRv.adapter = adapter
        }
        presenter.listenForLatestMessagesFromFireBase()
        presenter.updateAllLatestMessages()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    override fun onPause() {
        super.onPause()
        showStatusBar()
    }

    private fun setToolbarOptionsItemClick() {
        binding.chatListToolbar.setOnMenuItemClickListener(
            object : Toolbar.OnMenuItemClickListener,
                androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {

                    }
                    return true
                }
            })
    }

    override fun updateAdapterData(data: List<ChatMessage>) {
        adapter.setListDataForDiffUtil(data)
    }

    override fun showStatusBar() {
        mainActivity?.showStatusBar()
    }

    override fun hideStatusBar() {
        mainActivity?.hideStatusBar()
    }

    override fun onBackPressed() = presenter.onBackPressed()
}
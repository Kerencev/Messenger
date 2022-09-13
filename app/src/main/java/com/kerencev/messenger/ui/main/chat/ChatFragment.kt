package com.kerencev.messenger.ui.main.chat

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentChatBinding
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.*
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import moxy.ktx.moxyPresenter

class ChatFragment : ViewBindingFragment<FragmentChatBinding>(FragmentChatBinding::inflate),
    ChatView, OnBackPressedListener {

    private val presenter: ChatPresenter by moxyPresenter {
        ChatPresenter(
            FirebaseRepositoryImpl(),
            WallpapersRepositoryImpl(),
            MessengerApp.instance.router
        )
    }
    private val adapter = ChatAdapter()
    private var toUser: User? = null
    private var fromUSerId: String? = null
    private var toUserId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toUser = arguments?.getParcelable(BUNDLE_KEY_USER)
        setToolbarClicks()
        with(binding) {
            toUser?.let {
                chatToolbar.title = it.login
                toUserId = it.uid
            }
            chatRv.adapter = adapter
            chatFabSend.setOnClickListener { _ ->
                fromUSerId?.let { fromId ->
                    val text = chatEditText.text.toString()
                    toUserId?.let { presenter.performSendMessages(text, fromId, it) }
                    chatEditText.text?.clear()
                }
            }
        }
    }

    private fun setToolbarClicks() = with(binding) {
        chatToolbar.setNavigationOnClickListener {
            presenter.onBackPressed()
        }
        chatToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.actionWallpaper -> {
                    presenter.navigateToWallpaperFragment()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.getCurrentWallpaper(requireContext())
    }

    override fun setCurrentWallpaper(wallpaper: String) {
        when (wallpaper) {
            WALLPAPERS_ONE -> requireActivity().window?.setBackgroundDrawableResource(R.drawable.background_1)
            WALLPAPERS_TWO -> requireActivity().window?.setBackgroundDrawableResource(R.drawable.background_2)
            WALLPAPERS_THREE -> requireActivity().window?.setBackgroundDrawableResource(R.drawable.background_3)
        }
    }

    override fun setCurrentUserId(userId: String) {
        adapter.setCurrentUserId(userId)
        fromUSerId = userId
        fromUSerId?.let { from ->
            toUserId?.let { to ->
                presenter.loadAllMessagesFromFirebase(from, to)
            }
        }
    }

    override fun loadUserAvatar() {
        toUser?.avatarUrl?.let {
            Glide.with(requireContext()).load(toUser?.avatarUrl)
                .placeholder(R.drawable.ic_user_place_holder)
                .into(binding.ivAvatar)
        }
    }

    override fun addMessage(chatMessage: ChatMessage) {
        adapter.insertItem(chatMessage)
        binding.chatRv.scrollToPosition(adapter.itemCount - 1)
    }

    override fun setAdapterData(data: List<ChatMessage>) {
        adapter.setData(data)
        binding.chatRv.scrollToPosition(adapter.itemCount - 1)
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun onDestroyView() {
        //TODO Come up with a good way to reset unread messages when we leave the chat or close applications
        if (adapter.itemCount > 0) {
            toUserId?.let {
                fromUSerId?.let {
                    presenter.resetUnreadMessagesWithFirebase(toUserId!!, fromUSerId!!)
                }
            }
        }
        _binding = null
        requireActivity().window?.setBackgroundDrawableResource(R.color.white)
        super.onDestroyView()
    }

    companion object {
        private const val BUNDLE_KEY_USER = "BUNDLE_KEY_USER"
        fun newInstance(user: User): ChatFragment {
            return ChatFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(BUNDLE_KEY_USER, user)
                }
            }
        }
    }
}
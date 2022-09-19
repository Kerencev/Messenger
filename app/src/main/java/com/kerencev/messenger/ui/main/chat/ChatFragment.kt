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
            FirebaseMessagesRepositoryImpl(),
            WallpapersRepositoryImpl(),
            MessengerApp.instance.router
        )
    }
    private val user = MessengerApp.instance.user
    private val adapter = ChatAdapter(userId = user.uid)
    private var chatPartner: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatPartner = arguments?.getParcelable(BUNDLE_KEY_USER)
        chatPartner?.let { presenter.loadAllMessagesFromFirebase(user.uid, it.uid) }
        setToolbarClicks()
        with(binding) {
            chatPartner?.let {
                chatToolbar.title = it.login
            }
            chatRv.adapter = adapter
            chatFabSend.setOnClickListener { _ ->
                val text = chatEditText.text.toString()
                chatPartner?.let {
                    presenter.performSendMessages(
                        message = text,
                        user = user,
                        chatPartner = it
                    )
                }
                chatEditText.text?.clear()
            }
        }
    }

    private fun setToolbarClicks() = with(binding) {
        chatToolbar.setNavigationOnClickListener {
            presenter.onBackPressed()
        }
        chatToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
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

    override fun loadUserAvatar() {
        chatPartner?.avatarUrl?.let {
            Glide.with(requireContext()).load(chatPartner?.avatarUrl)
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
            chatPartner?.let {
                presenter.resetUnreadMessagesWithFirebase(it.uid, user.uid)
            }
        }
        _binding = null
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
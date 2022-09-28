package com.kerencev.messenger.ui.main.chat

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding.widget.RxTextView
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentChatBinding
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.*
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.vanniktech.emoji.EmojiPopup
import moxy.ktx.moxyPresenter
import java.util.concurrent.TimeUnit

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
    private var isSmileIcon = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpEmoji()
        chatPartner = arguments?.getParcelable(BUNDLE_KEY_USER)
        chatPartner?.let {
            presenter.loadAllMessagesFromFirebase(user.uid, it.uid)
            presenter.updateChatPartnerStatus(it)
            binding.chatEditText.addTextChangedListener {
                presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, user.uid, true)
            }
            RxTextView.textChanges(binding.chatEditText)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .subscribe {
                    presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, user.uid, false)
                }
            presenter.listenForChatPartnerIsTyping(user.uid, chatPartner!!.uid)
        }
        setToolbarClicks()
        scrollRvWhenShowKeyboard()
        with(binding) {
            chatPartner?.let {
                chatToolbarTitle.text = it.login
            }
            chatRv.adapter = adapter
            chatCardSend.setOnClickListener { _ ->
                val text = chatEditText.text.toString()
                chatPartner?.let {
                    presenter.performSendMessages(
                        message = text,
                        user = user,
                        chatPartner = it
                    )
                    presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, user.uid, false)
                }
                chatEditText.text?.clear()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.getCurrentWallpaper(requireContext())
    }

    override fun onStop() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.chatEditText.windowToken, 0)
        presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, user.uid, false)
        presenter.clearDisposableBag()
        super.onStop()
    }

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

    override fun setCurrentWallpaper(wallpaper: String) {
        when (wallpaper) {
            WALLPAPERS_ONE -> requireActivity().window?.setBackgroundDrawableResource(R.drawable.background_1)
            WALLPAPERS_TWO -> requireActivity().window?.setBackgroundDrawableResource(R.drawable.background_2)
            WALLPAPERS_THREE -> requireActivity().window?.setBackgroundDrawableResource(R.drawable.background_3)
        }
    }

    override fun setToolbarStatus(wasOnline: String) = with(binding) {
        chatToolbarSubtitle.text = wasOnline
    }

    override fun setChatPartnerIsTyping(isTyping: Boolean) = with(binding) {
        when (isTyping) {
            true -> {
                chatToolbarSubtitle.visibility = View.GONE
                chatToolbarTvTyping.visibility = View.VISIBLE
            }
            false -> {
                chatToolbarSubtitle.visibility = View.VISIBLE
                chatToolbarTvTyping.visibility = View.GONE
            }
        }
    }

    override fun loadUserAvatar() {
        with(binding) {
            when (chatPartner?.avatarUrl) {
                null -> {
                    tvChatLetter.visibility = View.VISIBLE
                    tvChatLetter.text = chatPartner?.login?.first().toString()
                }
                else -> {
                    ivAvatar.visibility = View.VISIBLE
                    Glide.with(requireContext()).load(chatPartner?.avatarUrl)
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(binding.ivAvatar)
                }
            }
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

    private fun setUpEmoji() = with(binding) {
        val popup = EmojiPopup(
            chatRoot,
            chatEditText
        )

        chatCardSmile.setOnClickListener {
            when (isSmileIcon) {
                true -> chatIvSmile.setImageResource(R.drawable.icon_keyboard)
                false -> chatIvSmile.setImageResource(R.drawable.icon_smile)
            }
            isSmileIcon = !isSmileIcon
            popup.toggle()
        }
    }

    private fun scrollRvWhenShowKeyboard() = with(binding) {
        chatRv.addOnLayoutChangeListener { _, _, _, _, newHeight, _, _, _, oldHeight ->
            if (newHeight < oldHeight) {
                chatRv.postDelayed(
                    { chatRv.scrollToPosition(adapter.itemCount - 1) },
                    10
                )
            } else {
                presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, user.uid, false)
            }
        }
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
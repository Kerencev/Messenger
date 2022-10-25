package com.kerencev.messenger.ui.main.chat

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding.widget.RxTextView
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.data.remote.RetrofitInstance
import com.kerencev.messenger.databinding.FragmentChatBinding
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.*
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.services.FirebaseService
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.vanniktech.emoji.EmojiPopup
import moxy.ktx.moxyPresenter
import java.util.concurrent.TimeUnit

class ChatFragment : ViewBindingFragment<FragmentChatBinding>(FragmentChatBinding::inflate),
    ChatView, OnBackPressedListener {

    private val presenter: ChatPresenter by moxyPresenter {
        ChatPresenter(
            MessagesRepositoryImpl(
                notificationAPI = RetrofitInstance.api
            ),
            WallpapersRepositoryImpl(),
            MessengerApp.instance.router
        )
    }

    private val adapter = ChatAdapter()

    private var chatPartner: User? = null

    private var isSmileIcon = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatPartner = arguments?.getParcelable(BUNDLE_KEY_USER)
        chatPartner?.let {
            with(binding) {
                chatToolbarTitle.text = it.login
                updateChatPartnerAvatar(chatPartner!!.avatarUrl)
                chatRv.adapter = adapter
                // Cancel notification from the chat partner
                FirebaseService.notifManager?.cancel(chatPartner!!.notificationId)
                presenter.loadAllMessagesFromFirebase(it.uid)
                presenter.updateChatPartnerInfo(it)
                presenter.listenForChatPartnerIsTyping(chatPartner!!.uid)
                chatEditText.addTextChangedListener {
                    presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, true)
                }
                RxTextView.textChanges(chatEditText)
                    .debounce(1000, TimeUnit.MILLISECONDS)
                    .subscribe {
                        presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, false)
                    }
                chatCardSend.setOnClickListener { _ ->
                    val text = chatEditText.text.toString()
                    presenter.performSendMessages(
                        message = text,
                        chatPartner = it
                    )
                    presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, false)
                    chatEditText.text?.clear()
                }
            }
        }
        setUpEmoji()
        setToolbarClicks()
        scrollRvWhenShowKeyboard()
    }

    override fun onResume() {
        presenter.getCurrentWallpaper(requireContext())
        MessengerApp.instance.chatPartner = chatPartner
        super.onResume()
    }

    override fun onPause() {
        MessengerApp.instance.chatPartner = null
        super.onPause()
    }

    override fun onStop() {
        requireActivity().window?.setBackgroundDrawableResource(R.color.white)
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.chatEditText.windowToken, 0)
        presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, false)
        presenter.clearDisposableBag()
        //TODO Come up with a good way to reset unread messages when we leave the chat or close applications
        if (adapter.itemCount > 0) {
            chatPartner?.let {
                presenter.resetUnreadMessagesWithFirebase(it.uid)
            }
        }
        super.onStop()
    }

    override fun setCurrentWallpaper(wallpaper: String) {
        when (wallpaper) {
            WALLPAPERS_ONE -> requireActivity().window?.setBackgroundDrawableResource(R.drawable.background_1)
            WALLPAPERS_TWO -> requireActivity().window?.setBackgroundDrawableResource(R.drawable.background_2)
            WALLPAPERS_THREE -> requireActivity().window?.setBackgroundDrawableResource(R.drawable.background_3)
        }
    }

    override fun updateChatPartnerStatus(status: String) = with(binding) {
        chatToolbarSubtitle.text = status
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

    override fun updateChatPartnerLogin(login: String) {
        binding.chatToolbarTitle.text = login
    }

    override fun updateChatPartnerAvatar(avatarUrl: String?) {
        with(binding) {
            when (avatarUrl) {
                null -> {
                    ivAvatar.visibility = View.GONE
                    tvChatLetter.visibility = View.VISIBLE
                    tvChatLetter.text = chatPartner?.login?.first().toString()
                }
                else -> {
                    tvChatLetter.visibility = View.GONE
                    ivAvatar.visibility = View.VISIBLE
                    Glide.with(requireContext()).load(avatarUrl)
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
                presenter.updateUserTypingStatusWithFirebase(chatPartner!!.uid, false)
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
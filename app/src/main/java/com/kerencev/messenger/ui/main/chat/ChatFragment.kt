package com.kerencev.messenger.ui.main.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.transition.Fade
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding.widget.RxTextView
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentChatBinding
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.WALLPAPERS_ONE
import com.kerencev.messenger.model.repository.impl.WALLPAPERS_THREE
import com.kerencev.messenger.model.repository.impl.WALLPAPERS_TWO
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.services.FirebaseService
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.utils.*
import com.kerencev.messenger.utils.vibration.AppVibrator
import com.kerencev.messenger.utils.vibration.Vibration
import com.vanniktech.emoji.EmojiPopup
import moxy.ktx.moxyPresenter
import java.util.concurrent.TimeUnit
import com.kerencev.messenger.utils.postDelayed as postDelayed

// If the user swipes to the left by less than -200 on the x axis, the voice message will be deleted
private const val SWIPE_LENGTH_TO_DELETE = -200

class ChatFragment : ViewBindingFragment<FragmentChatBinding>(FragmentChatBinding::inflate),
    ChatView, OnBackPressedListener {

    private val presenter: ChatPresenter by moxyPresenter {
        ChatPresenter().apply {
            app.appComponent.inject(
                this
            )
        }
    }
    private val adapter = ChatAdapter()
    private var chatPartner: User? = null
    private var isSmileIcon = true
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var timer: Stopwatch? = null
    private var recordTimer: CountDownTimer? = null
    private var vibration: Vibration? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatPartner = arguments?.getParcelable(BUNDLE_KEY_USER)
        initFields()
        with(binding) {
            chatRv.adapter = adapter
            chatToolbarTitle.text = chatPartner?.login
        }
        updateChatPartnerAvatar(chatPartner?.avatarUrl)
        cancelChatPartnerNotification(chatPartner?.notificationId)
        chatPartner?.let { partner ->
            presenter.loadAllMessagesFromFirebase(partner.uid)
            presenter.updateChatPartnerInfo(partner)
            presenter.listenForChatPartnerIsTyping(partner.uid)
            setTextChangeListeners(partner.uid)
            setBtnSendClickListener(partner)
            setBtnMicClickListener()
        }
        setUpEmoji()
        setToolbarClicks()
        scrollRvWhenShowKeyboard()
    }

    override fun onResume() {
        presenter.getCurrentWallpaper(requireContext())
        app.chatPartner = chatPartner
        super.onResume()
    }

    override fun onPause() {
        app.chatPartner = null
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

    override fun showVoiceRecordInfo() {
        with(binding) {
            root.finishAndAnimateWithDelayFade()
            tvVoiceRecordInfo.makeVisible()
            postDelayed(1000) {
                root.finishAndAnimateWithDelayFade()
                tvVoiceRecordInfo.makeGone()
            }
        }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun onDestroyView() {
        _binding = null
        presenter.deleteVoiceRecord()
        recordTimer?.cancel()
        recordTimer = null
        super.onDestroyView()
    }

    private fun initFields() {
        timer = Stopwatch()
        recordTimer = object : CountDownTimer(1000000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                TransitionManager.beginDelayedTransition(binding.root, Fade())
                binding.imgMicRed.reverseVisibility()
                binding.tvSecRecord.text = timer?.getTime()
                timer?.increment()
            }

            override fun onFinish() = Unit
        }
        vibration = AppVibrator(requireContext())
    }

    /**
     * Cancel notification from the chat partner
     */
    private fun cancelChatPartnerNotification(notificationId: Int?) {
        FirebaseService.notifManager?.cancel(notificationId ?: return)
    }

    private fun setTextChangeListeners(partnerId: String) {
        with(binding) {
            chatEditText.addTextChangedListener { editable ->
                if (editable.isNullOrEmpty()) {
                    chatCardMic.makeVisible()
                    chatCardSend.makeGone()
                } else {
                    chatCardMic.makeGone()
                    chatCardSend.makeVisible()
                }
                presenter.updateUserTypingStatusWithFirebase(partnerId, true)
            }
            RxTextView.textChanges(chatEditText)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .subscribe {
                    presenter.updateUserTypingStatusWithFirebase(partnerId, false)
                }
        }
    }

    private fun setBtnSendClickListener(partner: User) {
        with(binding) {
            chatCardSend.setOnClickListener { _ ->
                val text = chatEditText.text.toString()
                presenter.performSendMessages(
                    message = text,
                    chatPartner = partner
                )
                presenter.updateUserTypingStatusWithFirebase(partner.uid, false)
                chatEditText.text?.clear()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setBtnMicClickListener() {
        binding.chatCardMic.setOnTouchListener { _, event ->
            if (checkRecordPermission()) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        vibration?.vibrate(10)
                        startRecord()
                        recordTimer?.start()
                    }
                    MotionEvent.ACTION_UP -> {
                        vibration?.vibrate(10)
                        stopRecord(event.x >= SWIPE_LENGTH_TO_DELETE)
                        recordTimer?.cancel()
                        timer?.resetTimer()
                        binding.tvDeleteVoiceInfo.makeGone()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        with(binding) {
                            if (event.x <= SWIPE_LENGTH_TO_DELETE) {
                                imgMicRed.setImageResource(R.drawable.icon_trash_red)
                                tvDeleteVoiceInfo.makeVisible()
                            } else {
                                imgMicRed.setImageResource(R.drawable.icon_mic_red)
                                tvDeleteVoiceInfo.makeGone()
                            }
                        }
                    }
                }
            }
            true
        }
    }

    private fun startRecord() {
        with(binding) {
            root.finishAndAnimateWithDelayFade()
            linearSendAudio.makeVisible()
            linearSendText.makeGone()
            imgRecordBackground.makeVisible()
        }
        presenter.startVoiceRecord()
    }

    private fun stopRecord(isSaveRecord: Boolean) {
        with(binding) {
            root.finishAndAnimateWithDelayFade()
            linearSendAudio.makeGone()
            linearSendText.makeVisible()
            imgRecordBackground.makeGone()
            imgMicRed.setImageResource(R.drawable.icon_mic_red)
        }
        if (isSaveRecord) {
            presenter.saveVoiceRecord()
        } else {
            presenter.deleteVoiceRecord()
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

    private fun checkRecordPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            false
        } else {
            true
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
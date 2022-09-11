package com.kerencev.messenger.ui.main.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kerencev.messenger.databinding.ItemChatFromUserBinding
import com.kerencev.messenger.databinding.ItemChatToUserBinding
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.utils.MyDate

private const val FROM_USER_TYPE = 1
private const val TO_USER_TYPE = 2

//TODO: Add DiffUtil nad Room Cache
class ChatAdapter : RecyclerView.Adapter<ChatAdapter.BaseViewHolder>() {

    private val data = ArrayList<ChatMessage>()
    private var userId: String? = null

    fun setData(listOfMessages: List<ChatMessage>) {
        data.clear()
        data.addAll(listOfMessages)
        notifyDataSetChanged()
    }

    fun setCurrentUserId(id: String) {
        userId = id
    }

    fun insertItem(chatMessage: ChatMessage) {
        data.add(chatMessage)
        notifyItemInserted(data.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (userId == data[position].fromId) {
            FROM_USER_TYPE
        } else {
            TO_USER_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            FROM_USER_TYPE -> {
                val binding = ItemChatFromUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ChatFromUser(binding)
            }
            TO_USER_TYPE -> {
                val binding = ItemChatToUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ChatToUser(binding)
            }
            else -> {
                val binding = ItemChatToUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ChatToUser(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(chatMessage: ChatMessage)
    }

    inner class ChatFromUser(private val binding: ItemChatFromUserBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(chatMessage: ChatMessage) {
            with(binding) {
                tvChatFromUserMessage.text = chatMessage.message
                tvChatFromUserTime.text = MyDate.getTime(chatMessage.timesTamp)
            }
        }
    }

    inner class ChatToUser(private val binding: ItemChatToUserBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(chatMessage: ChatMessage) {
            with(binding) {
                tvChatToUserMessage.text = chatMessage.message
                tvChatToUserTime.text = MyDate.getTime(chatMessage.timesTamp)
            }
        }
    }
}
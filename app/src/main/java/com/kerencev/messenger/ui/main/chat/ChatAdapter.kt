package com.kerencev.messenger.ui.main.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kerencev.messenger.databinding.ItemChatDateBinding
import com.kerencev.messenger.databinding.ItemChatFromUserBinding
import com.kerencev.messenger.databinding.ItemChatToUserBinding
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.utils.MyDate

private const val DATE_TYPE = 0
private const val FROM_USER_TYPE = 1
private const val TO_USER_TYPE = 2

//TODO: Add DiffUtil and Room Cache
class ChatAdapter(private val userId: String) : RecyclerView.Adapter<ChatAdapter.BaseViewHolder>() {

    private val data = ArrayList<ChatMessage>()

    fun setData(listOfMessages: List<ChatMessage>) {
        data.clear()
        data.addAll(listOfMessages)
        notifyDataSetChanged()
    }

    fun insertItem(chatMessage: ChatMessage) {
        val currentDate = MyDate.getDate(System.currentTimeMillis())
        if (data.isEmpty()) {
            data.add(ChatMessage(message = currentDate))
            notifyItemInserted(data.size - 1)
            data.add(chatMessage)
            notifyItemInserted(data.size - 1)
            return
        }
        val lastDate = MyDate.getDate(data.last().timesTamp)
        if (currentDate != lastDate) {
            data.add(ChatMessage(message = currentDate))
            notifyItemInserted(data.size - 1)
        }
        data.add(chatMessage)
        notifyItemInserted(data.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        if (data[position].id.isEmpty()) {
            return DATE_TYPE
        }
        return if (userId == data[position].fromId) {
            FROM_USER_TYPE
        } else {
            TO_USER_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            DATE_TYPE -> {
                val binding = ItemChatDateBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DateViewHolder(binding)
            }
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

    inner class DateViewHolder(private val binding: ItemChatDateBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(chatMessage: ChatMessage) {
            binding.tvItemChatDate.text = chatMessage.message
        }
    }
}
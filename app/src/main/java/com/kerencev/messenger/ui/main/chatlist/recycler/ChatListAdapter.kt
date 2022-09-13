package com.kerencev.messenger.ui.main.chatlist.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.ItemChatListBinding
import com.kerencev.messenger.model.entities.ChatMessage
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.utils.MyDate
import com.squareup.picasso.Picasso

interface OnItemClick {
    fun onClick(user: User)
}

class ChatListAdapter(private val onItemClick: OnItemClick) :
    RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val data = ArrayList<ChatMessage>()

    fun setListDataForDiffUtil(lisDataNew: List<ChatMessage>) {
        val diff = DiffUtil.calculateDiff(DiffUtilCallback(data, lisDataNew))
        diff.dispatchUpdatesTo(this)
        data.clear()
        data.addAll(lisDataNew)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding =
            ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onBindViewHolder(
        holder: ChatViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val createCombinePayload = createCombinePayload(payloads as List<Change<ChatMessage>>)
            if (createCombinePayload.newData.message != createCombinePayload.oldData.message) {
                holder.itemView.findViewById<TextView>(R.id.itemChatListTvMessage).text =
                    createCombinePayload.newData.message
            }
            if (createCombinePayload.newData.timesTamp != createCombinePayload.oldData.timesTamp) {
                holder.itemView.findViewById<TextView>(R.id.itemChatListTvTime).text =
                    MyDate.getTime(createCombinePayload.newData.timesTamp)
            }
            if (createCombinePayload.newData.countOfUnread != createCombinePayload.oldData.countOfUnread) {
                holder.itemView.findViewById<TextView>(R.id.itemChatListTvNoticeCount).text =
                    createCombinePayload.newData.countOfUnread.toString()
                holder.itemView.findViewById<ImageView>(R.id.itemChatListIvNoticeOval).visibility =
                    if (createCombinePayload.newData.countOfUnread > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
            if (createCombinePayload.newData.chatPartnerLogin != createCombinePayload.oldData.chatPartnerLogin) {
                holder.itemView.findViewById<TextView>(R.id.itemChatListTvLogin).text =
                    createCombinePayload.newData.chatPartnerLogin
            }
        }
    }

    override fun getItemCount() = data.size

    inner class ChatViewHolder(private val binding: ItemChatListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            with(binding) {
                itemChatListTvLogin.text = message.chatPartnerLogin
                itemChatListTvMessage.text = message.message
                itemChatListTvTime.text = MyDate.getTime(message.timesTamp)
                itemChatListIvNoticeOval.visibility = if (message.countOfUnread > 0) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
                itemChatListTvNoticeCount.text = message.countOfUnread.toString()
                Picasso.get().load(message.ChatPartnerAvatarUrl).placeholder(R.drawable.ic_user_place_holder)
                    .into(itemChatListIvAvatar)
                root.setOnClickListener {
                    onItemClick.onClick(
                        User(
                            message.chatPartnerId,
                            message.chatPartnerLogin,
                            message.chatPartnerEmail,
                            "null",
                            message.ChatPartnerAvatarUrl
                        )
                    )
                }
            }
        }
    }
}
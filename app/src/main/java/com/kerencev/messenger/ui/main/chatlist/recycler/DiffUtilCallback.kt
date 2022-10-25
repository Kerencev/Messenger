package com.kerencev.messenger.ui.main.chatlist.recycler

import androidx.recyclerview.widget.DiffUtil
import com.kerencev.messenger.model.entities.ChatMessage

private const val TAG = "DiffUtilCallback"

class DiffUtilCallback(
    private var oldItems: List<ChatMessage>,
    private var newItems: List<ChatMessage>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].chatPartnerId == newItems[newItemPosition].chatPartnerId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].message == newItems[newItemPosition].message
                && oldItems[oldItemPosition].timesTamp == newItems[newItemPosition].timesTamp
                && oldItems[oldItemPosition].countOfUnread == newItems[newItemPosition].countOfUnread
                && oldItems[oldItemPosition].chatPartnerLogin == newItems[newItemPosition].chatPartnerLogin
                && oldItems[oldItemPosition].chatPartnerAvatarUrl == newItems[newItemPosition].chatPartnerAvatarUrl
                && oldItems[oldItemPosition].chatPartnerIsOnline == newItems[newItemPosition].chatPartnerIsOnline
                && oldItems[oldItemPosition].chatPartnerIsTyping == newItems[newItemPosition].chatPartnerIsTyping
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val old = oldItems[oldItemPosition]
        val new = newItems[newItemPosition]

        return Change(old, new)
    }
}

data class Change<T>(val oldData: T, val newData: T)

fun <T> createCombinePayload(payloads: List<Change<T>>): Change<T> {
    assert(payloads.isNotEmpty())
    val firstChange = payloads.first()
    val lastChange = payloads.last()

    return Change(firstChange.oldData, lastChange.newData)
}
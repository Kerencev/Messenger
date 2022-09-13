package com.kerencev.messenger.utils

import com.kerencev.messenger.model.entities.ChatMessage

/**
 * Class to help filter latest messages and sort it
 */
class SortChatListData {
    private val latestMessagesMap = HashMap<String, ChatMessage>()
    private val data = ArrayList<ChatMessage>()
    private val sortByTimeComparator =  Comparator<ChatMessage> { a, b ->
        when {
            (a.timesTamp > b.timesTamp) -> -1
            else -> 1
        }
    }

    fun getSortedData(latestMessage: ChatMessage): List<ChatMessage> {
        latestMessagesMap[latestMessage.chatPartnerId] = latestMessage
        data.clear()
        data.addAll(latestMessagesMap.values)
        data.sortWith(sortByTimeComparator)
        return data
    }

}
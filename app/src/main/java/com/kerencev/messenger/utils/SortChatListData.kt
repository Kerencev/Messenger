package com.kerencev.messenger.utils

import com.kerencev.messenger.model.entities.LatestMessage

/**
 * Class to help filter latest messages and sort it
 */
class SortChatListData {
    private val latestMessagesMap = HashMap<String, LatestMessage>()
    private val data = ArrayList<LatestMessage>()
    private val sortByTimeComparator =  Comparator<LatestMessage> { a, b ->
        when {
            (a.timesTamp > b.timesTamp) -> -1
            else -> 1
        }
    }

    fun getSortedData(latestMessage: LatestMessage): List<LatestMessage> {
        latestMessagesMap[latestMessage.chatPartnerId] = latestMessage
        data.clear()
        data.addAll(latestMessagesMap.values)
        data.sortWith(sortByTimeComparator)
        return data
    }

}
package com.kerencev.messenger.navigation.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.kerencev.messenger.ui.main.chatlist.ChatListFragment
import com.kerencev.messenger.ui.main.newmessage.NewMessageFragment

object ChatListScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return ChatListFragment()
    }
}

object NewMessageScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return NewMessageFragment()
    }
}
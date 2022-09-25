package com.kerencev.messenger.navigation.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.ui.main.chat.ChatFragment
import com.kerencev.messenger.ui.main.chat.wallpapers.WallpapersFragment
import com.kerencev.messenger.ui.main.chatlist.ChatListFragment
import com.kerencev.messenger.ui.main.newmessage.NewMessageFragment
import com.kerencev.messenger.ui.main.settings.SettingsFragment
import com.kerencev.messenger.ui.main.settings.changename.ChangeNameFragment

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

class ChatScreen(private val user: User) : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return ChatFragment.newInstance(user)
    }
}

object WallpapersScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return WallpapersFragment()
    }
}

object SettingsScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return SettingsFragment()
    }
}

object ChangeNameScreen: FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return ChangeNameFragment()
    }
}
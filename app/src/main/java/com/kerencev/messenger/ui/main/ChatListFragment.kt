package com.kerencev.messenger.ui.main

import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.kerencev.messenger.databinding.FragmentChatListBinding
import com.kerencev.messenger.ui.base.ViewBindingFragment

class ChatListFragment :
    ViewBindingFragment<FragmentChatListBinding>(FragmentChatListBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            //The solution is to make the icons visible
            chatListBottomNavigation.itemIconTintList = null;
            btnSignOut.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
            }
        }
    }
}
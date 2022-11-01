package com.kerencev.messenger.ui.main.newmessage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.ItemNewMessageUserRvBinding
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.utils.STATUS_ONLINE
import com.kerencev.messenger.utils.load

interface OnUserClickListener {
    fun onItemClick(user: User)
}

class UsersListAdapter(private val onUserClickListener: OnUserClickListener) :
    RecyclerView.Adapter<UsersListAdapter.ItemUserViewHolder>() {

    private val data = ArrayList<User>()

    fun setData(listOfUsers: List<User>) {
        data.clear()
        data.addAll(listOfUsers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemUserViewHolder {
        val binding =
            ItemNewMessageUserRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemUserViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: ItemUserViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ItemUserViewHolder(
        private val binding: ItemNewMessageUserRvBinding,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: User) = with(binding) {
            when (item.avatarUrl) {
                null -> tvLetter.text = item.login.first().toString()
                else -> ivAvatar.load(item.avatarUrl)
            }
            tvUserName.text = item.login
            when (item.status) {
                STATUS_ONLINE -> tvUserStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.user_status_online
                    )
                )
                else -> tvUserStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.hint_message
                    )
                )
            }
            tvUserStatus.text = item.status
            binding.root.setOnClickListener {
                onUserClickListener.onItemClick(item)
            }
        }
    }
}
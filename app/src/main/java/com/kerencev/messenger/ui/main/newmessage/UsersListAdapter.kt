package com.kerencev.messenger.ui.main.newmessage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kerencev.messenger.databinding.ItemNewMessageUserRvBinding
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.utils.loadUserImageWithGlide

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
        return ItemUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemUserViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ItemUserViewHolder(private val binding: ItemNewMessageUserRvBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: User) = with(binding) {
            item.avatarUrl?.let { ivAvatar.loadUserImageWithGlide(it) }
            tvUserName.text = item.login
            //TODO: add status
            tvUserStatus.text = "Online"
            binding.root.setOnClickListener {
                onUserClickListener.onItemClick(item)
            }
        }
    }
}
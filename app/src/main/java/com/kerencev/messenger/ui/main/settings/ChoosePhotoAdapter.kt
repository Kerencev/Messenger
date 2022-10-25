package com.kerencev.messenger.ui.main.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kerencev.messenger.R

interface OnItemClick {
    fun onClick(path: String)
}

class ChoosePhotoAdapter(private val onItemClick: OnItemClick) :
    RecyclerView.Adapter<ChoosePhotoAdapter.PhotoViewHolder>() {

    private val data = ArrayList<String>()

    fun setDataWithFirstPhotoIcon(listOfPhotos: List<String>) {
        data.clear()
        data.add(SettingsFragment.PHOTO_ICON)
        data.addAll(listOfPhotos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class PhotoViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val ivPhoto = view.findViewById<ImageView>(R.id.ivPhoto)

        fun bind(path: String) {
            if (path == SettingsFragment.PHOTO_ICON) {
                ivPhoto.setImageResource(R.drawable.icon_add_photo)
                ivPhoto.scaleType = ImageView.ScaleType.CENTER
            } else {
                Glide.with(view.context).load(path).into(ivPhoto)
            }
            view.setOnClickListener {
                onItemClick.onClick(path)
            }
        }
    }
}
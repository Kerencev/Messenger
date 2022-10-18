package com.kerencev.messenger.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide

object CircleImage {
    fun getCircleBitmap(context: Context, resources: Resources, imageUrl: String): Bitmap {
        val bitmap: Bitmap = Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .submit(512, 512)
            .get()
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(
            resources, bitmap
        )
        val roundPx = bitmap.width.toFloat() * 0.5f
        roundedBitmapDrawable.cornerRadius = roundPx
        return roundedBitmapDrawable.toBitmap()
    }
}
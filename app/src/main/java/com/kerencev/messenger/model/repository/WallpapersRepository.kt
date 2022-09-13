package com.kerencev.messenger.model.repository

import android.content.Context

interface WallpapersRepository {
    fun saveWallpaper(context: Context, wallpaper: String)
    fun getWallpaper(context: Context): String
}
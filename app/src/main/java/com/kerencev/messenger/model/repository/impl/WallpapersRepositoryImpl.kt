package com.kerencev.messenger.model.repository.impl

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.kerencev.messenger.model.repository.WallpapersRepository

const val WALLPAPERS_ONE = "WALLPAPERS_ONE"
const val WALLPAPERS_TWO = "WALLPAPERS_TWO"
const val WALLPAPERS_THREE = "WALLPAPERS_THREE"
private const val WALLPAPERS_KEY = "WALLPAPERS_KEY"
private const val S_PREF_WALLPAPERS_NAME = "S_PREF_WALLPAPERS_NAME"

class WallpapersRepositoryImpl : WallpapersRepository {

    override fun saveWallpaper(context: Context, wallpaper: String) {
        val sPref = context.getSharedPreferences(S_PREF_WALLPAPERS_NAME, MODE_PRIVATE)
        sPref.edit().putString(WALLPAPERS_KEY, wallpaper).apply()
    }

    override fun getWallpaper(context: Context): String {
        val sPref = context.getSharedPreferences(S_PREF_WALLPAPERS_NAME, MODE_PRIVATE)
        return sPref.getString(WALLPAPERS_KEY, WALLPAPERS_ONE)!!
    }
}
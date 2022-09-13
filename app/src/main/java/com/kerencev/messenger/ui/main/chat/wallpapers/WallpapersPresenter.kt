package com.kerencev.messenger.ui.main.chat.wallpapers

import android.content.Context
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.WallpapersRepository
import com.kerencev.messenger.ui.base.BasePresenter

class WallpapersPresenter(
    private val repository: WallpapersRepository,
    router: Router
) : BasePresenter<WallpapersView>(router) {

    fun saveWallpaper(context: Context, wallpaper: String) {
        repository.saveWallpaper(context, wallpaper)
        viewState.setChosenWallpaper(wallpaper)
    }

    fun loadWallpaper(context: Context) {
        val wallpaper = repository.getWallpaper(context)
        viewState.setChosenWallpaper(wallpaper)
    }
}
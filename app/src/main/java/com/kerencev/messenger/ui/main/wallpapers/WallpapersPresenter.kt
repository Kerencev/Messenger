package com.kerencev.messenger.ui.main.wallpapers

import android.content.Context
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.WallpapersRepository
import com.kerencev.messenger.ui.base.BasePresenter
import javax.inject.Inject

class WallpapersPresenter() : BasePresenter<WallpapersView>() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var repository: WallpapersRepository

    fun saveWallpaper(context: Context, wallpaper: String) {
        repository.saveWallpaper(context, wallpaper)
        viewState.setChosenWallpaper(wallpaper)
    }

    fun loadWallpaper(context: Context) {
        val wallpaper = repository.getWallpaper(context)
        viewState.setChosenWallpaper(wallpaper)
    }

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }
}
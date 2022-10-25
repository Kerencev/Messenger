package com.kerencev.messenger.ui.main.wallpapers

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface WallpapersView : MvpView {
    fun setChosenWallpaper(wallpaper: String)
}
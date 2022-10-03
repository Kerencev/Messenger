package com.kerencev.messenger.ui.main.settings.cropimage

import android.graphics.Bitmap
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface CropImageView : MvpView {
    fun finishWithResult(avatarUrl: String)
    fun showLoading(bitmap: Bitmap)
    fun hideLoading()
}
package com.kerencev.messenger.ui.main.settings.cropimage

import android.graphics.Bitmap
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.impl.MediaStoreRepository
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.log
import com.kerencev.messenger.utils.subscribeByDefault

class CropImagePresenter(
    private val router: Router,
    private val repoMedia: MediaStoreRepository
) : BasePresenter<CropImageView>(router) {

    fun saveAvatarToFirebase(bitmap: Bitmap) {
        viewState.showLoading(bitmap)
        repoMedia.saveImageToFirebase(bitmap)
            .subscribeByDefault()
            .subscribe(
                { avatarUrl ->
                    viewState.setResultForSettingsFragment(avatarUrl)
                    router.exit()
                },
                {
                    log(it.message.toString())
                }
            ).disposeBy(bag)
    }

    fun cancelDownload() {
        viewState.hideLoading()
        bag.dispose()
        bag.clear()
    }
}
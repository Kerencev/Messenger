package com.kerencev.messenger.ui.main.settings.cropimage

import android.graphics.Bitmap
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.MediaStoreRepository
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.log
import com.kerencev.messenger.utils.subscribeByDefault
import javax.inject.Inject

class CropImagePresenter() : BasePresenter<CropImageView>() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var repoMedia: MediaStoreRepository

    fun saveAvatarToFirebase(bitmap: Bitmap) {
        viewState.showLoading(bitmap)
        repoMedia.saveImageToFirebase(bitmap)
            .subscribeByDefault()
            .subscribe(
                { avatarUrl ->
                    viewState.finishWithResult(avatarUrl)
                },
                {
                    log(it.message.toString())
                }
            ).disposeBy(bag)
    }

    fun cancelDownload() {
        bag.clear()
        viewState.hideLoading()
    }

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }
}
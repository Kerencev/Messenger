package com.kerencev.messenger.model.repository

import android.content.Context
import android.graphics.Bitmap
import io.reactivex.rxjava3.core.Single

interface MediaStoreRepository {
    fun getImagesFromExternalStorage(context: Context): Single<List<String>>
    fun saveImageToFirebase(bitmap: Bitmap): Single<String>
}
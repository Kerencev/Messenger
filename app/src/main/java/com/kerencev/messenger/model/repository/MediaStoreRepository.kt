package com.kerencev.messenger.model.repository

import android.content.Context
import android.graphics.Bitmap
import io.reactivex.rxjava3.core.Single
import java.io.File

interface MediaStoreRepository {
    fun getImagesFromExternalStorage(context: Context): Single<List<String>>
    fun saveImageToFirebase(bitmap: Bitmap): Single<String>
    fun saveFileToFirebaseStorage(file: File): Single<String>
}
package com.kerencev.messenger.model.repository.impl

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kerencev.messenger.model.repository.MediaStoreRepository
import io.reactivex.rxjava3.core.Single
import java.io.ByteArrayOutputStream

class MediaStoreRepositoryImpl : MediaStoreRepository {
    override fun getImagesFromExternalStorage(context: Context): Single<List<String>> {
        return Single.create {
            val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val cursor: Cursor
            val listOfAllImages = ArrayList<String>()
            var absolutePathOfImage: String

            val projection =
                arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            val orderBy = MediaStore.Video.Media.DATE_TAKEN
            cursor = context.contentResolver.query(uri, projection, null, null, "$orderBy DESC")!!

            val columnIndexData: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(columnIndexData)
                listOfAllImages.add(absolutePathOfImage)
            }

            it.onSuccess(listOfAllImages)
        }
    }

    override fun saveImageToFirebase(bitmap: Bitmap): Single<String> {
        return Single.create { emitter ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            val byteArray: ByteArray = stream.toByteArray()
            val storageRef = FirebaseStorage.getInstance().getReference("/users-avatars/$userId")
            storageRef.putBytes(byteArray)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val userAvatarRef = FirebaseDatabase.getInstance()
                            .getReference("/users/$userId/avatarUrl")
                        userAvatarRef.setValue(uri.toString())
                            .addOnSuccessListener {
                                emitter.onSuccess(uri.toString())
                            }
                            .addOnFailureListener {
                                emitter.onError(it)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    emitter.onError(exception)
                }
        }
    }
}
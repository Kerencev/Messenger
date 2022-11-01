package com.kerencev.messenger.utils.record

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File

interface Recorder {

    fun startRecord(): Completable
    fun stopRecord(): Single<File>
    fun deleteRecord(): Completable
    fun releaseRecorder(): Completable
}
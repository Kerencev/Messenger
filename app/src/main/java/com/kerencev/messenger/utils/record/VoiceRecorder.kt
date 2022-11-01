package com.kerencev.messenger.utils.record

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File

/**
 * Class to record voice messages
 */
class VoiceRecorder(private val context: Context) : Recorder {

    private val mediaRecorder by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }
    private var file: File? = null

    override fun startRecord(): Completable {
        return Completable.create { emitter ->
            try {
                createFileForRecord(context)
                prepareMediaRecorder()
                mediaRecorder.start()
                emitter.onComplete()
            } catch (e: Exception) {
                file?.delete()
                emitter.onError(e)
            }
        }
    }

    override fun stopRecord(): Single<File> {
        return Single.create { emitter ->
            try {
                mediaRecorder.stop()
                if (file != null) {
                    emitter.onSuccess(file!!)
                }
            } catch (e: Exception) {
                file?.delete()
                emitter.onError(e)
            }
        }
    }

    override fun deleteRecord(): Completable {
        return Completable.create { emitter ->
            try {
                mediaRecorder.stop()
                file?.delete()
                emitter.onComplete()
            } catch (e: Exception) {
                file?.delete()
                emitter.onError(e)
            }
        }
    }

    override fun releaseRecorder(): Completable = Completable.create { emitter ->
        try {
            mediaRecorder.release()
            emitter.onComplete()
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    private fun createFileForRecord(context: Context) {
        file = File(context.filesDir, System.currentTimeMillis().toString())
        file?.createNewFile()
    }

    private fun prepareMediaRecorder() {
        with(mediaRecorder) {
            reset()
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(96000)
            setAudioEncodingBitRate(128000)
            setOutputFile(file?.absolutePath)
            prepare()
        }
    }
}
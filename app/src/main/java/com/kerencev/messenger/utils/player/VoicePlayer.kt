package com.kerencev.messenger.utils.player

import android.media.MediaPlayer
import io.reactivex.rxjava3.core.Completable
import java.io.File

class VoicePlayer : Player {

    private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }

    override fun play(audioFile: File): Completable = Completable.create { emitter ->
        try {
            mediaPlayer.setDataSource(audioFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.stop()
                mediaPlayer.reset()
                emitter.onComplete()
            }
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    override fun releasePlayer(): Completable = Completable.create { emitter ->
        try {
            mediaPlayer.release()
            emitter.onComplete()
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }
}
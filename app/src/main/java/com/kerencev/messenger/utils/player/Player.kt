package com.kerencev.messenger.utils.player

import io.reactivex.rxjava3.core.Completable
import java.io.File

interface Player {

    fun play(audioFile: File): Completable
    fun releasePlayer(): Completable
}
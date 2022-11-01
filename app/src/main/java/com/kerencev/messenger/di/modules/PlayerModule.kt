package com.kerencev.messenger.di.modules

import android.content.Context
import com.kerencev.messenger.utils.player.Player
import com.kerencev.messenger.utils.player.VoicePlayer
import com.kerencev.messenger.utils.record.Recorder
import com.kerencev.messenger.utils.record.VoiceRecorder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PlayerModule {

    @Singleton
    @Provides
    fun provideVoiceRecorder(context: Context): Recorder {
        return VoiceRecorder(context)
    }

    @Singleton
    @Provides
    fun provideVoicePlayer(): Player {
        return VoicePlayer()
    }
}
package com.kerencev.messenger.di.modules

import android.content.Context
import com.kerencev.messenger.utils.vibration.AppVibrator
import com.kerencev.messenger.utils.vibration.Vibration
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class VibrationModule {

    @Singleton
    @Provides
    fun provideAppVibrator(context: Context): Vibration {
        return AppVibrator(context)
    }
}
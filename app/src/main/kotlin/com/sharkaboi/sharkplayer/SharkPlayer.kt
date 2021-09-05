package com.sharkaboi.sharkplayer

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import kotlin.time.ExperimentalTime

@HiltAndroidApp
class SharkPlayer : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }
}
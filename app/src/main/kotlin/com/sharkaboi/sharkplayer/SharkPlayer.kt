package com.sharkaboi.sharkplayer

import androidx.multidex.MultiDexApplication
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SharkPlayer : MultiDexApplication() {

    @Inject
    lateinit var coilImageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(coilImageLoader)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
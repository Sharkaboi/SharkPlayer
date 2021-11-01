package com.sharkaboi.sharkplayer

import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SharkPlayer : MultiDexApplication(), Configuration.Provider {

    @Inject
    lateinit var coilImageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(coilImageLoader)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}
package com.sharkaboi.sharkplayer.modules.splash.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sharkaboi.sharkplayer.common.extensions.launchAndFinishAffinity
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.modules.main.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPrefRepository: SharedPrefRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configTheme()
        openAppFlow()
    }

    private fun openAppFlow() = launchAndFinishAffinity<MainActivity>()

    private fun configTheme() {
        val isDarkTheme = sharedPrefRepository.isDarkTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
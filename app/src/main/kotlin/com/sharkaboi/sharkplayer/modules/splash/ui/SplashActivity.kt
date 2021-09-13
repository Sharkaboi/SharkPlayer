package com.sharkaboi.sharkplayer.modules.splash.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.constants.AppConstants
import com.sharkaboi.sharkplayer.common.extensions.launchAndFinishAffinity
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.modules.main.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            handlePermissionResult(isGranted)
        }

    @Inject
    lateinit var sharedPrefRepository: SharedPrefRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        configTheme()
    }

    private fun checkPermissions() {
        requestPermissions.launch(AppConstants.requiredPermissions)
    }

    private fun configTheme() {
        val isDarkTheme = sharedPrefRepository.isDarkTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun openAppFlow() = launchAndFinishAffinity<MainActivity>()

    private fun handlePermissionResult(grantedMap: Map<String, Boolean>) {
        var isGranted = true
        grantedMap.forEach { isGranted = isGranted && it.value }
        if (isGranted) {
            openAppFlow()
        } else {
            showToast(R.string.permissions_hint)
            checkPermissions()
        }
    }
}
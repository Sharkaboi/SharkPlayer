package com.sharkaboi.sharkplayer.common.constants

import android.Manifest
import android.os.Build

object AppConstants {
    val requiredPermissions: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.MEDIA_CONTENT_CONTROL,
//        Manifest.permission.MODIFY_AUDIO_SETTINGS,
//        *sdkSpecificPermissions
    )
    private val sdkSpecificPermissions: Array<String>
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.MANAGE_MEDIA,
                    Manifest.permission.MANAGE_MEDIA,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                )
            } else {
                return emptyArray()
            }
        }
    const val githubLink = "https://github.com/Sharkaboi/SharkPlayer"
}
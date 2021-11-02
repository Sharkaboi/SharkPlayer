package com.sharkaboi.sharkplayer.data.sharedpref

import android.content.SharedPreferences
import com.sharkaboi.sharkplayer.common.extensions.emptyString

class SharedPrefRepository(
    private val sharedPreferences: SharedPreferences
) {

    fun isDarkTheme() = sharedPreferences.getBoolean(SharedPrefKeys.DARK_THEME, false)

    fun getSubtitleLanguages() =
        sharedPreferences.getString(SharedPrefKeys.SUBTITLE_LANGUAGE, String.emptyString)

    fun getAudioLanguages() =
        sharedPreferences.getString(SharedPrefKeys.AUDIO_LANGUAGE, String.emptyString)

    fun shouldStartVideoPaused() =
        sharedPreferences.getBoolean(SharedPrefKeys.START_PAUSED_VIDEO, false)

    fun shouldStartAudioPaused() =
        sharedPreferences.getBoolean(SharedPrefKeys.START_PAUSED_AUDIO, false)
}
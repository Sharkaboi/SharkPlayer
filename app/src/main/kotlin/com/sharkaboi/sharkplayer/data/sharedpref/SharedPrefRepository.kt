package com.sharkaboi.sharkplayer.data.sharedpref

import android.content.SharedPreferences

class SharedPrefRepository(
    private val sharedPreferences: SharedPreferences
) {

    fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(SharedPrefKeys.DARK_THEME, false)
    }
}
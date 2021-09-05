package com.sharkaboi.sharkplayer.modules.settings.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.sharkaboi.sharkplayer.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}
package com.sharkaboi.sharkplayer.modules.settings.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.sharkaboi.sharkplayer.BuildConfig
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefKeys
import com.sharkaboi.sharkplayer.modules.settings.vm.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsViewModel by viewModels<SettingsViewModel>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
    }

    private fun setupListeners() {
        findPreference<Preference>(SharedPrefKeys.ABOUT)?.summaryProvider =
            Preference.SummaryProvider { _: Preference ->
                getString(R.string.version, BuildConfig.VERSION_NAME)
            }
        findPreference<SwitchPreference>(SharedPrefKeys.DARK_THEME)?.setOnPreferenceChangeListener { _, newValue ->
            AppCompatDelegate.setDefaultNightMode(
                if (newValue == true)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
            true
        }
    }

}
package com.sharkaboi.sharkplayer.modules.settings.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharkaboi.appupdatechecker.AppUpdateChecker
import com.sharkaboi.appupdatechecker.models.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val appUpdateChecker: AppUpdateChecker
) : ViewModel() {
    private val _uiState = MutableLiveData<SettingsState>().getDefault()
    val uiState: LiveData<SettingsState> = _uiState

    fun checkUpdate() {
        viewModelScope.launch {
            _uiState.setLoading()
            when (val updateState = appUpdateChecker.checkUpdate()) {
                is UpdateState.UpdateAvailable -> _uiState.setSuccess(updateState)
                UpdateState.LatestVersionInstalled -> _uiState.setError("Latest version already installed")
                UpdateState.GithubInvalid -> _uiState.setError("Could not fetch update info, Try again later")
                UpdateState.NoNetworkFound -> _uiState.setError("No network found")
                else -> _uiState.setError("An error occurred")
            }
        }
    }

}

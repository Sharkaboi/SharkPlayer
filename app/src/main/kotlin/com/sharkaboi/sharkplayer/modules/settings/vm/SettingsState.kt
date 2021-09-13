package com.sharkaboi.sharkplayer.modules.settings.vm

import androidx.lifecycle.MutableLiveData
import com.sharkaboi.appupdatechecker.models.UpdateState
import com.sharkaboi.sharkplayer.common.extensions.emptyString


sealed class SettingsState {

    object Idle : SettingsState()

    object Loading : SettingsState()

    data class Failure(val message: String) : SettingsState()

    data class Success(val message: UpdateState.UpdateAvailable) : SettingsState()

}

internal fun MutableLiveData<SettingsState>.setError(message: String) {
    this.value = SettingsState.Failure(message)
}

internal fun MutableLiveData<SettingsState>.setError(exception: Exception) {
    this.value = SettingsState.Failure(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<SettingsState>.setSuccess(data: UpdateState.UpdateAvailable) {
    this.value = SettingsState.Success(data)
}

internal fun MutableLiveData<SettingsState>.setIdle() {
    this.value = SettingsState.Idle
}

internal fun MutableLiveData<SettingsState>.setLoading() {
    this.value = SettingsState.Loading
}

internal fun MutableLiveData<SettingsState>.getDefault() = this.apply {
    this.setIdle()
}
package com.sharkaboi.sharkplayer.modules.home.vm

import androidx.lifecycle.MutableLiveData
import com.sharkaboi.sharkplayer.common.extensions.emptyString

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class Failure(val message: String) : HomeState()
}

internal fun MutableLiveData<HomeState>.setError(message: String) {
    this.value = HomeState.Failure(message)
}

internal fun MutableLiveData<HomeState>.setError(exception: Exception) {
    this.value = HomeState.Failure(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<HomeState>.setIdle() {
    this.value = HomeState.Idle
}

internal fun MutableLiveData<HomeState>.setLoading() {
    this.value = HomeState.Loading
}

internal fun MutableLiveData<HomeState>.getDefault() = this.apply {
    this.setIdle()
}

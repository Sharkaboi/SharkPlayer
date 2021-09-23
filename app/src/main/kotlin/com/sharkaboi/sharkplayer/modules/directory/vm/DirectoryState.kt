package com.sharkaboi.sharkplayer.modules.directory.vm

import androidx.lifecycle.MutableLiveData
import com.sharkaboi.sharkplayer.common.extensions.emptyString
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile

sealed class DirectoryState {
    object Idle : DirectoryState()
    object Loading : DirectoryState()
    object DirectoryNotFound : DirectoryState()
    data class Failure(val message: String) : DirectoryState()
    data class LoadSuccess(val files: List<SharkPlayerFile>) : DirectoryState()
}

internal fun MutableLiveData<DirectoryState>.setSuccess(files: List<SharkPlayerFile>) {
    this.value = DirectoryState.LoadSuccess(files)
}

internal fun MutableLiveData<DirectoryState>.setError(message: String) {
    this.value = DirectoryState.Failure(message)
}

internal fun MutableLiveData<DirectoryState>.setError(exception: Exception) {
    this.value = DirectoryState.Failure(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<DirectoryState>.setIdle() {
    this.value = DirectoryState.Idle
}

internal fun MutableLiveData<DirectoryState>.setLoading() {
    this.value = DirectoryState.Loading
}

internal fun MutableLiveData<DirectoryState>.setDirectoryNotFound() {
    this.value = DirectoryState.DirectoryNotFound
}

internal fun MutableLiveData<DirectoryState>.getDefault() = this.apply {
    this.setIdle()
}

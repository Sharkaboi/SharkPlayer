package com.sharkaboi.sharkplayer.exoplayer.audio.vm

import androidx.lifecycle.MutableLiveData
import com.sharkaboi.sharkplayer.common.extensions.emptyString
import com.sharkaboi.sharkplayer.exoplayer.audio.model.AudioInfo

sealed class AudioPlayerState {
    object Idle : AudioPlayerState()

    object Loading : AudioPlayerState()

    data class InvalidData(
        val message: String
    ) : AudioPlayerState()

    data class Success(
        val audioInfo: AudioInfo
    ) : AudioPlayerState()
}

internal fun MutableLiveData<AudioPlayerState>.setSuccess(
    audioInfo: AudioInfo
) {
    this.value = AudioPlayerState.Success(audioInfo)
}

internal fun MutableLiveData<AudioPlayerState>.setInvalidData(message: String) {
    this.value = AudioPlayerState.InvalidData(message)
}

internal fun MutableLiveData<AudioPlayerState>.setError(exception: Exception) {
    this.value = AudioPlayerState.InvalidData(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<AudioPlayerState>.setIdle() {
    this.value = AudioPlayerState.Idle
}

internal fun MutableLiveData<AudioPlayerState>.setLoading() {
    this.value = AudioPlayerState.Loading
}

internal fun MutableLiveData<AudioPlayerState>.getDefault() = this.apply {
    this.setIdle()
}


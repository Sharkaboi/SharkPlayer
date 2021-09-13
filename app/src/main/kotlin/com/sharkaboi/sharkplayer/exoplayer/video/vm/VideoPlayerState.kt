package com.sharkaboi.sharkplayer.exoplayer.video.vm

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.sharkaboi.sharkplayer.common.extensions.emptyString
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo

sealed class VideoPlayerState {
    object Idle : VideoPlayerState()

    object Loading : VideoPlayerState()

    data class InvalidData(
        val message: String
    ) : VideoPlayerState()

    data class Success(
        val videoInfo: VideoInfo
    ) : VideoPlayerState()
}

internal fun MutableLiveData<VideoPlayerState>.setSuccess(
    videoInfo: VideoInfo
) {
    this.value = VideoPlayerState.Success(videoInfo)
}

internal fun MutableLiveData<VideoPlayerState>.setInvalidData(message: String) {
    this.value = VideoPlayerState.InvalidData(message)
}

internal fun MutableLiveData<VideoPlayerState>.setError(exception: Exception) {
    this.value = VideoPlayerState.InvalidData(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<VideoPlayerState>.setIdle() {
    this.value = VideoPlayerState.Idle
}

internal fun MutableLiveData<VideoPlayerState>.setLoading() {
    this.value = VideoPlayerState.Loading
}

internal fun MutableLiveData<VideoPlayerState>.getDefault() = this.apply {
    this.setIdle()
}


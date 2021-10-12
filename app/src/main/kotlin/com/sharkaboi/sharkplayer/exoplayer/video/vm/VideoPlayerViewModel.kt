package com.sharkaboi.sharkplayer.exoplayer.video.vm

import androidx.lifecycle.*
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo
import com.sharkaboi.sharkplayer.exoplayer.video.repo.VideoPlayerRepository
import com.sharkaboi.sharkplayer.modules.directory.vm.DirectoryViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val videoPlayerRepository: VideoPlayerRepository
) : ViewModel() {
    private val path = savedStateHandle.get<String>(DirectoryViewModel.PATH_KEY)
    private val _uiState = MutableLiveData<VideoPlayerState>().getDefault()
    val uiState: LiveData<VideoPlayerState> = _uiState
    private val _playbackState = MutableLiveData<VideoPlayBackState>()
    val playbackState: LiveData<VideoPlayBackState> = _playbackState

    init {
        if (path == null) {
            _uiState.setInvalidData("Path was null")
        } else {
            loadVideoMetadata(path)
        }
    }

    private fun loadVideoMetadata(path: String) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = videoPlayerRepository.getMetaDataOf(path)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setSuccess(result.data)
            }
        }
    }

    private fun updateMetadata(videoInfo: VideoInfo) {
        _uiState.setLoading()
        _uiState.setSuccess(videoInfo)
    }

    private fun updatePlaybackState(videoPlayBackState: VideoPlayBackState) {
        _uiState.setLoading()
        _playbackState.value = videoPlayBackState
    }

    companion object {
        const val PATH_KEY = "path"
    }
}
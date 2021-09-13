package com.sharkaboi.sharkplayer.exoplayer.video.vm

import androidx.lifecycle.*
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

    init {
        if (path == null) {
            _uiState.setInvalidData("Path was null")
        } else {
            loadVideoMetadata(path)
        }
    }

    private fun loadVideoMetadata(path: String) {
        viewModelScope.launch {
            _uiState.setLoading()
            val result = videoPlayerRepository.getMetaDataOf(path)
        }
    }


    companion object {
        const val PATH_KEY = "path"
    }
}
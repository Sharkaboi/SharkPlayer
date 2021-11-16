package com.sharkaboi.sharkplayer.exoplayer.audio.vm

import androidx.lifecycle.*
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.exoplayer.audio.repo.AudioPlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AudioPlayerViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {
    private val audioPath = savedStateHandle.get<String>(AUDIO_NAV_ARGS_KEY)
    private val _uiState = MutableLiveData<AudioPlayerState>().getDefault()
    val uiState: LiveData<AudioPlayerState> = _uiState

    init {
        if (audioPath == null || audioPath.isBlank()) {
            _uiState.setInvalidData("Path was null")
        } else {
            loadVideoMetadata(audioPath)
        }
    }

    private fun loadVideoMetadata(audioPath: String) {
        Timber.d("Vm called")
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = audioPlayerRepository.getMetaDataOf(audioPath)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setSuccess(result.data)
            }
        }
    }

    companion object {
        const val AUDIO_NAV_ARGS_KEY = "path"
    }
}
package com.sharkaboi.sharkplayer.modules.directory.vm

import androidx.lifecycle.*
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.modules.directory.repo.DirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectoryViewModel
@Inject constructor(
    private val directoryRepository: DirectoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val path = savedStateHandle.get<String>(PATH_KEY)
    val selectedDir = SharkPlayerFile.directoryFromPath(path)
    val isFavorite: LiveData<Boolean> =
        directoryRepository.favorites.map { list ->
            list.firstOrNull { it.path == selectedDir.path } != null
        }.asLiveData()
    val subtitleIndexOfDirectory: LiveData<Int?> =
        directoryRepository.subtitleTrackIndices.map { subtitleIndices ->
            subtitleIndices[selectedDir.path]
        }.asLiveData()
    val audioIndexOfDirectory: LiveData<Int?> =
        directoryRepository.audioTrackIndices.map { audioIndices ->
            audioIndices[selectedDir.path]
        }.asLiveData()
    private val _uiState = MutableLiveData<DirectoryState>().getDefault()
    val uiState: LiveData<DirectoryState> = _uiState

    init {
        loadDirectory()
    }

    private fun loadDirectory() {
        _uiState.setLoading()
        viewModelScope.launch {
            when (directoryRepository.doesExist(selectedDir)) {
                is TaskState.Failure -> {
                    _uiState.setDirectoryNotFound()
                    return@launch
                }
                else -> Unit
            }
            when (val result = directoryRepository.getFilesInFolder(selectedDir)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setSuccess(result.data)
            }
        }
    }

    fun toggleFavorite() {
        _uiState.setLoading()
        viewModelScope.launch {
            val result =
                if (isFavorite.value == true) {
                    directoryRepository.removeFolderAsFavorite(selectedDir)
                } else {
                    directoryRepository.setFolderAsFavorite(selectedDir)
                }
            when (result) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setIdle()
            }
        }
    }

    fun setSubTrackIndexOfDir(trackId: Int) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = directoryRepository.setSubTrackIndexOfDir(trackId, selectedDir)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setIdle()
            }
        }
    }

    fun setAudioTrackIndexOfDir(trackId: Int) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = directoryRepository.setAudioTrackIndexOfDir(trackId, selectedDir)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setIdle()
            }
        }
    }

    companion object {
        const val PATH_KEY = "path"
    }
}
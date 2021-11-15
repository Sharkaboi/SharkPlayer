package com.sharkaboi.sharkplayer.modules.directory.vm

import android.app.Application
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.SharkPlayer
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.ffmpeg.command.FFMpegCommandWrapper
import com.sharkaboi.sharkplayer.ffmpeg.workers.FFMpegWorker
import com.sharkaboi.sharkplayer.modules.directory.repo.DirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectoryViewModel
@Inject constructor(
    app: Application,
    private val directoryRepository: DirectoryRepository,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {
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

    private val _files = MutableLiveData<List<SharkPlayerFile>>()
    val files: LiveData<List<SharkPlayerFile>> = _files

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
                is TaskState.Success -> {
                    _files.value = result.data
                    _uiState.setIdle()
                }
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
                else -> _uiState.setIdle()
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

    fun runRescaleWork(videoFile: SharkPlayerFile.VideoFile, targetResolution: String?) {
        if (targetResolution == null) {
            _uiState.setError("Invalid resolution passed")
            return
        }

        val (cmd, outputPath) = FFMpegCommandWrapper.rescaleVideo(videoFile, targetResolution)
        val notificationContent = getApplication<SharkPlayer>()
            .getString(
                R.string.rescale_notification_content,
                videoFile.fileName,
                videoFile.videoHeight,
                targetResolution
            )
        val notificationTitle = getApplication<SharkPlayer>()
            .getString(R.string.rescale_notification_title)

        val inputData = FFMpegWorker.getWorkData(
            cmd,
            notificationTitle = notificationTitle,
            notificationContent = notificationContent,
            targetFilePath = outputPath
        )
        val rescaleWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<FFMpegWorker>()
            .setInputData(inputData)
            .addTag(notificationContent)
            .build()
        WorkManager.getInstance(getApplication<SharkPlayer>().applicationContext)
            .enqueue(rescaleWorkRequest)
    }

    fun refresh() {
        loadDirectory()
    }

    fun deleteVideo(videoFile: SharkPlayerFile.VideoFile) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = directoryRepository.deleteVideo(videoFile)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> {
                    refresh()
                    _uiState.setIdle()
                }
            }
        }
    }

    companion object {
        const val PATH_KEY = "path"
    }
}
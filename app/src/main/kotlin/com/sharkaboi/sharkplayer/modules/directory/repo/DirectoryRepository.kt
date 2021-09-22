package com.sharkaboi.sharkplayer.modules.directory.repo

import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import kotlinx.coroutines.flow.Flow

interface DirectoryRepository {
    val favorites: Flow<List<SharkPlayerFile.Directory>>

    suspend fun getFilesInFolder(directory: SharkPlayerFile.Directory): TaskState<List<SharkPlayerFile>>

    suspend fun setFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit>

    suspend fun removeFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit>

    suspend fun setSubTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit>

    suspend fun setAudioTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit>
}
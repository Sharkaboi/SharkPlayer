package com.sharkaboi.sharkplayer.modules.directory.repo

import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.models.toSharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

class FileDirectoryRepository(
    private val dataStoreRepository: DataStoreRepository,
) : DirectoryRepository {

    override val favorites: Flow<List<SharkPlayerFile.Directory>> =
        dataStoreRepository.favouritesDirsFlow

    override suspend fun getFilesInFolder(directory: SharkPlayerFile.Directory): TaskState<List<SharkPlayerFile>> =
        tryCatching {
            val currentDirectory = File(directory.path)
            val filesInDir = currentDirectory.listFiles().orEmpty()
            val sharkFiles: List<SharkPlayerFile> = filesInDir.map { it.toSharkPlayerFile() }
            TaskState.Success(sharkFiles)
        }

    override suspend fun setFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            dataStoreRepository.addFavorite(directory)
            TaskState.Success(Unit)
        }

    override suspend fun removeFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            dataStoreRepository.removeFavorite(directory)
            TaskState.Success(Unit)
        }

    override suspend fun setSubTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun setAudioTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun doesExist(selectedDir: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            val file = File(selectedDir.path)
            if (file.exists() && file.isDirectory) {
                TaskState.Success(Unit)
            } else {
                TaskState.failureWithMessage("Directory does not exist.")
            }
        }
}
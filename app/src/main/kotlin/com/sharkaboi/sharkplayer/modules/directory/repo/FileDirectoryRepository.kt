package com.sharkaboi.sharkplayer.modules.directory.repo

import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.models.toSharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class FileDirectoryRepository(
    private val dataStoreRepository: DataStoreRepository,
) : DirectoryRepository {

    override val favorites: Flow<List<SharkPlayerFile.Directory>> =
        dataStoreRepository.favouritesDirsFlow

    override suspend fun getFilesInFolder(directory: SharkPlayerFile.Directory): TaskState<List<SharkPlayerFile>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val currentDirectory = File(directory.path)
                val filesInDir = currentDirectory.listFiles()?.toList() ?: emptyList()
                val sharkFiles: List<SharkPlayerFile> = filesInDir.map { it.toSharkPlayerFile() }
                TaskState.Success(sharkFiles)
            } catch (e: Exception) {
                Timber.e(e)
                TaskState.Failure(e)
            }
        }

    override suspend fun setFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                dataStoreRepository.addFavorite(directory)
                TaskState.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e)
                TaskState.Failure(e)
            }
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
}
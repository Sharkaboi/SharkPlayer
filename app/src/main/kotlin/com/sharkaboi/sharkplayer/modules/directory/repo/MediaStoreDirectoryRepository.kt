package com.sharkaboi.sharkplayer.modules.directory.repo

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import com.sharkaboi.sharkplayer.common.extensions.*
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.time.Duration

class MediaStoreDirectoryRepository(
    private val dataStoreRepository: DataStoreRepository,
    private val contentResolver: ContentResolver
) : DirectoryRepository {

    override val favorites: Flow<List<SharkPlayerFile.Directory>> =
        dataStoreRepository.favouritesDirsFlow

    override suspend fun getFilesInFolder(directory: SharkPlayerFile.Directory): TaskState<List<SharkPlayerFile>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val sharkFiles = mutableListOf<SharkPlayerFile.VideoFile>()
                val mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                val cursor = ContentResolverCompat.query(
                    contentResolver,
                    mediaUri,
                    arrayOf(
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.RESOLUTION,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.SIZE,
                    ),
                    null,
                    null,
                    MediaStore.Video.Media.DATE_ADDED + " DESC",
                    null
                )
                while (cursor != null && cursor.moveToNext()) {
                    val path = cursor.getStringOfColumnName(MediaStore.Video.Media.DATA)
                    val name = cursor.getStringOfColumnName(MediaStore.Video.Media.DISPLAY_NAME)
                    val resolution = cursor.getStringOfColumnName(MediaStore.Video.Media.RESOLUTION)
                    val length = cursor.getLongOfColumnName(MediaStore.Video.Media.DURATION)
                    val size = cursor.getLongOfColumnName(MediaStore.Video.Media.SIZE)
                    sharkFiles.add(
                        SharkPlayerFile.VideoFile(
                            fileName = name,
                            path = path,
                            resolution = resolution,
                            length = Duration.milliseconds(length),
                            size = size
                        )
                    )
                }
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
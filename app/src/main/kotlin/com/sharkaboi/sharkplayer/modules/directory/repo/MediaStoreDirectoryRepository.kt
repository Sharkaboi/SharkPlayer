package com.sharkaboi.sharkplayer.modules.directory.repo

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import com.sharkaboi.sharkplayer.common.extensions.getLongOfColumnName
import com.sharkaboi.sharkplayer.common.extensions.getStringOfColumnName
import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import kotlin.time.Duration

class MediaStoreDirectoryRepository(
    private val dataStoreRepository: DataStoreRepository,
    private val contentResolver: ContentResolver,
) : DirectoryRepository {

    override val favorites: Flow<List<SharkPlayerFile.Directory>> =
        dataStoreRepository.favouritesDirsFlow
    override val subtitleTrackIndices: Flow<Map<String, Int>> =
        dataStoreRepository.subtitleTrackIndices
    override val audioTrackIndices: Flow<Map<String, Int>> =
        dataStoreRepository.audioTrackIndices

    override suspend fun getFilesInFolder(directory: SharkPlayerFile.Directory): TaskState<List<SharkPlayerFile>> =
        tryCatching {
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
                val height = cursor.getLongOfColumnName(MediaStore.Video.Media.HEIGHT)
                val width = cursor.getLongOfColumnName(MediaStore.Video.Media.WIDTH)
                val length = cursor.getLongOfColumnName(MediaStore.Video.Media.DURATION)
                val size = cursor.getLongOfColumnName(MediaStore.Video.Media.SIZE)
                sharkFiles.add(
                    SharkPlayerFile.VideoFile(
                        fileName = name,
                        path = path,
                        videoHeight = height.toInt(),
                        videoWidth = width.toInt(),
                        length = Duration.milliseconds(length),
                        size = size
                    )
                )
            }
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
    ): TaskState<Unit> = tryCatching {
        dataStoreRepository.setSubTrackIndexOfDir(trackId, directory)
        TaskState.Success(Unit)
    }

    override suspend fun setAudioTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit> = tryCatching {
        dataStoreRepository.setAudioTrackIndexOfDir(trackId, directory)
        TaskState.Success(Unit)
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

    override suspend fun deleteVideo(videoFile: SharkPlayerFile.VideoFile): TaskState<Unit> =
        tryCatching {
            val file = videoFile.getFile()
            if (!file.exists()) {
                return@tryCatching TaskState.failureWithMessage("Video file does not exist.")
            }

            val isSuccess = file.delete()
            if (!isSuccess) {
                return@tryCatching TaskState.failureWithMessage("Couldn't delete video file.")
            }

            TaskState.Success(Unit)
        }
}
package com.sharkaboi.sharkplayer.exoplayer.video.repo

import android.net.Uri
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.MimeTypes
import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.exoplayer.util.AUDIO_LANGUAGE_SEPARATOR
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SUBTITLE_LANGUAGE_SEPARATOR
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoNavArgs
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

class VideoPlayerRepository(
    private val sharedPrefRepository: SharedPrefRepository,
    private val dataStoreRepository: DataStoreRepository,
) {
    suspend fun getMetaDataOf(videoNavArgs: VideoNavArgs): TaskState<VideoInfo> =
        tryCatching {
            val dirPath = videoNavArgs.dirPath
            val subtitleOptions = getSubsConfiguration(dirPath, videoNavArgs)
            val audioOptions = getAudioConfiguration(dirPath, videoNavArgs)
            val playWhenReady = sharedPrefRepository.shouldStartVideoPaused().not()
            val mediaItems = videoNavArgs.videoPaths.map {
                val mediaItemBuilder = MediaItem.Builder().setUri(it)
                val subsList = mutableListOf<MediaItem.Subtitle>()

                val sameNameSubUri = subOfVideoName(it, dirPath)
                if (sameNameSubUri != null) {
                    val mediaItemSubtitle =
                        MediaItem.Subtitle(
                            sameNameSubUri,
                            MimeTypes.APPLICATION_SUBRIP,
                            null,
                            0,
                            0,
                            "Local - ${File(sameNameSubUri.toString()).name}"
                        )
                    subsList.add(mediaItemSubtitle)
                }

                val subsFolderSubUri = subOfVideoNameInSubsFolder(it, dirPath)
                if (subsFolderSubUri != null) {
                    val mediaItemSubtitle =
                        MediaItem.Subtitle(
                            subsFolderSubUri,
                            MimeTypes.APPLICATION_SUBRIP,
                            null,
                            0,
                            0,
                            "Subs Folder - ${File(subsFolderSubUri.toString()).name}"
                        )
                    subsList.add(mediaItemSubtitle)
                }

                mediaItemBuilder.setSubtitles(subsList)
                mediaItemBuilder.build()
            }

            TaskState.Success(
                VideoInfo(
                    videoMediaItems = mediaItems,
                    subtitleOptions = subtitleOptions,
                    audioOptions = audioOptions,
                    playWhenReady = playWhenReady
                )
            )
        }

    private fun subOfVideoNameInSubsFolder(videoUri: String, dirPath: String): Uri? {
        val videoFile = File(videoUri)
        val subFile = File(
            dirPath
                    + File.separator
                    + SUBS_FOLDER_NAME
                    + File.separator
                    + videoFile.nameWithoutExtension
                    + SUB_EXTENSION
        )
        return when {
            subFile.exists() -> subFile.absolutePath.toUri()
            else -> null
        }
    }

    private fun subOfVideoName(videoUri: String, dirPath: String): Uri? {
        val videoFile = File(videoUri)
        val subFile = File(
            dirPath
                    + File.separator
                    + videoFile.nameWithoutExtension
                    + SUB_EXTENSION
        )
        return when {
            subFile.exists() -> subFile.absolutePath.toUri()
            else -> null
        }
    }

    private suspend fun getAudioConfiguration(
        dirPath: String,
        videoNavArgs: VideoNavArgs
    ): AudioOptions {
        val audioLanguageOptions = sharedPrefRepository.getAudioLanguages()
        val audioIndexOptions =
            dataStoreRepository.audioTrackIndices.firstOrNull()?.get(dirPath)

        return when {
            audioIndexOptions != null -> AudioOptions.WithTrackId(audioIndexOptions)
            audioLanguageOptions != null -> {
                val languages =
                    audioLanguageOptions.split(AUDIO_LANGUAGE_SEPARATOR).map { it.trim() }
                AudioOptions.WithLanguages(languages)
            }
            else -> AudioOptions.WithTrackId()
        }
    }

    private suspend fun getSubsConfiguration(
        dirPath: String,
        videoNavArgs: VideoNavArgs
    ): SubtitleOptions {
        val subtitleLanguageOptions = sharedPrefRepository.getSubtitleLanguages()

        val subtitleIndexOptions =
            dataStoreRepository.subtitleTrackIndices.firstOrNull()?.get(dirPath)

        return when {
            subtitleIndexOptions != null -> SubtitleOptions.WithTrackId(subtitleIndexOptions)
            subtitleLanguageOptions != null -> {
                val languages =
                    subtitleLanguageOptions.split(SUBTITLE_LANGUAGE_SEPARATOR).map { it.trim() }
                SubtitleOptions.WithLanguages(languages)
            }
            else -> SubtitleOptions.WithLanguages()
        }
    }

    companion object {
        private const val SUB_EXTENSION = ".srt"
        private const val SUBS_FOLDER_NAME = "subs"
    }
}

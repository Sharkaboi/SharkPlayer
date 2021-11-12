package com.sharkaboi.sharkplayer.exoplayer.video.repo

import android.net.Uri
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.MimeTypes
import com.masterwok.opensubtitlesandroid.OpenSubtitlesUrlBuilder
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
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
                val subsList = mutableListOf<MediaItem.SubtitleConfiguration>()

                val sameNameSubUri = subOfVideoName(it, dirPath)
                if (sameNameSubUri != null) {
                    val mediaItemSubtitle =
                        MediaItem.SubtitleConfiguration.Builder(sameNameSubUri)
                            .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                            .setLanguage(null)
                            .setSelectionFlags(0)
                            .setRoleFlags(0)
                            .setLabel("Local - ${File(sameNameSubUri.toString()).name}")
                            .build()
                    subsList.add(mediaItemSubtitle)
                }

                val subsFolderSubUri = subOfVideoNameInSubsFolder(it, dirPath)
                if (subsFolderSubUri != null) {
                    val mediaItemSubtitle =
                        MediaItem.SubtitleConfiguration.Builder(subsFolderSubUri)
                            .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                            .setLanguage(null)
                            .setSelectionFlags(0)
                            .setRoleFlags(0)
                            .setLabel("Subs Folder - ${File(subsFolderSubUri.toString()).name}")
                            .build()
                    subsList.add(mediaItemSubtitle)
                }

                val openSubsUris = getOpenSubsUris(it, dirPath)
                Timber.d(openSubsUris.toString())
                openSubsUris.forEach { openSubsUri ->
                    val mediaItemSubtitle =
                        MediaItem.SubtitleConfiguration.Builder(openSubsUri)
                            .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                            .setLanguage(null)
                            .setSelectionFlags(0)
                            .setRoleFlags(0)
                            .setLabel("Open Subs - ${File(subsFolderSubUri.toString()).name}")
                            .build()
                    subsList.add(mediaItemSubtitle)
                }

                mediaItemBuilder.setSubtitleConfigurations(subsList)
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

    private suspend fun getOpenSubsUris(videoUri: String, dirPath: String): List<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                val videoFile = File(videoUri)
                val url = OpenSubtitlesUrlBuilder()
                    .query(videoFile.name)
                    .build()
                val service = OpenSubtitlesService()
                val searchResult: Array<OpenSubtitleItem> =
                    service.search(OpenSubtitlesService.TemporaryUserAgent, url)
                searchResult.map { it.SubDownloadLink.toUri() }.take(3)
            }.getOrDefault(emptyList())
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

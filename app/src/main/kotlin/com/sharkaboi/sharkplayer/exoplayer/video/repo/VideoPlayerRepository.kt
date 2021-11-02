package com.sharkaboi.sharkplayer.exoplayer.video.repo

import androidx.core.net.toUri
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

class VideoPlayerRepository(
    private val sharedPrefRepository: SharedPrefRepository,
    private val dataStoreRepository: DataStoreRepository,
) {
    suspend fun getMetaDataOf(videoNavArgs: VideoNavArgs): TaskState<VideoInfo> =
        tryCatching {
            val dirPath = videoNavArgs.dirPath
            val subtitleLanguageOptions = sharedPrefRepository.getSubtitleLanguages()
            val subtitleIndexOptions =
                dataStoreRepository.subtitleTrackIndices.firstOrNull()?.get(dirPath)
            val audioLanguageOptions = sharedPrefRepository.getAudioLanguages()
            val audioIndexOptions =
                dataStoreRepository.audioTrackIndices.firstOrNull()?.get(dirPath)
            val subtitleOptions: SubtitleOptions = when {
                subtitleIndexOptions != null -> SubtitleOptions.WithTrackId(subtitleIndexOptions)
                subtitleLanguageOptions != null -> {
                    val languages =
                        subtitleLanguageOptions.split(SUBTITLE_LANGUAGE_SEPARATOR).map { it.trim() }
                    SubtitleOptions.WithLanguages(languages)
                }
                else -> SubtitleOptions.WithTrackId()
            }
            val audioOptions: AudioOptions = when {
                audioIndexOptions != null -> AudioOptions.WithTrackId(audioIndexOptions)
                audioLanguageOptions != null -> {
                    val languages =
                        audioLanguageOptions.split(AUDIO_LANGUAGE_SEPARATOR).map { it.trim() }
                    AudioOptions.WithLanguages(languages)
                }
                else -> AudioOptions.WithTrackId()
            }
            val playWhenReady = sharedPrefRepository.shouldStartVideoPaused().not()
            TaskState.Success(
                VideoInfo(
                    videoUris = videoNavArgs.videoPaths.map(String::toUri),
                    subtitleOptions = subtitleOptions,
                    audioOptions = audioOptions,
                    playWhenReady = playWhenReady
                )
            )
        }
}

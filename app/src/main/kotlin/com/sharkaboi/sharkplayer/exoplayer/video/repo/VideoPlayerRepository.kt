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
import kotlinx.coroutines.flow.firstOrNull

class VideoPlayerRepository(
    private val sharedPrefRepository: SharedPrefRepository,
    private val dataStoreRepository: DataStoreRepository,
) {
    suspend fun getMetaDataOf(path: String): TaskState<VideoInfo> =
        tryCatching {
            val subtitleLanguageOptions = sharedPrefRepository.getSubtitleLanguages()
            val subtitleIndexOptions =
                dataStoreRepository.subtitleTrackIndices.firstOrNull()?.get(path)
            val audioLanguageOptions = sharedPrefRepository.getAudioLanguages()
            val audioIndexOptions =
                dataStoreRepository.audioTrackIndices.firstOrNull()?.get(path)
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
            TaskState.Success(
                VideoInfo(
                    videoUri = path.toUri(),
                    subtitleOptions = subtitleOptions,
                    audioOptions = audioOptions
                )
            )
        }
}

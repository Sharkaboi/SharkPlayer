package com.sharkaboi.sharkplayer.exoplayer.audio.repo

import androidx.core.net.toUri
import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.exoplayer.audio.model.AudioInfo
import java.io.File

class AudioPlayerRepository(
    private val sharedPrefRepository: SharedPrefRepository,
    private val dataStoreRepository: DataStoreRepository,
) {
    suspend fun getMetaDataOf(audioPath: String): TaskState<AudioInfo> =
        tryCatching {
            val playWhenReady = sharedPrefRepository.shouldStartAudioPaused().not()
            TaskState.Success(
                AudioInfo(
                    name = File(audioPath).nameWithoutExtension,
                    audioUri = audioPath.toUri(),
                    playWhenReady = playWhenReady
                )
            )
        }
}

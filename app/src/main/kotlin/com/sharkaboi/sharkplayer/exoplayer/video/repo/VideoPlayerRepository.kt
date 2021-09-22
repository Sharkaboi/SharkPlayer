package com.sharkaboi.sharkplayer.exoplayer.video.repo

import androidx.core.net.toUri
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo

class VideoPlayerRepository(
    private val sharedPrefRepository: SharedPrefRepository
) {
    fun getMetaDataOf(path: String): TaskState<VideoInfo> {
        return try {
            // TODO: 13-09-2021 load sub/audio options by priority and get id
            TaskState.Success(
                VideoInfo(
                    videoUri = path.toUri(),
                    subtitleOptions = SubtitleOptions.DefaultTrack,
                    audioOptions = AudioOptions.DefaultTrack
                )
            )
        } catch (e: Exception) {
            TaskState.Failure(e)
        }
    }
}

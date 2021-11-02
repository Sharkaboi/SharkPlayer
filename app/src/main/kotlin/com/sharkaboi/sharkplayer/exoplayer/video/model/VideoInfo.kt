package com.sharkaboi.sharkplayer.exoplayer.video.model

import android.net.Uri
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions

data class VideoInfo(
    val videoUris: List<Uri>,
    val subtitleOptions: SubtitleOptions,
    val audioOptions: AudioOptions,
    val playWhenReady: Boolean
)

package com.sharkaboi.sharkplayer.exoplayer.video.model

import com.google.android.exoplayer2.MediaItem
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions

data class VideoInfo(
    val videoMediaItems: List<MediaItem>,
    val subtitleOptions: SubtitleOptions,
    val audioOptions: AudioOptions,
    val playWhenReady: Boolean
)

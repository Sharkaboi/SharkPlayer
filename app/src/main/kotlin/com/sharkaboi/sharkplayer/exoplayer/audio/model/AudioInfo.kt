package com.sharkaboi.sharkplayer.exoplayer.audio.model

import android.net.Uri

data class AudioInfo(
    val name: String,
    val audioUri: Uri,
    val playWhenReady: Boolean
)

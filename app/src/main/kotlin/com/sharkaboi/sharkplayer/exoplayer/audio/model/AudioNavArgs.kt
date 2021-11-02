package com.sharkaboi.sharkplayer.exoplayer.audio.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioNavArgs(
    val dirPath: String,
    val videoPaths: List<String>,
) : Parcelable

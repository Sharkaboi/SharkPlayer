package com.sharkaboi.sharkplayer.exoplayer.video.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoNavArgs(
    val dirPath: String,
    val videoPaths: List<String>,
) : Parcelable

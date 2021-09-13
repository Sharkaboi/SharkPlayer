package com.sharkaboi.sharkplayer.exoplayer.util

data class AudioOptions(
    val audioTrackId: Int
) {
    companion object {
        val DefaultTrack = AudioOptions(-1)
    }
}

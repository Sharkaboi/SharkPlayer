package com.sharkaboi.sharkplayer.exoplayer.util

data class SubtitleOptions(
    val trackId: Int
) {
    companion object {
        val DefaultTrack = SubtitleOptions(-1)
    }
}

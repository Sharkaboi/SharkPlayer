package com.sharkaboi.sharkplayer.exoplayer.util

internal const val DEFAULT_SUBTITLE_TRACK = -1
internal const val DEFAULT_SUBTITLE_LANGUAGE = "eng"
internal const val SUBTITLE_LANGUAGE_SEPARATOR = ","

sealed class SubtitleOptions {

    data class WithTrackId(
        val trackId: Int = DEFAULT_SUBTITLE_TRACK
    ) : SubtitleOptions()

    data class WithLanguages(
        val languages: List<String> = listOf(DEFAULT_SUBTITLE_LANGUAGE)
    ) : SubtitleOptions()
}

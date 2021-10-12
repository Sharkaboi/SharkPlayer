package com.sharkaboi.sharkplayer.exoplayer.util

internal const val DEFAULT_AUDIO_TRACK = -1
internal const val DEFAULT_AUDIO_LANGUAGE = "eng"
internal const val AUDIO_LANGUAGE_SEPARATOR = ","

sealed class AudioOptions {

    data class WithTrackId(
        val trackId: Int = DEFAULT_AUDIO_TRACK
    ) : AudioOptions()

    data class WithLanguages(
        val languages: List<String> = listOf(DEFAULT_AUDIO_LANGUAGE)
    ) : AudioOptions()
}

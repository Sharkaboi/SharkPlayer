package com.sharkaboi.sharkplayer.common.extensions

import android.media.MediaMetadataRetriever
import java.io.File
import kotlin.time.Duration

internal const val SIZE_NOT_A_FILE = -1L

internal val File.childCount: Int
    get() = this.list()?.size ?: 0

internal val File.isVideoFile: Boolean
    get() {
        val supportedFileRegex = "mp4|webm|mkv|flv".toRegex(RegexOption.IGNORE_CASE)
        return this.isFile && this.extension.matches(supportedFileRegex)
    }

internal val File.isAudioFile: Boolean
    get() {
        val supportedFileRegex = "m4a|mp3|ogg|wav|flac|opus".toRegex(RegexOption.IGNORE_CASE)
        return this.isFile && this.extension.matches(supportedFileRegex)
    }

internal val File.fileSize: Long
    get() = when {
        this.isFile -> this.length()
        else -> SIZE_NOT_A_FILE
    }

internal val File.videoOrAudioLength: Duration
    get() = runCatching {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(absolutePath)
        val lengthInMilliSeconds =
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong() ?: 0L
        metadataRetriever.release()
        return Duration.milliseconds(lengthInMilliSeconds)
    }.getOrElse { Duration.ZERO }

internal val File.videoResolution: Pair<Int, Int>
    get() = runCatching {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(absolutePath)
        val videoHeight =
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toInt() ?: 0
        val videoWidth =
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toInt() ?: 0
        metadataRetriever.release()
        return Pair(videoHeight, videoWidth)
    }.getOrElse { Pair(0, 0) }

internal val File.audioBitrate: Long
    get() = runCatching {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(absolutePath)
        val bitsPerSecond =
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toLong() ?: 0L
        metadataRetriever.release()
        return bitsPerSecond / 1024
    }.getOrElse { 0L }


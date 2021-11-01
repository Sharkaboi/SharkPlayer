package com.sharkaboi.sharkplayer.common.models

import android.os.Environment
import com.sharkaboi.sharkplayer.common.extensions.*
import java.io.File
import kotlin.time.Duration

sealed class SharkPlayerFile {
    data class Directory(
        val folderName: String,
        val path: String,
        val childFileCount: Int
    ) : SharkPlayerFile()

    data class VideoFile(
        val fileName: String,
        val path: String,
        val length: Duration,
        val size: Long,
        val videoWidth: Int,
        val videoHeight: Int,
    ) : SharkPlayerFile() {
        val resolution: String
            get() = "$videoWidth x $videoHeight"

        val isDirty: Boolean
            get() = path.isBlank()
                    || size <= 0L
                    || !length.isPositive()
                    || size <= 0L
                    || videoWidth <= 0L
                    || videoHeight <= 0L
    }

    data class AudioFile(
        val fileName: String,
        val path: String,
        val size: Long,
        val length: Duration,
        val bitRate: Long
    ) : SharkPlayerFile() {

        val quality: String
            get() = "$bitRate kbps"

        val isDirty: Boolean
            get() = path.isBlank()
                    || size <= 0L
                    || !length.isPositive()
                    || bitRate <= 0L
    }

    data class OtherFile(
        val fileName: String,
        val path: String,
        val size: Long
    ) : SharkPlayerFile()

    val absolutePath: String
        get() {
            return when (this) {
                is Directory -> this.path
                is VideoFile -> this.path
                is AudioFile -> this.path
                is OtherFile -> this.path
            }
        }

    val sortField: String
        get() {
            return when (this) {
                is Directory -> this.folderName.lowercase()
                is VideoFile -> this.fileName.lowercase()
                is AudioFile -> this.fileName.lowercase()
                is OtherFile -> this.fileName.lowercase()
            }
        }

    fun getFile(): File {
        return when (this) {
            is Directory -> File(this.path)
            is VideoFile -> File(this.path)
            is AudioFile -> File(this.path)
            is OtherFile -> File(this.path)
        }
    }

    val parentPath
        get() = this.absolutePath.substringBeforeLast(File.separator)

    companion object {
        fun directoryFromPath(path: String?): Directory {
            val requiredPath = path ?: Environment.getRootDirectory().absolutePath
            return File(requiredPath).toSharkPlayerDirectory()
        }
    }
}

internal fun File.toSharkPlayerFile(): SharkPlayerFile {
    return when {
        this.isDirectory -> this.toSharkPlayerDirectory()
        this.isVideoFile -> this.toSharkPlayerVideoFile()
        this.isAudioFile -> this.toSharkPlayerAudioFile()
        else -> this.toSharkPlayerOtherFile()
    }
}

private fun File.toSharkPlayerDirectory(): SharkPlayerFile.Directory {
    return SharkPlayerFile.Directory(
        folderName = this.name,
        path = this.absolutePath,
        childFileCount = this.childCount
    )
}

private fun File.toSharkPlayerVideoFile(): SharkPlayerFile.VideoFile {
    val (height, width) = this.videoResolution
    return SharkPlayerFile.VideoFile(
        fileName = this.nameWithoutExtension,
        path = this.absolutePath,
        videoHeight = height,
        videoWidth = width,
        length = this.videoOrAudioLength,
        size = this.fileSize
    )
}

private fun File.toSharkPlayerAudioFile(): SharkPlayerFile.AudioFile {
    return SharkPlayerFile.AudioFile(
        fileName = this.nameWithoutExtension,
        path = this.absolutePath,
        bitRate = this.audioBitrate,
        length = this.videoOrAudioLength,
        size = this.fileSize
    )
}

private fun File.toSharkPlayerOtherFile(): SharkPlayerFile.OtherFile {
    return SharkPlayerFile.OtherFile(
        fileName = this.name,
        path = this.absolutePath,
        size = this.fileSize
    )
}
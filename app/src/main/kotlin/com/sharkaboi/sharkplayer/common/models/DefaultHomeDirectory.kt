package com.sharkaboi.sharkplayer.common.models

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

sealed class SharkPlayerFile {
    data class Directory(
        val folderName: String,
        val path: String,
        val childFileCount: Int
    ) : SharkPlayerFile()

    data class MediaFile(
        val fileName: String,
        val path: String,
        val length: Duration,
        val quality: String
    ) : SharkPlayerFile()

    data class OtherFile(
        val fileName: String,
        val path: String,
        val size: Long
    ) : SharkPlayerFile()

    fun getIdentifier(): String {
        return when(this){
            is Directory -> this.path
            is MediaFile -> this.path
            is OtherFile -> this.path
        }
    }
}

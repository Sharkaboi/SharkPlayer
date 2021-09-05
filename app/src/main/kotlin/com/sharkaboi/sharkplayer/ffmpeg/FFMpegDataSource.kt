package com.sharkaboi.sharkplayer.ffmpeg

import com.sharkaboi.sharkplayer.common.util.TaskState

interface FFMpegDataSource {
    val isRunning: Boolean
    suspend fun loadBinary(): TaskState<Unit>
    suspend fun execute(command: Array<String>): TaskState<String>
    fun killProcess(): Boolean
    fun setTimeout(timeout: Long)
}
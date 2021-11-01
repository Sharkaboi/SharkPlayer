package com.sharkaboi.sharkplayer.ffmpeg.sources

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.ffmpeg.FFMpegDataSource
import com.sharkaboi.sharkplayer.ffmpeg.command.FFMpegCommand
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidFFmpegDataSource(
    private val ffmpeg: FFmpeg
) : FFMpegDataSource {
    override val isRunning: Boolean get() = ffmpeg.isFFmpegCommandRunning

    override suspend fun loadBinary(): TaskState<Unit> {
        return suspendCoroutine { continuation ->
            try {
                ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                    override fun onFailure() {
                        continuation.resume(TaskState.failureWithMessage("FFmpeg failure"))
                    }

                    override fun onSuccess() {
                        continuation.resume(TaskState.Success(Unit))
                    }
                })
            } catch (e: FFmpegNotSupportedException) {
                continuation.resume(TaskState.Failure(e))
            }
        }
    }

    override fun killProcess(): Boolean = ffmpeg.killRunningProcesses()

    override fun setTimeout(timeout: Long) = ffmpeg.setTimeout(timeout)

    override suspend fun execute(
        command: FFMpegCommand
    ): TaskState<String> {
        return suspendCoroutine { continuation ->
            try {
                ffmpeg.execute(command, object : ExecuteBinaryResponseHandler() {
                    override fun onStart() {}

                    override fun onProgress(message: String) {
                        Timber.d(message)
                    }

                    override fun onFailure(message: String) {
                        continuation.resume(TaskState.failureWithMessage(message))
                    }

                    override fun onSuccess(message: String) {
                        continuation.resume(TaskState.Success(message))
                    }

                    override fun onFinish() {}
                })
            } catch (e: FFmpegNotSupportedException) {
                continuation.resume(TaskState.Failure(e))
            }
        }
    }
}
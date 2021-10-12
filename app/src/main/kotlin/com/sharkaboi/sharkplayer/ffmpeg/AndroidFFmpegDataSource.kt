package com.sharkaboi.sharkplayer.ffmpeg

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.sharkaboi.sharkplayer.common.util.TaskState
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidFFmpegDataSource(
    private val fFmpeg: FFmpeg
) : FFMpegDataSource {
    override val isRunning: Boolean get() = fFmpeg.isFFmpegCommandRunning

    override suspend fun loadBinary(): TaskState<Unit> {
        return suspendCoroutine { continuation ->
            try {
                fFmpeg.loadBinary(object : LoadBinaryResponseHandler() {
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

    override fun killProcess(): Boolean = fFmpeg.killRunningProcesses()

    override fun setTimeout(timeout: Long) = fFmpeg.setTimeout(timeout)

    override suspend fun execute(command: Array<String>): TaskState<String> {
        return suspendCoroutine { continuation ->
            try {
                fFmpeg.execute(command, object : ExecuteBinaryResponseHandler() {
                    override fun onStart() {}
                    override fun onProgress(message: String) {}
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
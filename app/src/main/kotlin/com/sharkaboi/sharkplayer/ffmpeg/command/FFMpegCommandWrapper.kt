package com.sharkaboi.sharkplayer.ffmpeg.command

import com.sharkaboi.sharkplayer.common.extensions.nextEvenInt
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import java.io.File

typealias FFMpegCommand = Array<String>

object FFMpegCommandWrapper {
    fun rescaleVideo(
        videoFile: SharkPlayerFile.VideoFile,
        targetResolution: String
    ): FFMpegCommand {
        val dirPath = videoFile.parentPath
        val outputPath = buildString {
            append(dirPath)
            append(File.separator)
            append(videoFile.fileName)
            append("_$targetResolution")
            append("_${System.currentTimeMillis()}")
            append('.')
            append(videoFile.getFile().extension)
        }

        val outputHeight = targetResolution.trimEnd('p').toInt()
        val inputWidth = videoFile.videoWidth.toFloat()
        val inputHeight = videoFile.videoHeight.toFloat()
        val outputWidth = (inputWidth / (inputHeight / outputHeight)).nextEvenInt()

        // ffmpeg -i input.mp4 -vcodec libx264 -crf 27 -preset veryfast -c:a copy -s 960x540 output.mp4
        return arrayOf(
            "-i",
            videoFile.path,
            "-vcodec",
            "libx264",
            "-crf",
            "27",
            "-preset",
            "veryfast",
            "-c:a",
            "copy",
            "-s",
            "${outputWidth}x$outputHeight",
            outputPath
        )
    }
}
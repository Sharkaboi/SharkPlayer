package com.sharkaboi.sharkplayer.common.extensions

import android.widget.ImageView
import coil.load
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile

fun ImageView.setThumbnailOf(
    videoFile: SharkPlayerFile.VideoFile,
    block: ImageRequest.Builder.() -> Unit = {}
) {
    this.load(videoFile.getFile()) {
        videoFrameMillis(videoFile.length.inWholeMilliseconds / 2)
        block()
    }
}
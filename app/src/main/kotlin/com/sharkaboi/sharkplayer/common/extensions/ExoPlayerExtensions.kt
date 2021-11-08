package com.sharkaboi.sharkplayer.common.extensions

import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

internal fun ExoPlayer.setVideosAsPlayList(mediaItems: List<MediaItem>) {
    clearMediaItems()
    setMediaItems(mediaItems, true)
}

internal fun ExoPlayer.setAudio(audioUri: Uri) {
    setMediaItem(MediaItem.fromUri(audioUri))
}
package com.sharkaboi.sharkplayer.exoplayer.video.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.navigation.navArgs
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.sharkaboi.sharkplayer.databinding.ActivityVideoPlayerBinding
import java.io.File

class VideoPlayerActivity : AppCompatActivity() {
    private val args: VideoPlayerActivityArgs by navArgs()
    private lateinit var binding: ActivityVideoPlayerBinding
    private lateinit var player: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        player = SimpleExoPlayer.Builder(this).build()
        binding.playerView.player = player
        binding.playerView.setShowSubtitleButton(true)
        val mediaItem: MediaItem = MediaItem.fromUri(File(args.path).toUri())
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun onDestroy() {
        binding.playerView.player = null
        player.release()
        super.onDestroy()
    }
}
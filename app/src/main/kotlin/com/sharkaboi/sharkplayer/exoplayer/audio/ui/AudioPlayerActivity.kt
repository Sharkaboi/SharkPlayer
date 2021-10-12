package com.sharkaboi.sharkplayer.exoplayer.audio.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.navigation.navArgs
import coil.load
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.databinding.ActivityAudioPlayerBinding
import java.io.File

class AudioPlayerActivity : AppCompatActivity() {
    private val args: AudioPlayerActivityArgs by navArgs()
    private lateinit var binding: ActivityAudioPlayerBinding
    private lateinit var player: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        player = SimpleExoPlayer.Builder(this).build()
        binding.playerView.player = player
        val mediaItem: MediaItem = MediaItem.fromUri(File(args.path).toUri())
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        if (player.mediaMetadata.artworkUri == null) {
            binding.ivAudioPlaceholder.isVisible = true
            binding.ivAudioPlaceholder.load(R.drawable.ic_audio_track)
        }
    }

    override fun onDestroy() {
        binding.playerView.player = null
        player.release()
        super.onDestroy()
    }
}
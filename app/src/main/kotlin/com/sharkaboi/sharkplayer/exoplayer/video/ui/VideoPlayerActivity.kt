package com.sharkaboi.sharkplayer.exoplayer.video.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.common.extensions.setVideosAsPlayList
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.databinding.ActivityVideoPlayerBinding
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo
import com.sharkaboi.sharkplayer.exoplayer.video.vm.VideoPlayBackState
import com.sharkaboi.sharkplayer.exoplayer.video.vm.VideoPlayerState
import com.sharkaboi.sharkplayer.exoplayer.video.vm.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private lateinit var player: SimpleExoPlayer
    private val videoPlayerViewModel by viewModels<VideoPlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setObservers()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // TODO: 11-10-2021 add support for video pan
        super.onConfigurationChanged(newConfig)
    }

    private fun initViews() {
        WindowCompat.setDecorFitsSystemWindows(window,false)
//        binding.playerView.setShowSubtitleButton(true)
    }

    private fun setObservers() {
        observe(videoPlayerViewModel.uiState) { state ->
            binding.progress.isVisible = state is VideoPlayerState.Loading
            when (state) {
                is VideoPlayerState.InvalidData -> showToast(state.message)
                is VideoPlayerState.Success -> handleMetaDataUpdate(state.videoInfo)
                else -> Unit
            }
        }
        observe(videoPlayerViewModel.playbackState, ::handlePlaybackUpdate)

    }

    private fun handleMetaDataUpdate(videoInfo: VideoInfo) {
        val trackSelector = DefaultTrackSelector(this)
        val builder = trackSelector.buildUponParameters()
        when (videoInfo.subtitleOptions) {
            is SubtitleOptions.WithLanguages -> {
                builder.setPreferredTextLanguages(*videoInfo.subtitleOptions.languages.toTypedArray())
            }
            is SubtitleOptions.WithTrackId -> {
                // TODO: 25-09-2021
                //builder.setSelectionOverride()
            }
        }
        when (videoInfo.audioOptions) {
            is AudioOptions.WithLanguages -> {
                builder.setPreferredAudioLanguages(*videoInfo.audioOptions.languages.toTypedArray())
            }
            is AudioOptions.WithTrackId -> {
                // TODO: 25-09-2021
                //builder.setSelectionOverride()
            }
        }
        trackSelector.setParameters(builder)
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        binding.playerView.player = player
        player.setVideosAsPlayList(videoInfo.videoUris)
        player.prepare()
        player.play()
    }

    private fun handlePlaybackUpdate(videoPlayBackState: VideoPlayBackState) {
        // TODO: 25-09-2021
    }

    override fun onDestroy() {
        binding.playerView.player = null
        player.release()
        super.onDestroy()
    }
}
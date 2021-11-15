package com.sharkaboi.sharkplayer.exoplayer.video.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.MimeTypes
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.common.extensions.setVideosAsPlayList
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.databinding.ActivityVideoPlayerBinding
import com.sharkaboi.sharkplayer.exoplayer.download_sub.DownloadSubDialog
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo
import com.sharkaboi.sharkplayer.exoplayer.video.vm.VideoPlayerState
import com.sharkaboi.sharkplayer.exoplayer.video.vm.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File


@AndroidEntryPoint
class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null
    private val videoPlayerViewModel by viewModels<VideoPlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setObservers()
    }

    private fun initViews() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
        observe(videoPlayerViewModel.downloadedSubUri) { uri ->
            if (uri == null) {
                return@observe
            }

            val currentMediaItem = player?.currentMediaItem ?: return@observe

            player?.let {
                val currentIndex = it.currentMediaItemIndex
                it.removeMediaItem(currentIndex)
                val subConfiguration =
                    MediaItem.SubtitleConfiguration.Builder(uri)
                        .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .setRoleFlags(C.ROLE_FLAG_SUBTITLE)
                        .setLabel("Downloads - ${File(uri.toString()).name}")
                        .build()
                val mergedMediaItem = currentMediaItem.buildUpon()
                    .setSubtitleConfigurations(
                        currentMediaItem.localConfiguration?.subtitleConfigurations.orEmpty()
                            .plus(subConfiguration)
                    ).build()
                Timber.d(mergedMediaItem.toString())
                it.addMediaItem(mergedMediaItem)
            }
        }
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
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        binding.playerView.player = player
        player?.setVideosAsPlayList(videoInfo.videoMediaItems)
        player?.prepare()
        player?.playWhenReady = videoInfo.playWhenReady
        player?.addListener(playListListener)
        updateFileNameOf(player?.currentMediaItem)
        setDownloadListener()
    }

    private fun setDownloadListener() {
        val btnDownloadSub = binding.playerView.findViewById<View>(R.id.exo_download_sub)
        btnDownloadSub?.let {
            it.setOnClickListener {
                openDownloadSubDialog()
            }
        }
    }

    private fun openDownloadSubDialog() {
        DownloadSubDialog().show(supportFragmentManager, DownloadSubDialog::class.simpleName)
    }

    private val playListListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            updateFileNameOf(mediaItem)
        }
    }

    private fun updateFileNameOf(mediaItem: MediaItem?) {
        val tvFileName = binding.playerView.findViewById<TextView>(R.id.exo_video_file_name)
        tvFileName?.isSelected = true
        tvFileName?.text =
            mediaItem?.localConfiguration?.uri?.path?.let { File(it).nameWithoutExtension }
    }

    override fun onDestroy() {
        binding.playerView.player = null
        player?.removeListener(playListListener)
        player?.release()
        super.onDestroy()
    }
}
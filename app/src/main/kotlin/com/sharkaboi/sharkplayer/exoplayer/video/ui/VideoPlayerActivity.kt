package com.sharkaboi.sharkplayer.exoplayer.video.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverrides
import com.google.android.exoplayer2.util.EventLogger
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


@AndroidEntryPoint
class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var playListListener: Player.Listener? = null
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
        setLockOrientationListener()
    }

    private fun setLockOrientationListener() {
        val btnLockOrientation = findViewById<View>(R.id.exo_lock_orientation)
        btnLockOrientation.setOnClickListener {
            toggleLockedOrientation()
        }
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

            loadDownloadedSubOnto(currentMediaItem, uri)
        }
    }

    private fun loadDownloadedSubOnto(currentMediaItem: MediaItem, uri: Uri) {
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
            it.addMediaItem(currentIndex, mergedMediaItem)
            it.play()
        }
    }

    private fun handleMetaDataUpdate(videoInfo: VideoInfo) =
        lifecycleScope.launch(Dispatchers.Main) {
            trackSelector = DefaultTrackSelector(this@VideoPlayerActivity)
            val builder = trackSelector!!.buildUponParameters()
            when (videoInfo.subtitleOptions) {
                is SubtitleOptions.WithLanguages -> {
                    builder?.setPreferredTextLanguages(*videoInfo.subtitleOptions.languages.toTypedArray())
                }
                else -> Unit
            }
            when (videoInfo.audioOptions) {
                is AudioOptions.WithLanguages -> {
                    builder?.setPreferredAudioLanguages(*videoInfo.audioOptions.languages.toTypedArray())
                }
                else -> Unit
            }
            trackSelector?.setParameters(builder)
            player = ExoPlayer.Builder(this@VideoPlayerActivity)
                .setTrackSelector(trackSelector!!)
                .build()
            binding.playerView.player = player
            player?.setVideosAsPlayList(videoInfo.videoMediaItems)
            player?.prepare()
            setSubErrorHandler()
            player?.playWhenReady = videoInfo.playWhenReady
            player?.addListener(getPlayListListener(videoInfo)!!)
            player?.addAnalyticsListener(EventLogger(trackSelector))
            updateFileNameOf(player?.currentMediaItem)
            setDownloadListener()
        }

    private fun setSubErrorHandler() {
        player?.setErrorCallback {
            showToast("Corrupted subtitle file from open subtitles")
            videoPlayerViewModel.reloadVideo()
        }
    }

    private fun getPlayListListener(videoInfo: VideoInfo): Player.Listener? {
        playListListener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                updateFileNameOf(mediaItem)
            }

            override fun onTracksInfoChanged(tracksInfo: TracksInfo) {
                setTrackSelectionOptions(tracksInfo, videoInfo)
                Timber.d("onTracksInfoChanged called")
                super.onTracksInfoChanged(tracksInfo)
            }
        }
        return playListListener
    }

    private fun setTrackSelectionOptions(tracksInfo: TracksInfo?, videoInfo: VideoInfo) {
        val trackSelectorBuilder = player?.trackSelectionParameters?.buildUpon()
        val overrideBuilder = TrackSelectionOverrides.Builder()
        if (videoInfo.subtitleOptions is SubtitleOptions.WithTrackId) {
            val selectedTrackId = videoInfo.subtitleOptions.trackId

            val subGroups =
                tracksInfo?.trackGroupInfos?.filter { it.trackType == C.TRACK_TYPE_TEXT }
            Timber.d(subGroups.toString())
            val subGroup = subGroups?.map { it.trackGroup }?.getOrNull(selectedTrackId)
            Timber.d(subGroup.toString())
            if (subGroup != null && subGroup.length > 0) {
                Timber.d("Set to override sub at index $selectedTrackId")
                overrideBuilder
                    .setOverrideForType(
                        TrackSelectionOverrides.TrackSelectionOverride(
                            subGroup,
                            listOf(0)
                        )
                    )
            } else {
                Timber.d("Removed sub overrides as index - $selectedTrackId group - $subGroup")
                overrideBuilder.clearOverridesOfType(C.TRACK_TYPE_TEXT)
            }
        }

        if (videoInfo.audioOptions is AudioOptions.WithTrackId) {
            val selectedTrackId = videoInfo.audioOptions.trackId

            val audioGroups =
                tracksInfo?.trackGroupInfos?.filter { it.trackType == C.TRACK_TYPE_AUDIO }
            val audioGroup = audioGroups?.map { it.trackGroup }?.getOrNull(selectedTrackId)
            if (audioGroup != null && audioGroup.length > 0) {
                Timber.d("Set to override audio at index $selectedTrackId")
                overrideBuilder
                    .setOverrideForType(
                        TrackSelectionOverrides.TrackSelectionOverride(
                            audioGroup,
                            listOf(0)
                        )
                    )
            } else {
                Timber.d("Removed audio overrides as index - $selectedTrackId group - $audioGroup")
                overrideBuilder
                    .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
            }
        }

        trackSelectorBuilder?.setTrackSelectionOverrides(overrideBuilder.build())

        trackSelectorBuilder?.build()?.let {
            player?.trackSelectionParameters = it
        }
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

    private fun updateFileNameOf(mediaItem: MediaItem?) {
        val tvFileName = binding.playerView.findViewById<TextView>(R.id.exo_video_file_name)
        tvFileName?.isSelected = true
        tvFileName?.text =
            mediaItem?.localConfiguration?.uri?.path?.let { File(it).nameWithoutExtension }
    }

    override fun onDestroy() {
        resetPlayer()
        super.onDestroy()
    }

    private fun resetPlayer() {
        binding.playerView.player = null
        playListListener?.let { player?.removeListener(it) }
        player?.release()
        player?.cleanErrorCallback()
    }

    var isOrientationLocked = false
    private fun toggleLockedOrientation() {
        if (isOrientationLocked) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            when (resources.configuration.orientation) {
                ORIENTATION_PORTRAIT -> setPortraitMode()
                ORIENTATION_LANDSCAPE -> setLandScapeMode()
                else -> setLandScapeMode()
            }
        }
        isOrientationLocked = !isOrientationLocked
        val btnLockOrientation = findViewById<ImageView>(R.id.exo_lock_orientation)
        btnLockOrientation?.load(
            if (isOrientationLocked) R.drawable.ic_locked_rotation else R.drawable.ic_lock_rotation
        )
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setPortraitMode() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setLandScapeMode(orientation: Int = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        requestedOrientation = orientation
    }
}
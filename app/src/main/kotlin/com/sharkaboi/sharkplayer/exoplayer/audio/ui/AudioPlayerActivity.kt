package com.sharkaboi.sharkplayer.exoplayer.audio.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.common.extensions.setAudio
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.databinding.ActivityAudioPlayerBinding
import com.sharkaboi.sharkplayer.exoplayer.audio.model.AudioInfo
import com.sharkaboi.sharkplayer.exoplayer.audio.vm.AudioPlayerState
import com.sharkaboi.sharkplayer.exoplayer.audio.vm.AudioPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AudioPlayerActivity : AppCompatActivity() {
    private val args: AudioPlayerActivityArgs by navArgs()
    private lateinit var binding: ActivityAudioPlayerBinding
    private var player: ExoPlayer? = null
    private val audioPlayerViewModel by viewModels<AudioPlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setObservers()
        initViews()
        setObservers()
    }

    private fun initViews() {
        //Nothing
    }

    private fun setObservers() {
        observe(audioPlayerViewModel.uiState) { state ->
            binding.progress.isVisible = state is AudioPlayerState.Loading
            when (state) {
                is AudioPlayerState.InvalidData -> showToast(state.message)
                is AudioPlayerState.Success -> {
                    handleMetaDataUpdate(state.audioInfo)
                }
                else -> Unit
            }
        }
    }

    private fun handleMetaDataUpdate(audioInfo: AudioInfo) =
        lifecycleScope.launch(Dispatchers.Main) {
            Timber.d("called")
            resetPlayer()
            player = ExoPlayer.Builder(this@AudioPlayerActivity).build()
            binding.playerView.player = player
            player?.setAudio(audioInfo.audioUri)
            player?.prepare()
//            player?.addAnalyticsListener(EventLogger(player?.trackSelector as MappingTrackSelector?))
            player?.playWhenReady = audioInfo.playWhenReady
            binding.tvFileName.isSelected = true
            binding.tvFileName.text = audioInfo.name
        }

    private fun resetPlayer() {
        binding.playerView.player = null
        player?.release()
        player?.cleanErrorCallback()
    }

    override fun onDestroy() {
        resetPlayer()
        super.onDestroy()
    }
}
package com.sharkaboi.sharkplayer.exoplayer.download_sub

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sharkaboi.sharkplayer.common.extensions.debounce
import com.sharkaboi.sharkplayer.common.extensions.initLinearDefaults
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.databinding.DialogDownloadSubBinding
import com.sharkaboi.sharkplayer.exoplayer.video.vm.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class DownloadSubDialog : BottomSheetDialogFragment() {
    private lateinit var adapter: DownloadSubsAdapter
    private var _binding: DialogDownloadSubBinding? = null
    private val binding get() = _binding!!
    private val downloadSubViewModel by viewModels<DownloadSubViewModel>()
    private val videoPlayerViewModel by activityViewModels<VideoPlayerViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDownloadSubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSubs.adapter = null
        _binding = null
    }

    private fun setListeners() {
        val debounce = debounce<CharSequence?>(
            scope = lifecycleScope,
            delay = 500L
        ) {
            downloadSubViewModel.searchSubs(it)
        }
        binding.etDownloadSub.doOnTextChanged { text, _, _, _ ->
            debounce(text)
        }
    }

    private fun setObservers() {
        observe(downloadSubViewModel.subs) { list ->
            val rvSubs = binding.rvSubs
            adapter = DownloadSubsAdapter {
                downloadSubViewModel.downloadSub(it)
            }
            rvSubs.adapter = adapter
            rvSubs.initLinearDefaults(context)
            adapter.submitList(list)
        }
        observe(downloadSubViewModel.downloadedSubUri) { uri ->
            if (uri == null) {
                return@observe
            }

            Timber.d(videoPlayerViewModel.toString())
            videoPlayerViewModel.setDownloadedSubUri(uri)
            dismiss()
        }
    }
}
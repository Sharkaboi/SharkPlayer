package com.sharkaboi.sharkplayer.modules.directory.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sharkaboi.sharkplayer.BottomNavGraphDirections
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.extensions.initLinearDefaults
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.FragmentDirectoryBinding
import com.sharkaboi.sharkplayer.modules.directory.adapters.DirectoryAdapter
import com.sharkaboi.sharkplayer.modules.directory.vm.DirectoryState
import com.sharkaboi.sharkplayer.modules.directory.vm.DirectoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DirectoryFragment : Fragment() {
    private var _binding: FragmentDirectoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var directoryAdapter: DirectoryAdapter
    private val navController by lazy { findNavController() }
    private val directoryViewModel by viewModels<DirectoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvDirectories.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setListeners()
        setObservers()
    }

    private fun initViews() {
        setupTitle()
        setupBackButton()
        setupPathTextView()
        setupRecyclerView()
    }

    private fun setupTitle() {
        binding.toolbar.title = directoryViewModel.selectedDir.folderName
    }

    private fun setupBackButton() {
        binding.toolbar.setNavigationOnClickListener { navController.navigateUp() }
    }

    private fun setupPathTextView() {
        binding.tvPath.text = directoryViewModel.selectedDir.path
    }

    private fun setupRecyclerView() {
        val rvDirectories = binding.rvDirectories
        directoryAdapter = DirectoryAdapter { file ->
            navigateToFile(file)
        }
        rvDirectories.adapter = directoryAdapter
        rvDirectories.initLinearDefaults(context, hasFixedSize = true)
    }

    private fun setObservers() {
        observe(directoryViewModel.uiState) { state ->
            binding.progress.isVisible = state is DirectoryState.Loading
            when (state) {
                is DirectoryState.Failure -> showToast(state.message)
                is DirectoryState.LoadSuccess -> {
                    binding.tvEmptyHint.isVisible = state.files.isEmpty()
                    directoryAdapter.submitList(state.files)
                }
                else -> Unit
            }
        }
        observe(directoryViewModel.isFavorite) { isFavorite ->
            // TODO: 07-09-2021 set icon based on favorite setting of folder
        }
    }

    private fun setListeners() {
        binding.fabPlay.setOnClickListener { openAsPlaylist() }
    }

    private fun navigateToFile(file: SharkPlayerFile) {
        when (file) {
            is SharkPlayerFile.AudioFile -> Unit //TODO: play audio file
            is SharkPlayerFile.Directory -> openDirectory(file)
            is SharkPlayerFile.OtherFile -> showToast(R.string.unsupported_file)
            is SharkPlayerFile.VideoFile -> Unit //TODO : play video file
        }
    }

    private fun openAsPlaylist() {
        //TODO("Not yet implemented")
    }

    private fun openDirectory(file: SharkPlayerFile.Directory) {
        val action = BottomNavGraphDirections.openDirectory(file.path)
        navController.navigate(action)
    }
}
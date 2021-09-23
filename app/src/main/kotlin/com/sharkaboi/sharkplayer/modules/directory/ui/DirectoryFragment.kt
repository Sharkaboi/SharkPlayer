package com.sharkaboi.sharkplayer.modules.directory.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
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
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        binding.rvDirectories.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setListeners()
        setObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.directory_options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add_favorite -> directoryViewModel.toggleFavorite()
            R.id.item_subtitle_track -> Unit// TODO: 18-09-2021 show subtitle option dialog
            R.id.item_audio_track -> Unit// TODO: 18-09-2021 show audio option dialog
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun initViews() {
        setupTitle()
        setupBackButton()
        setupPathTextView()
        setupRecyclerView()
    }

    private fun setupTitle() {
        binding.toolbar.title = directoryViewModel.selectedDir.folderName
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
    }

    private fun setupBackButton() {
        binding.toolbar.setNavigationOnClickListener { navController.navigateUp() }
    }

    private fun setupPathTextView() {
        binding.tvPath.text = directoryViewModel.selectedDir.path
        binding.tvPath.isSelected = true
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
            binding.tvExistHint.isVisible = state is DirectoryState.DirectoryNotFound
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
            val favoriteItem = binding.toolbar.menu.findItem(R.id.item_add_favorite)
            if (isFavorite) {
                favoriteItem?.title = getString(R.string.remove_from_favorite)
                favoriteItem?.icon = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_favorite_selected
                )
            } else {
                favoriteItem?.title = getString(R.string.add_to_favorite)
                favoriteItem?.icon = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_add_to_favorite
                )
            }
        }
    }

    private fun setListeners() {
        binding.fabPlay.setOnClickListener { openAsPlaylist() }
    }

    private fun navigateToFile(file: SharkPlayerFile) {
        when (file) {
            is SharkPlayerFile.AudioFile -> openAudio(file)
            is SharkPlayerFile.Directory -> openDirectory(file)
            is SharkPlayerFile.OtherFile -> showToast(R.string.unsupported_file)
            is SharkPlayerFile.VideoFile -> openVideo(file)
        }
    }

    private fun openAsPlaylist() {
        //TODO("Not yet implemented")
    }

    private fun openDirectory(file: SharkPlayerFile.Directory) {
        val action = BottomNavGraphDirections.openDirectory(file.path)
        navController.navigate(action)
    }

    private fun openVideo(file: SharkPlayerFile.VideoFile) {
        val action = BottomNavGraphDirections.openVideo(file.path)
        navController.navigate(action)
    }

    private fun openAudio(file: SharkPlayerFile.AudioFile) {
        val action = BottomNavGraphDirections.openAudio(file.path)
        navController.navigate(action)
    }
}
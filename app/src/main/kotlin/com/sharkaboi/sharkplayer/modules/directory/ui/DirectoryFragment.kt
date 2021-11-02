package com.sharkaboi.sharkplayer.modules.directory.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharkaboi.sharkplayer.BottomNavGraphDirections
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.extensions.*
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.FragmentDirectoryBinding
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoNavArgs
import com.sharkaboi.sharkplayer.modules.directory.adapters.DirectoryAdapter
import com.sharkaboi.sharkplayer.modules.directory.vm.DirectoryState
import com.sharkaboi.sharkplayer.modules.directory.vm.DirectoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

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
        activity?.invalidateOptionsMenu()
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        binding.rvDirectories.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.directory_options_menu, menu)
        val favoriteItem = menu.findItem(R.id.item_add_favorite)
        if (directoryViewModel.isFavorite.value == true) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add_favorite -> directoryViewModel.toggleFavorite()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun initViews() {
        setupSwipeRefresh()
        setupTitle()
        setupBackButton()
        setupPathTextView()
        setupRecyclerView()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshDirectory.setOnRefreshListener {
            directoryViewModel.refresh()
            binding.swipeRefreshDirectory.isRefreshing = false
        }
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
        directoryAdapter = DirectoryAdapter(
            onClick = { file ->
                navigateToFile(file)
            },
            onVideoRescale = { videoFile ->
                showRescaleOptions(videoFile)
            },
            onDeleteVideo = { videoFile ->
                context?.showOneOpDialog(R.string.delete_video) {
                    directoryViewModel.deleteVideo(videoFile)
                }
            }
        )
        rvDirectories.adapter = directoryAdapter
        rvDirectories.initLinearDefaults(context, hasFixedSize = true)
    }

    private fun showRescaleOptions(videoFile: SharkPlayerFile.VideoFile) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rescale_options_title)
            .setItems(R.array.rescale_supported_resolutions) { dialog, which ->
                directoryViewModel.runRescaleWork(
                    videoFile,
                    resources.getStringArray(R.array.rescale_supported_resolutions).getOrNull(which)
                )
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun setObservers() {
        observe(directoryViewModel.uiState) { state ->
            binding.progress.isVisible = state is DirectoryState.Loading
            binding.tvExistHint.isVisible = state is DirectoryState.DirectoryNotFound
            when (state) {
                is DirectoryState.Failure -> showToast(state.message)
                else -> Unit
            }
        }
        observe(directoryViewModel.isFavorite) {
            activity?.invalidateOptionsMenu()
        }
        observe(directoryViewModel.files) { files ->
            binding.tvEmptyHint.isVisible = files.isEmpty()
            directoryAdapter.submitList(files)
            setPlayListListener(files)
        }
        observe(directoryViewModel.subtitleIndexOfDirectory) { subtitleIndex ->
            val subtitleItem = binding.toolbar.menu.findItem(R.id.item_subtitle_track)
            subtitleItem.setOnMenuItemClickListener {
                showSubtitleTrackDialog(subtitleIndex)
                true
            }
        }
        observe(directoryViewModel.audioIndexOfDirectory) { audioIndex ->
            val audioItem = binding.toolbar.menu.findItem(R.id.item_audio_track)
            audioItem.setOnMenuItemClickListener {
                showAudioTrackDialog(audioIndex)
                true
            }
        }
    }

    private fun navigateToFile(file: SharkPlayerFile) {
        when (file) {
            is SharkPlayerFile.AudioFile -> openAudio(file)
            is SharkPlayerFile.Directory -> openDirectory(file)
            is SharkPlayerFile.OtherFile -> showToast(R.string.unsupported_file)
            is SharkPlayerFile.VideoFile -> openVideo(file)
        }
    }

    private fun showAudioTrackDialog(defaultValue: Int?) {
        requireContext().showIntegerValuePromptDialog(
            titleId = R.string.enter_track_index,
            defaultValue = defaultValue
        ) { value ->
            directoryViewModel.setAudioTrackIndexOfDir(value)
        }
    }

    private fun showSubtitleTrackDialog(defaultValue: Int?) {
        requireContext().showIntegerValuePromptDialog(
            titleId = R.string.enter_track_index,
            defaultValue = defaultValue
        ) { value ->
            directoryViewModel.setSubTrackIndexOfDir(value)
        }
    }

    private fun setPlayListListener(files: List<SharkPlayerFile>) {
        val videoPaths = files.filterIsInstance<SharkPlayerFile.VideoFile>().map { it.path }
        Timber.d(videoPaths.toString())
        binding.fabPlay.setOnClickListener { openAsPlaylist(videoPaths) }
    }

    private fun openAsPlaylist(videoPaths: List<String>) {
        if (videoPaths.isEmpty()) {
            showToast(R.string.no_videos_in_folder)
            return
        }

        val action = BottomNavGraphDirections.openVideos(
            videoNavArgs = VideoNavArgs(
                dirPath = directoryViewModel.selectedDir.path,
                videoPaths = videoPaths
            )
        )
        navController.navigate(action)
    }

    private fun openDirectory(file: SharkPlayerFile.Directory) {
        val action = BottomNavGraphDirections.openDirectory(file.path)
        navController.navigate(action)
    }

    private fun openVideo(file: SharkPlayerFile.VideoFile) {
        if (file.isDirty) {
            showToast(R.string.video_corrupted)
            return
        }

        val action = BottomNavGraphDirections.openVideos(
            videoNavArgs = VideoNavArgs(
                dirPath = directoryViewModel.selectedDir.path,
                videoPaths = listOf(file.path)
            )
        )
        navController.navigate(action)
    }

    private fun openAudio(file: SharkPlayerFile.AudioFile) {
        if (file.isDirty) {
            showToast(R.string.audio_corrupted)
            return
        }

        val action = BottomNavGraphDirections.openAudio(file.path)
        navController.navigate(action)
    }
}
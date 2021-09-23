package com.sharkaboi.sharkplayer.modules.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sharkaboi.sharkplayer.BottomNavGraphDirections
import com.sharkaboi.sharkplayer.common.extensions.getDefaultDirectories
import com.sharkaboi.sharkplayer.common.extensions.initLinearDefaults
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.FragmentHomeBinding
import com.sharkaboi.sharkplayer.modules.home.adapters.HomeDirectoriesAdapter
import com.sharkaboi.sharkplayer.modules.home.adapters.HomeFavoritesAdapter
import com.sharkaboi.sharkplayer.modules.home.vm.HomeState
import com.sharkaboi.sharkplayer.modules.home.vm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoritesAdapter: HomeFavoritesAdapter
    private val homeViewModel by viewModels<HomeViewModel>()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvFavorites.adapter = null
        binding.rvHomeDirectories.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setObservers()
    }

    private fun initViews() {
        setupHomeDefaultDirectoriesList()
        setupFavoriteDirectoriesList()
    }

    private fun setupHomeDefaultDirectoriesList() {
        val defaultFileDirs = requireContext().getDefaultDirectories()
        val rvHomeDirectories = binding.rvHomeDirectories
        rvHomeDirectories.initLinearDefaults(context)
        rvHomeDirectories.adapter = HomeDirectoriesAdapter { item ->
            openDirectory(item)
        }.apply {
            submitList(defaultFileDirs)
        }
    }

    private fun setupFavoriteDirectoriesList() {
        val rvFavorites = binding.rvFavorites
        rvFavorites.initLinearDefaults(context, hasFixedSize = true)
        favoritesAdapter = HomeFavoritesAdapter(
            onItemClick = { item ->
                openDirectory(item)
            },
            onItemRemove = { item ->
                homeViewModel.removeFavorite(item)
            })
        rvFavorites.adapter = favoritesAdapter
    }

    private fun setObservers() {
        observe(homeViewModel.favorites) { favorites ->
            // TODO: 18-09-2021 Remove ripple, add nested scrolling maybe?
            favoritesAdapter.submitList(favorites)
        }
        observe(homeViewModel.uiState) { state ->
            binding.progress.isVisible = state is HomeState.Loading
            when (state) {
                is HomeState.Failure -> showToast(state.message)
                else -> Unit
            }
        }
    }

    private fun openDirectory(item: SharkPlayerFile.Directory) {
        val action = BottomNavGraphDirections.openDirectory(path = item.path)
        navController.navigate(action)
    }
}
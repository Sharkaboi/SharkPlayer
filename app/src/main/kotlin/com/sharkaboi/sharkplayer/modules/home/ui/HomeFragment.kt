package com.sharkaboi.sharkplayer.modules.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.sharkaboi.sharkplayer.BottomNavGraphDirections
import com.sharkaboi.sharkplayer.common.extensions.getDefaultDirectories
import com.sharkaboi.sharkplayer.common.extensions.initLinearDefaults
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.FragmentHomeBinding
import com.sharkaboi.sharkplayer.modules.home.adapters.HomeDirectoriesAdapter
import com.sharkaboi.sharkplayer.modules.home.adapters.HomeHintAdapter
import com.sharkaboi.sharkplayer.modules.home.vm.HomeState
import com.sharkaboi.sharkplayer.modules.home.vm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoritesAdapter: HomeDirectoriesAdapter
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
        binding.rvHomeDirectories.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setObservers()
    }

    private fun initViews() {
        setupHomeDirsList()
    }

    private fun setupHomeDirsList() {
        val defaultFileDirs = requireContext().getDefaultDirectories()
        val rvHomeDirectories = binding.rvHomeDirectories
        rvHomeDirectories.initLinearDefaults(context)
        val homeHintAdapter = HomeHintAdapter("Home")
        val homeDirAdapter = HomeDirectoriesAdapter(
            isHomeDirs = true,
            onItemClick = { item ->
                openDirectory(item)
            },
            onItemRemove = { }
        )
        homeDirAdapter.submitList(defaultFileDirs)
        val favsHintAdapter = HomeHintAdapter("Favorites")
        favoritesAdapter = HomeDirectoriesAdapter(
            isHomeDirs = false,
            onItemClick = { item ->
                openDirectory(item)
            },
            onItemRemove = { item ->
                homeViewModel.removeFavorite(item)
            }
        )
        favoritesAdapter.submitList(defaultFileDirs)
        rvHomeDirectories.adapter = ConcatAdapter(
            homeHintAdapter,
            homeDirAdapter,
            favsHintAdapter,
            favoritesAdapter
        )
    }

    private fun setObservers() {
        observe(homeViewModel.favorites) { favorites ->
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
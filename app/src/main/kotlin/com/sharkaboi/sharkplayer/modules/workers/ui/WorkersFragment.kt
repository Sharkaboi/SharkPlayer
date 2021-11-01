package com.sharkaboi.sharkplayer.modules.workers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sharkaboi.sharkplayer.common.extensions.initLinearDefaults
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.databinding.FragmentWorkersBinding
import com.sharkaboi.sharkplayer.modules.workers.adapters.WorkersAdapter
import com.sharkaboi.sharkplayer.modules.workers.vm.WorkersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkersFragment : Fragment() {
    private lateinit var workersAdapter: WorkersAdapter
    private var _binding: FragmentWorkersBinding? = null
    private val binding get() = _binding!!
    private val workersViewModel by viewModels<WorkersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkersBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvWorkers.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setObservers()
    }

    private fun initViews() {
        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        workersAdapter = WorkersAdapter()
        binding.rvWorkers.adapter = workersAdapter
        binding.rvWorkers.initLinearDefaults(context, hasFixedSize = true)
    }

    private fun setObservers() {
        observe(workersViewModel.workers) { workers ->
            binding.tvNoWorkers.isVisible = workers.isEmpty()
            workersAdapter.submitList(workers)
        }
    }
}
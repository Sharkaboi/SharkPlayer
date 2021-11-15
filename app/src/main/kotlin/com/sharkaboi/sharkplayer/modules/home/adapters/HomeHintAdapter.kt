package com.sharkaboi.sharkplayer.modules.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharkaboi.sharkplayer.databinding.ItemHomeHintBinding

class HomeHintAdapter(private val hintText: String) :
    RecyclerView.Adapter<HomeHintAdapter.HomeHintViewHolder>() {

    private lateinit var binding: ItemHomeHintBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHintViewHolder {
        binding = ItemHomeHintBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeHintViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeHintViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1

    inner class HomeHintViewHolder(
        private val binding: ItemHomeHintBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.tvHomeHint.text = hintText
        }
    }
}

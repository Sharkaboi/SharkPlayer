package com.sharkaboi.sharkplayer.modules.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.ItemDirectoryFileBinding

class HomeDirectoriesAdapter(
    private val onItemClick: (SharkPlayerFile.Directory) -> Unit
) : ListAdapter<SharkPlayerFile.Directory, HomeDirectoriesAdapter.HomeDirectoriesViewHolder>(
    diffUtilItemCallback
) {

    private lateinit var binding: ItemDirectoryFileBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeDirectoriesViewHolder {
        binding = ItemDirectoryFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeDirectoriesViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HomeDirectoriesViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class HomeDirectoriesViewHolder(
        private val binding: ItemDirectoryFileBinding,
        private val onItemClick: (SharkPlayerFile.Directory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvDetails.isGone = true
            binding.ibMore.isGone = true
        }

        fun bind(item: SharkPlayerFile.Directory) {
            binding.root.setOnClickListener { onItemClick(item) }
            binding.tvName.text = item.folderName
        }
    }
}

private val diffUtilItemCallback = object : DiffUtil.ItemCallback<SharkPlayerFile.Directory>() {
    override fun areItemsTheSame(
        oldItem: SharkPlayerFile.Directory,
        newItem: SharkPlayerFile.Directory
    ): Boolean {
        return oldItem.getIdentifier() == newItem.getIdentifier()
    }

    override fun areContentsTheSame(
        oldItem: SharkPlayerFile.Directory,
        newItem: SharkPlayerFile.Directory
    ): Boolean {
        return oldItem == newItem
    }
}
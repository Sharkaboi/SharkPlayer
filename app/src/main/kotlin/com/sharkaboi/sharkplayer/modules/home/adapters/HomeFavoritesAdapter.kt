package com.sharkaboi.sharkplayer.modules.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.ItemDirectoryFileBinding
import me.saket.cascade.CascadePopupMenu

class HomeFavoritesAdapter(
    private val onItemClick: (SharkPlayerFile.Directory) -> Unit,
    private val onItemRemove: (SharkPlayerFile.Directory) -> Unit
) : ListAdapter<SharkPlayerFile.Directory, HomeFavoritesAdapter.HomeDirectoriesViewHolder>(
    diffUtilItemCallback
) {

    private lateinit var binding: ItemDirectoryFileBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeDirectoriesViewHolder {
        binding = ItemDirectoryFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeDirectoriesViewHolder(binding, onItemClick, onItemRemove)
    }

    override fun onBindViewHolder(holder: HomeDirectoriesViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class HomeDirectoriesViewHolder(
        private val binding: ItemDirectoryFileBinding,
        private val onItemClick: (SharkPlayerFile.Directory) -> Unit,
        private val onItemRemove: (SharkPlayerFile.Directory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvDetails.isGone = true
        }

        fun bind(item: SharkPlayerFile.Directory) {
            binding.root.setOnClickListener { onItemClick(item) }
            binding.tvName.text = item.folderName
            binding.tvName.isSelected = true
            binding.ibMore.setOnClickListener {
                val menu = CascadePopupMenu(it.context, it)
                menu.inflate(R.menu.favorites_options_menu)
                menu.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId == R.id.remove_item) {
                        onItemRemove(item)
                    }
                    true
                }
                menu.show()
            }
        }
    }
}

private val diffUtilItemCallback = object : DiffUtil.ItemCallback<SharkPlayerFile.Directory>() {
    override fun areItemsTheSame(
        oldItem: SharkPlayerFile.Directory,
        newItem: SharkPlayerFile.Directory
    ): Boolean {
        return oldItem.absolutePath == newItem.absolutePath
    }

    override fun areContentsTheSame(
        oldItem: SharkPlayerFile.Directory,
        newItem: SharkPlayerFile.Directory
    ): Boolean {
        return oldItem == newItem
    }
}
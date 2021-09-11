package com.sharkaboi.sharkplayer.modules.directory.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.extensions.getSizeString
import com.sharkaboi.sharkplayer.common.extensions.getTimeString
import com.sharkaboi.sharkplayer.common.extensions.setThumbnailOf
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.ItemDirectoryFileBinding
import kotlin.time.DurationUnit

class DirectoryAdapter(private val onClick: (SharkPlayerFile) -> Unit) :
    ListAdapter<SharkPlayerFile, DirectoryAdapter.DirectoryViewHolder>(diffUtilItemCallback) {

    private lateinit var binding: ItemDirectoryFileBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoryViewHolder {
        binding = ItemDirectoryFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DirectoryViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class DirectoryViewHolder(
        private val binding: ItemDirectoryFileBinding,
        private val onClick: (SharkPlayerFile) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.ibMore.isGone = true
        }

        fun bind(item: SharkPlayerFile) {
            binding.root.setOnClickListener {
                onClick(item)
            }
            when (item) {
                is SharkPlayerFile.AudioFile -> {
                    binding.ivThumbnail.load(R.drawable.ic_audio_file)
                    binding.tvName.text = item.fileName
                    binding.tvDetails.text = ("${item.quality}\n${
                        item.length.getTimeString()
                    }\n${item.size.getSizeString()}")
                }
                is SharkPlayerFile.Directory -> {
                    binding.ivThumbnail.load(R.drawable.ic_directory)
                    binding.tvName.text = item.folderName
                    binding.tvDetails.text = ("${item.childFileCount} items")
                }
                is SharkPlayerFile.OtherFile -> {
                    binding.ivThumbnail.load(R.drawable.ic_other_file)
                    binding.tvName.text = item.fileName
                    binding.tvDetails.text = (item.size.getSizeString())
                }
                is SharkPlayerFile.VideoFile -> {
                    binding.ivThumbnail.setThumbnailOf(item) {
                        error(R.drawable.ic_video_file)
                        fallback(R.drawable.ic_video_file)
                    }
                    binding.tvName.text = item.fileName
                    binding.tvDetails.text =
                        ("${item.resolution}\n${
                            item.length.getTimeString()
                        }\n${item.size.getSizeString()}")
                }
            }
        }
    }
}

private val diffUtilItemCallback = object : DiffUtil.ItemCallback<SharkPlayerFile>() {
    override fun areItemsTheSame(oldItem: SharkPlayerFile, newItem: SharkPlayerFile): Boolean {
        return oldItem.getAbsolutePath() == newItem.getAbsolutePath()
    }

    override fun areContentsTheSame(
        oldItem: SharkPlayerFile,
        newItem: SharkPlayerFile
    ): Boolean {
        return oldItem == newItem
    }
}

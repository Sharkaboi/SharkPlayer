package com.sharkaboi.sharkplayer.modules.directory.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
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
import me.saket.cascade.CascadePopupMenu

class DirectoryAdapter(
    private val onClick: (SharkPlayerFile) -> Unit,
    private val onVideoRescale: (SharkPlayerFile.VideoFile) -> Unit,
    private val onDeleteVideo: (SharkPlayerFile.VideoFile) -> Unit
) : ListAdapter<SharkPlayerFile, DirectoryAdapter.DirectoryViewHolder>(diffUtilItemCallback) {

    private lateinit var binding: ItemDirectoryFileBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoryViewHolder {
        binding = ItemDirectoryFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DirectoryViewHolder(binding, onClick, onVideoRescale, onDeleteVideo)
    }

    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class DirectoryViewHolder(
        private val binding: ItemDirectoryFileBinding,
        private val onClick: (SharkPlayerFile) -> Unit,
        private val onVideoRescale: (SharkPlayerFile.VideoFile) -> Unit,
        private val onDeleteVideo: (SharkPlayerFile.VideoFile) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.ibMore.isGone = true
            binding.tvName.isSelected = true
        }

        fun bind(item: SharkPlayerFile) {
            binding.root.setOnClickListener {
                onClick(item)
            }
            when (item) {
                is SharkPlayerFile.AudioFile -> {
                    binding.ivThumbnail.load(item.path.toUri()) {
                        error(R.drawable.ic_audio_file)
                        fallback(R.drawable.ic_audio_file)
                        placeholder(R.drawable.ic_audio_file)
                    }
                    binding.tvName.text = item.fileName
                    binding.tvDetails.text = buildString {
                        append(item.quality)
                        append(" - ")
                        append(item.length.getTimeString())
                        append(" - ")
                        append(item.size.getSizeString())
                    }
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
                        placeholder(R.drawable.ic_video_file)
                    }
                    binding.tvName.text = item.fileName
                    binding.tvDetails.text = buildString {
                        append(item.resolution)
                        append(" - ")
                        append(item.length.getTimeString())
                        append(" - ")
                        append(item.size.getSizeString())
                    }
                    binding.ibMore.isVisible = true
                    binding.ibMore.setOnClickListener {
                        val menu = CascadePopupMenu(it.context, it)
                        menu.inflate(R.menu.video_options_menu)
                        menu.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.rescale_video_item -> onVideoRescale(item)
                                R.id.delete_video_item -> onDeleteVideo(item)
                            }
                            true
                        }
                        menu.show()
                    }
                }
            }
        }
    }
}

private val diffUtilItemCallback = object : DiffUtil.ItemCallback<SharkPlayerFile>() {
    override fun areItemsTheSame(oldItem: SharkPlayerFile, newItem: SharkPlayerFile): Boolean {
        return oldItem.absolutePath == newItem.absolutePath
    }

    override fun areContentsTheSame(
        oldItem: SharkPlayerFile,
        newItem: SharkPlayerFile
    ): Boolean {
        return oldItem == newItem
    }
}

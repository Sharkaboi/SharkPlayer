package com.sharkaboi.sharkplayer.exoplayer.download_sub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.sharkaboi.sharkplayer.databinding.ItemDownloadSubBinding

class DownloadSubsAdapter(private val onClick: (OpenSubtitleItem) -> Unit) :
    RecyclerView.Adapter<DownloadSubsAdapter.DownloadSubsViewHolder>() {

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<OpenSubtitleItem>() {
        override fun areItemsTheSame(
            oldItem: OpenSubtitleItem,
            newItem: OpenSubtitleItem
        ): Boolean {
            return oldItem.IDSubtitle == newItem.IDSubtitle
        }

        override fun areContentsTheSame(
            oldItem: OpenSubtitleItem,
            newItem: OpenSubtitleItem
        ): Boolean {
            return oldItem == newItem
        }

    }

    private val listDiffer = AsyncListDiffer(this, diffUtilItemCallback)

    private lateinit var binding: ItemDownloadSubBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadSubsViewHolder {
        binding = ItemDownloadSubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DownloadSubsViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: DownloadSubsViewHolder, position: Int) {
        holder.bind(listDiffer.currentList[position])
    }

    override fun getItemCount(): Int {
        return listDiffer.currentList.size
    }

    fun submitList(list: List<OpenSubtitleItem>) {
        listDiffer.submitList(list)
    }

    class DownloadSubsViewHolder
    constructor(
        private val binding: ItemDownloadSubBinding,
        private val onClick: (OpenSubtitleItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OpenSubtitleItem) {
            binding.root.setOnClickListener {
                onClick(item)
            }
            binding.tvTitle.text = item.SubFileName
            binding.tvSubtitle.text = item.MovieName
        }
    }
}

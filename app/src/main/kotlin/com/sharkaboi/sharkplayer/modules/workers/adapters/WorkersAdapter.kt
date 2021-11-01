package com.sharkaboi.sharkplayer.modules.workers.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.sharkaboi.sharkplayer.databinding.ItemWorkersBinding
import com.sharkaboi.sharkplayer.ffmpeg.workers.FFMpegWorker

class WorkersAdapter : RecyclerView.Adapter<WorkersAdapter.WorkersViewHolder>() {

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<WorkInfo>() {
        override fun areItemsTheSame(oldItem: WorkInfo, newItem: WorkInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkInfo, newItem: WorkInfo): Boolean {
            return oldItem == newItem
        }
    }

    private val listDiffer = AsyncListDiffer(this, diffUtilItemCallback)

    private lateinit var binding: ItemWorkersBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkersViewHolder {
        binding = ItemWorkersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkersViewHolder, position: Int) {
        holder.bind(listDiffer.currentList[position])
    }

    override fun getItemCount(): Int = listDiffer.currentList.size

    fun submitList(list: List<WorkInfo>) {
        listDiffer.submitList(list)
    }

    class WorkersViewHolder(
        private val binding: ItemWorkersBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkInfo) {
            binding.tvTitle.text =
                item.tags.firstOrNull { it !in FFMpegWorker.packages } ?: "Unnamed Work Task"
            binding.tvDetails.text = ("ID : ${item.id}")
            binding.tvState.text = item.state.toString()
        }
    }
}

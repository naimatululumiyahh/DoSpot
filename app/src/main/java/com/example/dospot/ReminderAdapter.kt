package com.example.dospot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dospot.databinding.ItemReminderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAdapter(
    private val onItemClick: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReminderViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReminderViewHolder(
        private val binding: ItemReminderBinding,
        private val onItemClick: (Reminder) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.apply {
                tvTitle.text = reminder.title
                tvDescription.text = reminder.description
                tvLocation.text = reminder.locationName
                tvTime.text = formatTime(reminder.timestamp)
                tvDate.text = formatDate(reminder.timestamp)

                root.setOnClickListener {
                    onItemClick(reminder)
                }
            }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return "${sdf.format(Date(timestamp))} WIB"
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            return sdf.format(Date(timestamp))
        }
    }

    class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}
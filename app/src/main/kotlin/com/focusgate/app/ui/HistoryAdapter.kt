package com.focusgate.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.focusgate.app.data.TaskRecord
import com.focusgate.app.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val records: List<TaskRecord>) :
    RecyclerView.Adapter<HistoryAdapter.VH>() {

    inner class VH(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = records.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val record = records[position]
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        holder.binding.tvHistoryDate.text = sdf.format(Date(record.completedAt))
        holder.binding.tvHistoryTask.text = record.taskType.replace('_', ' ').lowercase()
            .replaceFirstChar { it.uppercaseChar() }
    }
}

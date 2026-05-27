package com.focusgate.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.focusgate.app.data.TaskType
import com.focusgate.app.databinding.ItemTaskBinding

class TaskAdapter(
    private val tasks: List<TaskType>,
    private val onClick: (TaskType) -> Unit
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    inner class VH(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = tasks[position]
        val ctx = holder.binding.root.context

        holder.binding.tvTaskTitle.setText(task.titleRes)
        holder.binding.tvTaskDesc.setText(task.descRes)
        holder.binding.ivTaskIcon.setImageResource(task.iconRes)

        val restricted = task.timeRestriction != null
        holder.binding.tvTaskBadge.visibility =
            if (restricted) android.view.View.VISIBLE else android.view.View.GONE

        val available = task.isAvailableNow()
        holder.binding.root.alpha = if (available) 1f else 0.45f
        holder.binding.root.isEnabled = available
        holder.binding.root.setOnClickListener { if (available) onClick(task) }
    }
}

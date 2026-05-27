package com.focusgate.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.focusgate.app.data.TaskType
import com.focusgate.app.databinding.ActivityTaskPickerBinding
import com.focusgate.app.ui.tasks.*

class TaskPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tasks = TaskType.values().toList()
        val adapter = TaskAdapter(tasks) { task ->
            when (task) {
                TaskType.PUSHUPS -> startActivity(Intent(this, PushupTaskActivity::class.java))
                TaskType.READING -> startActivity(Intent(this, ReadingTaskActivity::class.java))
                TaskType.STEPS -> startActivity(Intent(this, StepsTaskActivity::class.java))
                TaskType.SHOWER, TaskType.WATER, TaskType.BREATHING -> {
                    val intent = Intent(this, CheckboxTaskActivity::class.java)
                    intent.putExtra(CheckboxTaskActivity.EXTRA_TASK_TYPE, task.name)
                    startActivity(intent)
                }
            }
        }

        binding.rvTasks.adapter = adapter
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
    }
}

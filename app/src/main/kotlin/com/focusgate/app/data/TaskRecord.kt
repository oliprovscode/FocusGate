package com.focusgate.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_records")
data class TaskRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskType: String,
    val completedAt: Long = System.currentTimeMillis(),
    val dateKey: String // "YYYY-MM-DD" for easy daily lookup
)

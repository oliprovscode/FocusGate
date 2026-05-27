package com.focusgate.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insert(record: TaskRecord)

    @Query("SELECT * FROM task_records ORDER BY completedAt DESC LIMIT 30")
    fun getRecentHistory(): Flow<List<TaskRecord>>

    @Query("SELECT COUNT(*) FROM task_records WHERE dateKey = :dateKey")
    suspend fun countForDate(dateKey: String): Int

    @Query("SELECT DISTINCT dateKey FROM task_records ORDER BY dateKey DESC LIMIT 60")
    suspend fun getCompletedDays(): List<String>
}

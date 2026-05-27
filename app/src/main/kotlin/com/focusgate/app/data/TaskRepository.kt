package com.focusgate.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).taskDao()
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    val recentHistory: Flow<List<TaskRecord>> = dao.getRecentHistory()

    suspend fun recordCompletion(taskType: TaskType) {
        dao.insert(
            TaskRecord(
                taskType = taskType.name,
                dateKey = LocalDate.now().format(fmt)
            )
        )
    }

    suspend fun isCompletedToday(): Boolean {
        val today = LocalDate.now().format(fmt)
        return dao.countForDate(today) > 0
    }

    /** Returns streak in consecutive days ending today **/
    suspend fun getCurrentStreak(): Int {
        val days = dao.getCompletedDays().toMutableList()
        if (days.isEmpty()) return 0
        var streak = 0
        var current = LocalDate.now()
        for (day in days) {
            val dayDate = LocalDate.parse(day, fmt)
            if (dayDate == current || dayDate == current.minusDays(streak.toLong())) {
                streak++
                current = dayDate
            } else {
                break
            }
        }
        return streak
    }
}

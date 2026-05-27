package com.focusgate.app.data

import java.time.LocalTime

enum class TaskType(
    val titleRes: Int,
    val descRes: Int,
    val iconRes: Int,
    val timeRestriction: Pair<LocalTime, LocalTime>? = null
) {
    PUSHUPS(
        com.focusgate.app.R.string.task_pushups_title,
        com.focusgate.app.R.string.task_pushups_desc,
        com.focusgate.app.R.drawable.ic_pushup
    ),
    SHOWER(
        com.focusgate.app.R.string.task_shower_title,
        com.focusgate.app.R.string.task_shower_desc,
        com.focusgate.app.R.drawable.ic_shower,
        timeRestriction = Pair(LocalTime.of(8, 0), LocalTime.of(10, 0))
    ),
    STEPS(
        com.focusgate.app.R.string.task_steps_title,
        com.focusgate.app.R.string.task_steps_desc,
        com.focusgate.app.R.drawable.ic_steps
    ),
    READING(
        com.focusgate.app.R.string.task_read_title,
        com.focusgate.app.R.string.task_read_desc,
        com.focusgate.app.R.drawable.ic_book
    ),
    WATER(
        com.focusgate.app.R.string.task_water_title,
        com.focusgate.app.R.string.task_water_desc,
        com.focusgate.app.R.drawable.ic_water
    ),
    BREATHING(
        com.focusgate.app.R.string.task_breathing_title,
        com.focusgate.app.R.string.task_breathing_desc,
        com.focusgate.app.R.drawable.ic_breathing
    );

    fun isAvailableNow(): Boolean {
        val restriction = timeRestriction ?: return true
        val now = LocalTime.now()
        return now.isAfter(restriction.first) && now.isBefore(restriction.second)
    }
}

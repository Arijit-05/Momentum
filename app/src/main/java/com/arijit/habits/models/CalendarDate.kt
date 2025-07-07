package com.arijit.habits.models

import java.util.Date

data class DateTaskStatus(
    val date: Date,
    val finishedTasks: Int,
    val totalTasks: Int
)
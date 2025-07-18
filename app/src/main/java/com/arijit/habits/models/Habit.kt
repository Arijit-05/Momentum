package com.arijit.habits.models

data class Habit(
    var id: String = "",
    var name: String = "",
    var emoji: String = "",
    var isDone: Boolean = false,
    var hasReminder: Boolean = false,
    var reminderTime: String = "",
    var creationDate: String = "",
    var completionDates: MutableList<String> = mutableListOf()

)
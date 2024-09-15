package com.example.to_dolist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskName: String,
    var isCompleted: Boolean = false
)

package com.example.dospot

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")

data class ReminderEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val timestamp: Long
)
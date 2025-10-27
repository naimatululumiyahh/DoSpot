package com.example.dospot

data class Reminder(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
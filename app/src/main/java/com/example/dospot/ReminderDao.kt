package com.example.dospot

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllReminders(userId: String): LiveData<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE userId = :userId")
    suspend fun deleteAllReminders(userId: String)
}

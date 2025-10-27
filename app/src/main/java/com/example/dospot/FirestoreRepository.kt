package com.example.dospot

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val remindersCollection = db.collection("reminders")

    suspend fun addReminder(reminder: Reminder): Result<String> {
        return try {
            // Use the reminder.id as the document id so the id remains consistent
            val docId = if (reminder.id.isNotEmpty()) reminder.id else remindersCollection.document().id
            remindersCollection.document(docId).set(reminder).await()
            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReminder(reminderId: String, reminder: Reminder): Result<Unit> {
        return try {
            remindersCollection.document(reminderId).set(reminder).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            remindersCollection.document(reminderId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReminders(userId: String): Result<List<Reminder>> {
        return try {
            val snapshot = remindersCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val reminders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reminder::class.java)?.copy(id = doc.id)
            }
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReminderById(reminderId: String): Result<Reminder> {
        return try {
            val snapshot = remindersCollection.document(reminderId).get().await()
            val reminder = snapshot.toObject(Reminder::class.java)?.copy(id = snapshot.id)
                ?: return Result.failure(Exception("Reminder not found"))
            Result.success(reminder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
package com.example.dospot

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val remindersCollection = db.collection("reminders")

    suspend fun addReminder(reminder: Reminder): Result<String> {
        return try {
            val docRef = remindersCollection.add(reminder).await()
            Result.success(docRef.id)
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
}
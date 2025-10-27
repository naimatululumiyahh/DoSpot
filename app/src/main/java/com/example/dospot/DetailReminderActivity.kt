package com.example.dospot

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dospot.databinding.ActivityDetailReminderBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailReminderBinding
    private lateinit var firestoreRepository: FirestoreRepository
    private var reminderId: String = ""
    private var currentReminder: Reminder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreRepository = FirestoreRepository()
        reminderId = intent.getStringExtra("REMINDER_ID") ?: ""

        if (reminderId.isEmpty()) {
            Toast.makeText(this, "Error: Reminder ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        loadReminderDetail()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEdit.setOnClickListener {
            val intent = android.content.Intent(this, EditReminderActivity::class.java).apply {
                putExtra("REMINDER_ID", reminderId)
                putExtra("REMINDER_TITLE", currentReminder?.title)
                putExtra("REMINDER_DESCRIPTION", currentReminder?.description)
                putExtra("REMINDER_LOCATION_NAME", currentReminder?.locationName)
                putExtra("REMINDER_LATITUDE", currentReminder?.latitude)
                putExtra("REMINDER_LONGITUDE", currentReminder?.longitude)
                putExtra("REMINDER_TIMESTAMP", currentReminder?.timestamp)
            }
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun loadReminderDetail() {
        showLoading(true)

        lifecycleScope.launch {
            firestoreRepository.getReminderById(reminderId).fold(
                onSuccess = { reminder ->
                    currentReminder = reminder
                    displayReminderData()
                    showLoading(false)
                },
                onFailure = { e ->
                    showLoading(false)
                    Toast.makeText(
                        this@DetailReminderActivity,
                        "Gagal memuat detail: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            )
        }
    }

    private fun displayReminderData() {
        // currentReminder is populated from Firestore in loadReminderDetail()
        binding.apply {
            tvTitle.text = currentReminder?.title
            tvSubtitle.text = currentReminder?.description
            tvLocationName.text = currentReminder?.locationName
            tvCoordinates.text = "Lat: ${String.format("%.4f", currentReminder?.latitude)}, " +
                    "Lng: ${String.format("%.4f", currentReminder?.longitude)}"
            tvTime.text = formatTime(currentReminder?.timestamp ?: 0L)
            tvDate.text = formatDate(currentReminder?.timestamp ?: 0L)
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pengingat")
            .setMessage("Apakah Anda yakin ingin menghapus pengingat ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteReminder()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteReminder() {
        showLoading(true)

        lifecycleScope.launch {
            firestoreRepository.deleteReminder(reminderId).fold(
                onSuccess = {
                    showLoading(false)
                    Toast.makeText(
                        this@DetailReminderActivity,
                        "Pengingat berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                },
                onFailure = { e ->
                    showLoading(false)
                    Toast.makeText(
                        this@DetailReminderActivity,
                        "Gagal menghapus: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "${sdf.format(Date(timestamp))} WIB"
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
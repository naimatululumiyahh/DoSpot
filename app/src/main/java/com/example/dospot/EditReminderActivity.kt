package com.example.dospot

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dospot.databinding.ActivityAddReminderBinding
import kotlinx.coroutines.launch

class EditReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditReminderBinding
    private lateinit var locationHelper: LocationHelper
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var firestoreRepository: FirestoreRepository

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var locationName: String = ""
    private var reminderId: String = ""
    private var currentReminder: Reminder? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(
                this,
                "Permission ditolak. Tidak dapat mengambil lokasi.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationHelper = LocationHelper(this)
        preferenceHelper = PreferenceHelper(this)
        firestoreRepository = FirestoreRepository()

        reminderId = intent.getStringExtra("REMINDER_ID") ?: ""
        if (reminderId.isEmpty()) {
            Toast.makeText(this, "Error: Reminder ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupListeners()
        loadReminderData()
    }

    private fun setupViews() {
        binding.btnSave.text = "Update"
        binding.tvTitle.text = "Edit Pengingat"
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            updateReminder()
        }

        binding.btnGetLocation.setOnClickListener {
            requestLocationPermission()
        }

        binding.etLocation.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun loadReminderData() {
        showLoading(true)
        lifecycleScope.launch {
            firestoreRepository.getReminderById(reminderId).fold(
                onSuccess = { reminder ->
                    currentReminder = reminder
                    displayReminderData(reminder)
                    showLoading(false)
                },
                onFailure = { e ->
                    showLoading(false)
                    Toast.makeText(
                        this@EditReminderActivity,
                        "Gagal memuat data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            )
        }
    }

    private fun displayReminderData(reminder: Reminder) {
        binding.apply {
            etTitle.setText(reminder.title)
            etDescription.setText(reminder.description)
            etLocation.setText(reminder.locationName)
            
            currentLatitude = reminder.latitude
            currentLongitude = reminder.longitude
            locationName = reminder.locationName
            
            tvCoordinates.text = "Lat: ${String.format("%.4f", currentLatitude)}, " +
                    "Lng: ${String.format("%.4f", currentLongitude)}"
            cardLocationInfo.visibility = View.VISIBLE
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    this,
                    "Izin lokasi diperlukan untuk fitur ini",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val location: Location? = locationHelper.getCurrentLocation()

                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude

                    locationName = locationHelper.getAddressFromLocation(
                        currentLatitude,
                        currentLongitude
                    )

                    binding.etLocation.setText(locationName)
                    binding.tvCoordinates.text =
                        "Lat: ${String.format("%.4f", currentLatitude)}, " +
                                "Lng: ${String.format("%.4f", currentLongitude)}"
                    binding.cardLocationInfo.visibility = View.VISIBLE

                    Toast.makeText(
                        this@EditReminderActivity,
                        "Lokasi berhasil diambil!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@EditReminderActivity,
                        "Gagal mendapatkan lokasi. Pastikan GPS aktif.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EditReminderActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateReminder() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (title.isEmpty()) {
            binding.tilTitle.error = "Judul tidak boleh kosong"
            return
        }
        binding.tilTitle.error = null

        if (description.isEmpty()) {
            binding.tilDescription.error = "Deskripsi tidak boleh kosong"
            return
        }
        binding.tilDescription.error = null

        if (location.isEmpty() || currentLatitude == 0.0 || currentLongitude == 0.0) {
            binding.tilLocation.error = "Lokasi harus diambil"
            Toast.makeText(this, "Silakan ambil lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        binding.tilLocation.error = null

        val updatedReminder = currentReminder?.copy(
            title = title,
            description = description,
            latitude = currentLatitude,
            longitude = currentLongitude,
            locationName = locationName,
            timestamp = System.currentTimeMillis(),
            userId = currentReminder?.userId ?: preferenceHelper.getUserId() ?: ""
        ) ?: return

        showLoading(true)
        lifecycleScope.launch {
            firestoreRepository.updateReminder(reminderId, updatedReminder.copy(id = reminderId)).fold(
                onSuccess = {
                    showLoading(false)
                    Toast.makeText(
                        this@EditReminderActivity,
                        "Pengingat berhasil diperbarui!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                },
                onFailure = { e ->
                    showLoading(false)
                    Toast.makeText(
                        this@EditReminderActivity,
                        "Gagal memperbarui: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.btnGetLocation.isEnabled = !isLoading
    }
}
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
import java.util.UUID

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var locationHelper: LocationHelper
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var firestoreRepository: FirestoreRepository

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var locationName: String = ""

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
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationHelper = LocationHelper(this)
        preferenceHelper = PreferenceHelper(this)
        firestoreRepository = FirestoreRepository()

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveReminder()
        }

        binding.btnGetLocation.setOnClickListener {
            requestLocationPermission()
        }

        binding.etLocation.setOnClickListener {
            requestLocationPermission()
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
                        this@AddReminderActivity,
                        "Lokasi berhasil diambil!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@AddReminderActivity,
                        "Gagal mendapatkan lokasi. Pastikan GPS aktif.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AddReminderActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun saveReminder() {
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

        val userId = preferenceHelper.getUserId() ?: return

        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            description = description,
            latitude = currentLatitude,
            longitude = currentLongitude,
            locationName = locationName,
            timestamp = System.currentTimeMillis()
        )

        showLoading(true)
        lifecycleScope.launch {
            firestoreRepository.addReminder(reminder).fold(
                onSuccess = {
                    showLoading(false)
                    Toast.makeText(
                        this@AddReminderActivity,
                        "Pengingat berhasil ditambahkan!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                },
                onFailure = { e ->
                    showLoading(false)
                    Toast.makeText(
                        this@AddReminderActivity,
                        "Gagal menyimpan: ${e.message}",
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
// Taruh di: com.example.dospot/LocationHelper.kt
package com.example.dospot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null

        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: SecurityException) {
            // Tangani secara spesifik jika izin lokasi tidak ada/dicabut
            Log.e("LocationError", "Izin lokasi tidak diberikan. Pastikan izin sudah benar.", e)
            null // Kembalikan null karena tidak ada izin
        } catch (e: java.util.concurrent.TimeoutException) {
            // Tangani jika waktu pencarian lokasi habis
            Log.w("LocationError", "Waktu permintaan lokasi habis (timeout).", e)
            null // Gagal mendapatkan lokasi dalam waktu yang ditentukan
        } catch (e: Exception) {
            // Tangani error tak terduga lainnya
            Log.e("LocationError", "Gagal mendapatkan lokasi: ${e.message}", e)
            null
        }

    }

    fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                buildString {
                    address.getAddressLine(0)?.let { append(it) }
                }
            } else {
                "Lokasi tidak diketahui"
            }
        } catch (e: Exception) {
            "Lat: $latitude, Lng: $longitude"
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}
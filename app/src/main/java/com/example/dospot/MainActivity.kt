package com.example.dospot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dospot.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ReminderAdapter
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        preferenceHelper = PreferenceHelper(this)
        firestoreRepository = FirestoreRepository()

        if (!preferenceHelper.isLoggedIn()) {
            navigateToLogin()
            return
        }

        setupRecyclerView()
        setupListeners()
        loadReminders()
    }

    override fun onResume() {
        super.onResume()
        loadReminders()
    }

    private fun setupRecyclerView() {
        adapter = ReminderAdapter { reminder ->
            val intent = Intent(this, DetailReminderActivity::class.java)
            intent.putExtra("REMINDER_ID", reminder.id)
            startActivity(intent)
        }

        binding.rvReminders.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = this@MainActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddReminderActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.btnMenu.setOnClickListener {
            Toast.makeText(this, "Menu coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadReminders() {
        val userId = preferenceHelper.getUserId() ?: return

        showLoading(true)
        lifecycleScope.launch {
            firestoreRepository.getReminders(userId).fold(
                onSuccess = { reminders ->
                    showLoading(false)
                    if (reminders.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        adapter.submitList(reminders)
                    }
                },
                onFailure = { e ->
                    showLoading(false)
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal memuat data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                logout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        preferenceHelper.clearUser()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvReminders.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
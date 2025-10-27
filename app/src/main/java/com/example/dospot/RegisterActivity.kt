// Taruh di: com.example.dospot/RegisterActivity.kt
package com.example.dospot

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dospot.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        preferenceHelper = PreferenceHelper(this)

    }


    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            binding.tilName.error = "Nama tidak boleh kosong"
            return false
        }
        binding.tilName.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email tidak valid"
            return false
        }
        binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return false
        }

        if (password.length < 6) {
            binding.tilPassword.error = "Password minimal 6 karakter"
            return false
        }
        binding.tilPassword.error = null

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
            return false
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Password tidak sama"
            return false
        }
        binding.tilConfirmPassword.error = null

        return true
    }

    private fun registerUser(name: String, email: String, password: String) {
        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let { user ->
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user.updateProfile(profileUpdates)
                        .addOnSuccessListener {
                            preferenceHelper.saveUserId(user.uid)
                            Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                            navigateToMain()
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            Toast.makeText(this, "Gagal update profil: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Registrasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
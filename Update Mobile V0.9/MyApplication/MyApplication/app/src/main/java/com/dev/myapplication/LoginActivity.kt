package com.dev.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val database by lazy {
        FirebaseDatabase.getInstance("https://sdmuhammadiyah-d3ddc-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_login)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showToast("Masukkan Username dan Password!")
                return@setOnClickListener
            }

            validateLogin(username, password)
        }
    }

    private fun validateLogin(username: String, password: String) {
        // Membaca data dari Realtime Database
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isValidUser = false
                var userFullName: String? = null
                var userId: String? = null
                var mataPelajaran: String? = null // Perbaikan: Simpan mata pelajaran di luar loop

                for (userSnapshot in snapshot.children) {
                    val dbUsername = userSnapshot.child("username").getValue(String::class.java)
                    val dbPassword = userSnapshot.child("password").getValue(String::class.java)
                    val fullName = userSnapshot.child("nama").getValue(String::class.java)
                    val mata = userSnapshot.child("mata_pelajaran").getValue(String::class.java)
                    val id = userSnapshot.child("id").getValue(String::class.java)

                    if (dbUsername == username && dbPassword == password) {
                        isValidUser = true
                        userFullName = fullName
                        userId = id
                        mataPelajaran = mata // Perbaikan: Simpan nilai mata_pelajaran di variabel ini
                        break
                    }
                }

                if (isValidUser) {
                    val displayName = userFullName ?: username
                    showToast("Login Berhasil! Selamat datang, $displayName")
                    navigateToProfileActivity(userFullName ?: username, username, userId ?: "Unknown", mataPelajaran)
                } else {
                    showToast("Username atau Password salah!")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}", error.toException())
                showToast("Terjadi kesalahan saat mengakses database.")
            }
        })
    }


    private fun navigateToProfileActivity(userFullName: String, userName: String, userId: String, mataPelajaran: String?) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("USER_NAME", userFullName)
        intent.putExtra("USERNAME", userName)
        intent.putExtra("MATA_PELAJARAN", mataPelajaran) // Kirim data mata_pelajaran
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finish()
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

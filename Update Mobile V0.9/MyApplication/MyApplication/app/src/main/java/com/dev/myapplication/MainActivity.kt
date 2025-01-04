package com.dev.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val SPLASH_SCREEN_DURATION = 1500L // Durasi splash screen dalam milidetik
        private const val DATABASE_URL = "https://sdmuhammadiyah-d3ddc-default-rtdb.asia-southeast1.firebasedatabase.app"
    }

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi Realtime Database dengan URL yang benar
        database = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users")

        lifecycleScope.launch {
            val dbConnectionJob = async(Dispatchers.IO) { connectToRealtimeDatabase() }
            delay(SPLASH_SCREEN_DURATION) // Menunggu splash screen selesai
            navigateToLogin() // Navigasi ke halaman login
            dbConnectionJob.await() // Menunggu operasi database selesai
        }
    }

    /**
     * Fungsi untuk mengambil data dari Realtime Database
     */
    private suspend fun connectToRealtimeDatabase() {
        try {
            if (!isInternetAvailable(this)) {
                Log.e(TAG, "No internet connection.")
                return
            }

            // SupervisorScope untuk menangani kesalahan tanpa membatalkan coroutine lainnya
            supervisorScope {
                val snapshot = database.get().await() // Mendapatkan data Realtime Database
                if (!snapshot.exists()) {
                    Log.d(TAG, "No data found under 'users' node.")
                } else {
                    for (child in snapshot.children) {
                        val userData = child.value as? Map<*, *> ?: continue
                        Log.d(TAG, "Data: $userData")
                    }
                }
            }
        } catch (e: Exception) { // Menangkap semua jenis exception
            Log.e(TAG, "Error connecting to Realtime Database.", e)
        }
    }

    /**
     * Navigasi ke halaman login
     */
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /**
     * Mengecek apakah koneksi internet tersedia
     */
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}

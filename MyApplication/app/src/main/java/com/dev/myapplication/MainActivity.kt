package com.dev.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val SPLASH_SCREEN_DURATION = 700L // Duration for splash screen in milliseconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start the splash screen flow
        lifecycleScope.launch {
            delay(SPLASH_SCREEN_DURATION)
            handleDatabaseConnection()
        }
    }

    // Handle database connection and navigate to the login screen
    private suspend fun handleDatabaseConnection() {
        try {
            // Connect to the database in the IO thread
            withContext(Dispatchers.IO) {
                DatabaseConnection.connectToDatabase()
                Log.d(TAG, "Database connection successful.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to database.", e)
        } finally {
            // Navigate to the login screen regardless of the connection outcome
            navigateToLogin()
        }
    }

    // Method to navigate to the login screen
    private fun navigateToLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close MainActivity to prevent users from going back
    }
}

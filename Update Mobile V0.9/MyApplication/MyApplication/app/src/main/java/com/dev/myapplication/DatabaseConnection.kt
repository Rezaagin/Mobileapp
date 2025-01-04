package com.dev.myapplication

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

object DatabaseConnection {

    private const val TAG = "DatabaseConnection"
    private val db = FirebaseDatabase.getInstance().reference.child("Users") // Mengacu pada node "Users"

    // Function to fetch all users from Realtime Database
    suspend fun fetchAllUsers() {
        try {
            val snapshot = db.get().await()
            if (!snapshot.exists()) {
                Log.d(TAG, "No users found.")
                return
            }

            for (child in snapshot.children) {
                val id = child.child("id").value as? String
                val name = child.child("nama").value as? String
                val username = child.child("username").value as? String
                Log.d(TAG, "ID: $id, Name: $name, Username: $username")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch users from Realtime Database", e)
        }
    }

    // Function to validate a user (username & password) in Realtime Database
    suspend fun validateUser(username: String, password: String): Boolean {
        return try {
            val snapshot = db.orderByChild("username").equalTo(username).get().await()
            if (!snapshot.exists()) {
                Log.d(TAG, "No user found with username: $username")
                return false
            }

            for (child in snapshot.children) {
                val userPassword = child.child("password").value as? String
                if (userPassword == password) {
                    Log.d(TAG, "User validated: $username")
                    return true
                }
            }
            Log.d(TAG, "Invalid credentials for username: $username")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error validating user", e)
            false
        }
    }

    // Function to add a new user to Realtime Database
    suspend fun addUser(id: String, name: String, username: String, password: String) {
        val user = mapOf(
            "id" to id,
            "nama" to name,
            "username" to username,
            "password" to password
        )

        try {
            db.push().setValue(user).await()
            Log.d(TAG, "User added: $username")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add user", e)
        }
    }

    // Function to delete a user by ID in Realtime Database
    suspend fun deleteUser(userId: String) {
        try {
            val snapshot = db.orderByChild("id").equalTo(userId).get().await()
            if (!snapshot.exists()) {
                Log.d(TAG, "No user found with ID: $userId")
                return
            }

            for (child in snapshot.children) {
                child.ref.removeValue().await()
                Log.d(TAG, "User with ID $userId deleted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user", e)
        }
    }
}

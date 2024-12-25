package com.dev.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.myapplication.network.PostgreSQLClient
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
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
        val connection: Connection? = PostgreSQLClient.getConnection()
        if (connection != null) {
            try {
                val query = "SELECT FROM aabsen.users WHERE username = ? AND password = ?"
                val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                preparedStatement.setString(1, username)
                preparedStatement.setString(2, password)

                val resultSet: ResultSet = preparedStatement.executeQuery()
                if (resultSet.next()) {
                    val userName = resultSet.getString("username")
                    showToast("Login Berhasil! Selamat datang, $userName")
                    navigateToScanActivity(userName)
                } else {
                    showToast("Username atau Password salah!")
                }

                resultSet.close()
                preparedStatement.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                showToast("Terjadi kesalahan saat menghubungi server!")
            } finally {
                connection.close()
            }
        } else {
            showToast("Gagal terhubung ke database!")
        }
    }

    private fun navigateToScanActivity(userName: String) {
        val intent = Intent(this, ScanActivity::class.java)
        intent.putExtra("USER_NAME", userName)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

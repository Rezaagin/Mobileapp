package com.dev.myapplication

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object DatabaseConnection {

    // Connection details
    private const val URL = "jdbc:mysql://vittoria.id.domainesia.com:3306/sdmuha10_maindb?connectTimeout=30&ssl=false";
    private const val USER = "sdmuha10_admin"
    private const val PASSWORD = "B#~D2WZmp61L"

    init {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            Log.d("DatabaseConnection", "MySQL JDBC driver loaded successfully.")
        } catch (e: ClassNotFoundException) {
            Log.e("DatabaseConnection", "MySQL JDBC driver not found!", e)
        }
    }

    // Function to connect to the database and run a query
    suspend fun connectToDatabase() {
        withContext(Dispatchers.IO) {
            var connection: Connection? = null
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD)
                Log.d("DatabaseConnection", "Connected to the database!")

                // Example Query
                val query = "SELECT id, name, username FROM users"
                connection.createStatement().use { statement ->
                    statement.executeQuery(query).use { resultSet ->
                        while (resultSet.next()) {
                            val id = resultSet.getInt("id")
                            val name = resultSet.getString("name")
                            val username = resultSet.getString("username")
                            Log.d("DatabaseConnection", "ID: $id, Name: $name, Username: $username")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DatabaseConnection", "Failed to connect to the database!", e)
            } finally {
                connection?.close()
                Log.d("DatabaseConnection", "Database connection closed.")
            }
        }
    }

    // Function to validate user with parameterized query
    suspend fun validateUser(username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            var isValid = false
            val query = "SELECT * FROM users WHERE username = ? AND password = ?"

            try {
                DriverManager.getConnection(URL, USER, PASSWORD).use { connection ->
                    connection.prepareStatement(query).use { preparedStatement ->
                        preparedStatement.setString(1, username)
                        preparedStatement.setString(2, password)
                        preparedStatement.executeQuery().use { resultSet ->
                            isValid = resultSet.next()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DatabaseConnection", "Error validating user", e)
            }
            isValid
        }
    }
}

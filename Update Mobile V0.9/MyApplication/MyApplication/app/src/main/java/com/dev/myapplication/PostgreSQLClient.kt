package com.dev.myapplication.network

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object PostgreSQLClient {

    private const val URL = "jdbc:postgresql://vittoria.id.domainesia.com:5432/sdmuha10_maindb" // Ganti localhost dengan IP server
    private const val USER = "sdmuha10"
    private const val PASSWORD = ";)vY1v}Zhn0N"

    fun getConnection(): Connection? {
        return try {
            DriverManager.getConnection(URL, USER, PASSWORD)
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }
}

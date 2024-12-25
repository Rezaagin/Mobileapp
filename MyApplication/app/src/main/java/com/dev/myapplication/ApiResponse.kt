package com.dev.myapplication.network

/**
 * Generic API Response
 * @param T The type of data expected from the API response
 * @param success Indicates whether the API call was successful
 * @param message Optional message returned by the API (e.g., error or success details)
 * @param data The actual data returned by the API, can be null if the response doesn't contain any
 */
data class ApiResponse<T>(
    val success: Boolean, // Indicates if the request was successful
    val message: String? = null, // Optional message for status/error description
    val data: T? = null // Optional generic data
)

package com.dev.myapplication.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class AbstractApiCallback<T> : Callback<T> {

    // Dipanggil ketika respons berhasil dan valid
    abstract fun onSuccess(response: T)

    // Dipanggil ketika terjadi error pada respons atau server
    open fun onError(errorMessage: String?) {
        // Default: log error atau tampilkan pesan (bisa di-overwrite)
        println("API Error: $errorMessage")
    }

    // Dipanggil ketika terjadi kegagalan koneksi (misalnya jaringan)
    open fun onFailure(t: Throwable) {
        // Default: log error atau tampilkan pesan (bisa di-overwrite)
        println("Connection Failure: ${t.message}")
    }

    final override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful && response.body() != null) {
            onSuccess(response.body()!!)
        } else {
            onError(response.errorBody()?.string() ?: "Unknown error")
        }
    }

    final override fun onFailure(call: Call<T>, t: Throwable) {
        onFailure(t)
    }
}

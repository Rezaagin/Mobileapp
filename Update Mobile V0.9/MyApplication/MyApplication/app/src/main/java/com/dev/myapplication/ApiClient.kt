package com.dev.myapplication.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "vittoria.id.domainesia.com/api/"

    // Menambahkan OkHttpClient untuk log dan pengaturan timeout
    private val client by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Untuk mencatat detail request/response
        }

        OkHttpClient.Builder()
            .addInterceptor(logging) // Logging Interceptor untuk debugging
            .connectTimeout(30, TimeUnit.SECONDS) // Timeout koneksi
            .readTimeout(30, TimeUnit.SECONDS) // Timeout membaca data
            .writeTimeout(30, TimeUnit.SECONDS) // Timeout menulis data
            .build()
    }

    // Instance Retrofit
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Converter untuk JSON
            .client(client) // OkHttpClient dengan konfigurasi
            .build()
            .create(ApiService::class.java) // Menghubungkan dengan ApiService
    }
}

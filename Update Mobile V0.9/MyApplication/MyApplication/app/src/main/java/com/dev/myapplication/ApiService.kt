package com.dev.myapplication.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface API Service untuk menghubungkan aplikasi dengan API backend.
 */
interface ApiService {

    /**
     * Endpoint untuk memindai kode QR dan mengirimkan data hasil pemindaian ke server.
     * @param scanData Data hasil pemindaian (termasuk scannedValue, latitude, longitude, dan timestamp).
     * @return ApiResponse berisi Unit jika berhasil.
     */
    @POST("submit-scan-data") // Pastikan endpoint sesuai dengan API backend Anda
    fun scanQRCode(@Body scanData: Map<String, String>): Call<ApiResponse<Unit>>
}

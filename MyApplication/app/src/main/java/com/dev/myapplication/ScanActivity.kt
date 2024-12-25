package com.dev.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dev.myapplication.network.ApiResponse
import com.dev.myapplication.network.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.zxing.integration.android.IntentIntegrator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ScanActivity : AppCompatActivity() {

    private lateinit var scanQrButton: Button
    private lateinit var scannedValueTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequestCode = 100

    private val qrScannerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
                if (intentResult?.contents != null) {
                    val scannedValue = intentResult.contents
                    scannedValueTextView.text = "Scanned Value: $scannedValue"
                    sendScannedValueToApi(scannedValue)
                } else {
                    showToast("Scan dibatalkan.")
                }
            } else {
                showToast("Scan gagal.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        // Inisialisasi view
        scanQrButton = findViewById(R.id.Scanqr)
        scannedValueTextView = findViewById(R.id.scannedValueTv)

        // Inisialisasi lokasi
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Periksa izin lokasi
        if (!checkLocationPermission()) {
            requestLocationPermission()
        }

        // Tombol untuk memulai scanner
        scanQrButton.setOnClickListener {
            startQRScanner()
        }
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Arahkan kamera ke QR Code")
            setBeepEnabled(true)
            setOrientationLocked(false)
        }
        qrScannerLauncher.launch(integrator.createScanIntent())
    }

    private fun sendScannedValueToApi(scannedValue: String) {
        if (!checkLocationPermission()) {
            showToast("Izin lokasi diperlukan untuk mengirim data.")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                showToast("Tidak dapat mendapatkan lokasi.")
                return@addOnSuccessListener
            }

            // Data lokasi
            val latitude = location.latitude
            val longitude = location.longitude
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // Data request
            val scanData = mapOf(
                "scannedValue" to scannedValue,
                "latitude" to latitude.toString(),
                "longitude" to longitude.toString(),
                "timestamp" to timestamp
            )

            // Panggil API
            val apiService = RetrofitClient.instance
            apiService.scanQRCode(scanData).enqueue(object : Callback<ApiResponse<Unit>> {
                override fun onResponse(call: Call<ApiResponse<Unit>>, response: Response<ApiResponse<Unit>>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        showToast("Data berhasil dikirim!")
                    } else {
                        val errorMessage = response.body()?.message ?: "Kesalahan tidak diketahui."
                        showToast("Gagal mengirim data: $errorMessage")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                    showToast("Kesalahan jaringan: ${t.message}")
                }
            })
        }.addOnFailureListener {
            showToast("Gagal mendapatkan lokasi: ${it.message}")
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            locationPermissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Izin lokasi diberikan.")
            } else {
                showToast("Izin lokasi ditolak.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

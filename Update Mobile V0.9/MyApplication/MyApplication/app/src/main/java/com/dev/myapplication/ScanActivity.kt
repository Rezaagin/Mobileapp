package com.dev.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import org.json.JSONObject

class ScanActivity : AppCompatActivity() {

    private lateinit var scanQrButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val database by lazy {
        // Inisialisasi Firebase Database dengan URL yang benar sesuai dengan region
        FirebaseDatabase.getInstance("https://sdmuhammadiyah-d3ddc-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
    }

    private lateinit var userFullName: String
    private lateinit var userId: String

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val VALID_API_KEY = "200OK"
        private const val MAX_DISTANCE_METERS = 100f // Maksimal jarak 100 meter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        scanQrButton = findViewById(R.id.Scanqr)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Ambil informasi user dari intent
        userFullName = intent.getStringExtra("USER_NAME") ?: "Unknown"
        userId = intent.getStringExtra("USER_ID") ?: "Unknown"

        // Set listener untuk tombol scan QR
        scanQrButton.setOnClickListener {
            startQRScanner()
        }
    }

    private fun startQRScanner() {
        val qrScanner = IntentIntegrator(this)
        qrScanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        qrScanner.setPrompt("Arahkan kamera ke QR Code")
        qrScanner.setCameraId(0)
        qrScanner.setBeepEnabled(true)
        qrScanner.setBarcodeImageEnabled(false)
        qrScanner.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showToast("Hasil scan kosong!")
            } else {
                try {
                    // Log hasil scan untuk debugging
                    val scannedDataRaw = result.contents
                    println("Hasil QR Code: $scannedDataRaw")

                    // Parsing JSON
                    val scannedData = JSONObject(scannedDataRaw)
                    val apiKey = scannedData.getString("api_key")

                    // Ambil lat dan lon sebagai string dan ubah "," menjadi "."
                    val scannedLat = scannedData.getString("lat").replace(",", ".").toDouble()
                    val scannedLng = scannedData.getString("lon").replace(",", ".").toDouble()

                    if (apiKey == VALID_API_KEY) {
                        validateLocationAndSendData(scannedLat, scannedLng)
                    } else {
                        showToast("API Key tidak valid!")
                    }
                } catch (e: Exception) {
                    showToast("Format QR Code tidak valid!")
                    e.printStackTrace() // Cetak kesalahan parsing untuk debugging
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun validateLocationAndSendData(scannedLat: Double, scannedLng: Double) {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = Location("").apply {
                            latitude = location.latitude
                            longitude = location.longitude
                        }
                        val scannedLocation = Location("").apply {
                            latitude = scannedLat
                            longitude = scannedLng
                        }

                        // Membandingkan hanya angka pokok (integer) dari koordinat
                        val userLatInt = location.latitude.toInt()
                        val userLngInt = location.longitude.toInt()

                        val scannedLatInt = scannedLat.toInt()
                        val scannedLngInt = scannedLng.toInt()

                        // Cek jika angka pokok sama
                        if (userLatInt == scannedLatInt && userLngInt == scannedLngInt) {
                            saveDataToFirebase(location.latitude, location.longitude)
                        } else {
                            showToast("Anda berada di luar jangkauan lokasi! Jarak: ${"%.2f".format(userLocation.distanceTo(scannedLocation))} meter.")
                        }
                    } else {
                        showToast("Lokasi tidak tersedia.")
                    }
                }
                .addOnFailureListener {
                    showToast("Gagal mendapatkan lokasi.")
                }
        } else {
            showToast("Izin lokasi diperlukan untuk fitur ini.")
        }
    }

    private fun saveDataToFirebase(latitude: Double, longitude: Double) {
        val absensiRef = database.child("users").child(userId)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // Generate ID unik untuk setiap absensi
        val newAbsensiRef = absensiRef.push()

        absensiRef.get().addOnSuccessListener { snapshot ->
            var isExistingAbsensi = false
            var absensiIdToUpdate: String? = null

            // Periksa apakah sudah ada absensi pada tanggal ini
            snapshot.children.forEach { child ->
                val tanggal = child.child("tanggal").value as? String
                if (tanggal == currentDate) {
                    isExistingAbsensi = true
                    absensiIdToUpdate = child.key
                }
            }

            if (isExistingAbsensi) {
                // Update jam keluar
                absensiIdToUpdate?.let { id ->
                    absensiRef.child(id).child("jam_keluar").setValue(currentTime)
                        .addOnSuccessListener {
                            showToast("Jam keluar berhasil dicatat!")
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showToast("Gagal mencatat jam keluar: ${e.message}")
                        }
                }
            } else {
                // Simpan data absensi baru
                val absensiData = mapOf(
                    "id_guru" to userId,
                    "nama_guru" to userFullName,
                    "lokasi" to getCityName(latitude, longitude),
                    "tanggal" to currentDate,
                    "jam_masuk" to currentTime
                )
                newAbsensiRef.setValue(absensiData)
                    .addOnSuccessListener {
                        showToast("Jam masuk berhasil dicatat!")
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showToast("Gagal mencatat jam masuk: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            showToast("Gagal memeriksa data absensi: ${e.message}")
        }
    }



    private fun getCityName(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.get(0)?.locality ?: "Lokasi tidak diketahui"
        } catch (e: Exception) {
            "Lokasi tidak diketahui"
        }
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

package com.dev.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val database by lazy {
        FirebaseDatabase.getInstance("https://sdmuhammadiyah-d3ddc-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("kehadiran")
    }

    private lateinit var userFullName: String
    private lateinit var userId: String
    private lateinit var mata: String

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val VALID_API_KEY = "200OK"
        private const val MAX_DISTANCE_METERS = 10000f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Ambil data dari Intent
        userFullName = intent.getStringExtra("USER_NAME") ?: "Unknown"
        userId = intent.getStringExtra("USER_ID") ?: "Unknown"
        mata = intent.getStringExtra("MATA_PELAJARAN") ?: "Unknown" // Pastikan key sama persis

        // Temukan View
        val idGuruTextView = findViewById<TextView>(R.id.Idguru)
        val namaGuruTextView = findViewById<TextView>(R.id.Namaguru)
        val scanButton = findViewById<Button>(R.id.scanButton)

        // Setel data ke View
        idGuruTextView.text = mata // Menampilkan mata pelajaran pada TextView idGuru
        namaGuruTextView.text = userFullName // Menampilkan nama guru pada TextView namaGuru

        // Set listener untuk tombol scan QR
        scanButton.setOnClickListener {
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
                    val scannedDataRaw = result.contents
                    val scannedData = JSONObject(scannedDataRaw)
                    val apiKey = scannedData.getString("api_key")
                    val scannedLat = scannedData.getString("lat").toDouble()
                    val scannedLng = scannedData.getString("lon").toDouble()

                    if (apiKey == VALID_API_KEY) {
                        validateLocationAndSendData(scannedLat, scannedLng)
                    } else {
                        showToast("API Key tidak valid!")
                    }
                } catch (e: Exception) {
                    showToast("Format QR Code tidak valid!")
                    e.printStackTrace()
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

                        val distance = userLocation.distanceTo(scannedLocation)
                        if (distance <= MAX_DISTANCE_METERS) {
                            saveDataToFirebase(location.latitude, location.longitude)
                        } else {
                            showToast("Anda berada di luar jangkauan lokasi! Jarak: ${"%.2f".format(distance)} meter.")
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
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newAbsensiRef = database.push()

        database.get().addOnSuccessListener { snapshot ->
            var isExistingAbsensi = false
            var absensiIdToUpdate: String? = null

            snapshot.children.forEach { child ->
                val idGuru = child.child("id_guru").value as? String
                val tanggal = child.child("tanggal").value as? String
                if (idGuru == userId && tanggal == currentDate) {
                    isExistingAbsensi = true
                    absensiIdToUpdate = child.key
                }
            }

            if (isExistingAbsensi) {
                absensiIdToUpdate?.let { id ->
                    database.child(id).child("jam_keluar").setValue(currentTime)
                        .addOnSuccessListener {
                            showToast("Jam keluar berhasil dicatat!")
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showToast("Gagal mencatat jam keluar: ${e.message}")
                        }
                }
            } else {
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
        return if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

package com.dev.myapplication



import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.dev.myapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginAActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Gunakan findViewById untuk mengakses elemen
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fies", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(email: String, password: String) {
        val request = LoginRequest(email, password)

//        RetrofitClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
//            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
//                if (response.isSuccessful) {
//                    val user = response.body()?.user
//                    val token = response.body()?.token
//
//                    Toast.makeText(this@LoginAActivity, "Welcome ${user?.name}", Toast.LENGTH_SHORT).show()
//                    // Save token if needed for future requests
//                } else {
//                    Toast.makeText(this@LoginAActivity, "Login failed", Toast.LENGTH_SHORT).show()
//                }
//            }

//            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//                Toast.makeText(this@LoginAActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
    }
}
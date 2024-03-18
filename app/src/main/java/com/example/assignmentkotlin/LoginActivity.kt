package com.example.assignmentkotlin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where

class LoginActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val realm = Realm.getDefaultInstance()
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            Log.d("username", "$username, password $password")

            if (loginUser(username, password)) {
                fetchCurrentLocation { locationData ->
                    val userId = getUserId(username, password, this)
                    saveUserAndLocation(this, userId, locationData)
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCurrentLocation(callback: (LocationData?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            callback(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val locationData = location?.run { LocationData(latitude, longitude) }
            Log.d("Location", "Latitude: ${locationData?.latitude}, Longitude: ${locationData?.longitude}")
            callback(locationData)
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to fetch location", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun saveUserAndLocation(context: Context, userId: Int, locationData: LocationData?) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val latitude = locationData?.latitude?.toFloat() ?: 0.0f
        val longitude = locationData?.longitude?.toFloat() ?: 0.0f
        editor.putInt("userId", userId)
        editor.putFloat("latitude", latitude)
        editor.putFloat("longitude", longitude)
        editor.apply()
        Log.d("Location", "User ID: $userId, Latitude: $latitude, Longitude: $longitude")
    }



    private fun loginUser(username: String, password: String): Boolean {
        val realm = Realm.getDefaultInstance()
        val user = realm.where<User>()
            .equalTo("username", username)
            .equalTo("password", password)
            .findFirst()
        return user != null
    }

    private fun getUserId(username: String, password: String, context: Context): Int {
        val realm = Realm.getDefaultInstance()
        val user = realm.where<User>()
            .equalTo("username", username)
            .equalTo("password", password)
            .findFirst()

        val userId = user?.id ?: -1

        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("userIdLogin", userId)
        editor.apply()

        return userId
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }
}

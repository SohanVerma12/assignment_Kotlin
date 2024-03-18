package com.example.assignmentkotlin

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.Manifest

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.widget.ListView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationRecyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var realm: Realm
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        realm = Realm.getDefaultInstance()

        locationRecyclerView = findViewById(R.id.locationRecyclerView)
        locationAdapter = LocationAdapter { position ->
            showMapDialog(locationAdapter.locations[position])
        }
        locationRecyclerView.layoutManager = LinearLayoutManager(this)
        locationRecyclerView.adapter = locationAdapter

        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        startLocationUpdates()
        setCurrentUser()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_switch_user -> {
                showUserSwitchDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun startLocationUpdates() {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchCurrentLocation(this@HomeActivity) { locationData ->
                    locationData?.let {
                        locationAdapter.addLocation(it)
                        handler.postDelayed(this, 1 * 60 * 1000) // Trigger every 5 minutes
                    }
                }
            }
        }, 0)
    }

    private fun fetchCurrentLocation(context: Context, callback: (LocationData?) -> Unit) {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        // Check if the app has permission to access the device's location
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, proceed to fetch the location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Location is available, create a LocationData object
                        val locationData = LocationData(location.latitude, location.longitude)
                        // Pass the location data to the callback function
                        callback(locationData)
                    } else {
                        // Location is not available
                        callback(null)
                    }
                }
                .addOnFailureListener { exception ->
                    // Failed to fetch location
                    callback(null)
                }
        } else {
            // App does not have permission to access location, handle accordingly
            // You can request permission here or handle it in your activity
            callback(null)
        }
    }

    private fun showMapDialog(locationData: LocationData) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_map)

        val mapView = dialog.findViewById<MapView>(R.id.mapView)
        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMapAsync { map ->
            val latLng = LatLng(locationData.latitude, locationData.longitude)
            Log.d("latlng","$latLng, ${locationData.longitude}, ${locationData.latitude}")
            map.addMarker(MarkerOptions().position(latLng).title("Location"))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }

        dialog.show()
    }

    private fun showUserSwitchDialog() {
        val users = realm.where(User::class.java).findAll() // Fetch all users from the database
        val userNames = users.map { it.username }.toTypedArray() // Extract usernames from User objects

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_switch_user)

        val userListView = dialog.findViewById<ListView>(R.id.userListView) // Define userListView here
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userNames)
        userListView.adapter = adapter
        userListView.setOnItemClickListener { _, _, position, _ ->
            // Update current user in SharedPreferences
            val selectedUserId = users[position]?.id // Get the selected user's ID from the database
            selectedUserId?.let { sharedPreferences.edit().putInt("userIdLogin", it).apply() }
            setCurrentUser()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun setCurrentUser() {
        val userId = sharedPreferences.getInt("userIdLogin", 0) // Get userIdLogin from SharedPreferences
        Log.d("userId1234", "$userId")

        // Perform Realm database query on the main thread
        val user = realm.where(User::class.java).equalTo("id", userId).findFirst()
        val userName = user?.username ?: "Unknown User"
        Log.d("userName1234", userName)
        supportActionBar?.title = "Welcome, $userName"

        // Show a Toast message with the username
        runOnUiThread {
            Toast.makeText(this@HomeActivity, "Welcome, $userName", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}

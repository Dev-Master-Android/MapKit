package com.example.mapkit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener

class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Initialize the MapKit library
        MapKitFactory.initialize(this)

        mapView = findViewById(R.id.mapview)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                val userLocation = Point(location.latitude, location.longitude)

                // Move the camera to the current coordinates
                mapView.map.move(
                    CameraPosition(userLocation, 14.0f, 0.0f, 0.0f),
                    com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 5f),
                    null
                )

                // Add a marker at the current location
                val placemark: PlacemarkMapObject = mapView.map.mapObjects.addPlacemark(userLocation)
                placemark.setIcon(ImageProvider.fromBitmap(convertSvgToDrawable(R.drawable.ic_location_pin)))
            }
        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun convertSvgToDrawable(icPin: Int): Bitmap {
        val drawable = resources.getDrawable(icPin, theme)
        val svg = drawable as android.graphics.drawable.VectorDrawable
        val bitmap = Bitmap.createBitmap(
            svg.intrinsicWidth,
            svg.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        svg.setBounds(0, 0, canvas.width, canvas.height)
        svg.draw(canvas)
        return bitmap
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            }
        }
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }
}

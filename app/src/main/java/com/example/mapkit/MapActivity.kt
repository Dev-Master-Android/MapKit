package com.example.mapkit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mapkit.R
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.example.mapkit.databinding.ActivityMapBinding
import com.yandex.mapkit.traffic.TrafficLayer

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var trafficLayer: TrafficLayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация MapKit
        MapKitFactory.initialize(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        trafficLayer = MapKitFactory.getInstance().createTrafficLayer(binding.mapview.mapWindow)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getCurrentLocation()
            }
        }

        // Проверка разрешения на доступ к местоположению
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation()
        }

        binding.fab.setOnClickListener {
            trafficLayer.setTrafficVisible(!trafficLayer.isTrafficVisible)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                val userLocation = Point(location.latitude, location.longitude)

                // Перемещение камеры к текущим координатам
                binding.mapview.map.move(
                    CameraPosition(userLocation, 14.0f, 0.0f, 0.0f),
                    com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 5f),
                    null
                )

                // Добавление метки в текущем местоположении
                val placemark: PlacemarkMapObject = binding.mapview.map.mapObjects.addPlacemark(userLocation)
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

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        binding.mapview.onStart()
        MapKitFactory.getInstance().onStart()
    }
}

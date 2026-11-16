package com.example.zelo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.zelo.databinding.ActivityMapaClinicaBinding
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

class MapaClinicaActivity : AppCompatActivity(), OnMapReadyCallback, Style.OnStyleLoaded {

    private lateinit var binding: ActivityMapaClinicaBinding
    private lateinit var mapa: MapLibreMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        binding = ActivityMapaClinicaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(map: MapLibreMap) {
        mapa = map
        mapa.setStyle("https://tiles.openfreemap.org/styles/liberty", this)
    }

    override fun onStyleLoaded(style: Style) {
        val clinicaLocal = LatLng(-15.867492948248856, -48.030878033892364)

        val ponto = Point.fromLngLat(clinicaLocal.longitude, clinicaLocal.latitude)
        val feature = Feature.fromGeometry(ponto)
        val fonte = GeoJsonSource("clinica-fonte", feature)

        style.addSource(fonte)

        val circulo = CircleLayer("clinica-pino", "clinica-fonte")
        circulo.setProperties(
            circleColor("#CB6440"),
            circleRadius(12f)
        )
        style.addLayer(circulo)

        val camera = CameraPosition.Builder()
            .target(clinicaLocal)
            .zoom(16.0)
            .build()

        mapa.cameraPosition = camera
    }


    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}
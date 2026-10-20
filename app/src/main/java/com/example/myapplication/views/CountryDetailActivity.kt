package com.example.myapplication.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.controllers.ControllerProvider
import com.example.myapplication.databinding.ActivityCountryDetailBinding
import com.example.myapplication.databinding.ItemInfoRowBinding
import com.example.myapplication.models.ApiResult
import com.example.myapplication.models.AppError
import com.example.myapplication.models.City
import com.example.myapplication.models.CityWeather
import com.example.myapplication.models.Country
import com.example.myapplication.models.WeatherInfo
import com.example.myapplication.models.WeatherState
import com.example.myapplication.views.adapters.CityWeatherAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File
import java.util.Locale

/**
 * Pantalla 3: detalle del país + ciudades principales + clima + mapa interactivo.
 * Cada bloque (país, ciudades, clima de cada ciudad) carga y falla de forma independiente.
 */
class CountryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountryDetailBinding
    private lateinit var cityAdapter: CityWeatherAdapter
    private val countryController = ControllerProvider.countryController
    private val cityController = ControllerProvider.cityController

    private var alpha2 = ""
    private var country: Country? = null
    private var cities: List<City> = emptyList()
    private val weather = mutableMapOf<Long, WeatherState>()
    private val markers = mutableMapOf<Long, Marker>()
    private var selectedCityId: Long? = null
    private var citiesJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initOsmdroid()
        binding = ActivityCountryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        alpha2 = intent.getStringExtra(EXTRA_ALPHA2).orEmpty()
        if (alpha2.isBlank()) {
            finish()
            return
        }
        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        binding.toolbar.title = name
        binding.tvCommonName.text = name
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.ivFlag.loadUrl("https://flags.restcountries.com/v5/w320/${alpha2.lowercase()}.png")

        setupMap()
        setupCityList()
        binding.btnRetryCountry.setOnClickListener { loadCountry() }
        binding.btnRetryCities.setOnClickListener { loadCities() }

        loadCountry()
        loadCities()
    }

    // ---------------------------------------------------------------- Mapa (osmdroid)

    private fun initOsmdroid() {
        val cfg = Configuration.getInstance()
        cfg.load(applicationContext, applicationContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        cfg.userAgentValue = "AtlasMundialApp/1.0 (student@desafio3.org)"
        val tileCache = File(cacheDir, "osmdroid/tiles")
        runCatching { tileCache.deleteRecursively() }
        cfg.osmdroidBasePath = File(cacheDir, "osmdroid")
        cfg.osmdroidTileCache = tileCache
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMap() {
        val map = binding.mapView
        val osmFr = org.osmdroid.tileprovider.tilesource.XYTileSource(
            "OSM_FR",
            0, 19, 256, ".png",
            arrayOf(
                "https://a.tile.openstreetmap.fr/osmfr/",
                "https://b.tile.openstreetmap.fr/osmfr/",
                "https://c.tile.openstreetmap.fr/osmfr/"
            )
        )
        map.setTileSource(osmFr)
        map.setMultiTouchControls(true)
        map.minZoomLevel = 2.0
        map.controller.setZoom(3.0)
        map.controller.setCenter(GeoPoint(15.0, 0.0))
        // El mapa vive dentro de un NestedScrollView: sin esto el scroll vertical "roba" los gestos del mapa.
        map.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                    v.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    private fun addMarkers() {
        val map = binding.mapView
        map.overlays.removeAll(markers.values.toSet())
        markers.clear()
        cities.forEach { city ->
            val marker = Marker(map).apply {
                position = GeoPoint(city.lat, city.lng)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = city.name
                snippet = getString(R.string.weather_loading)
                setOnMarkerClickListener { _, _ ->
                    selectCity(city, scrollToMap = false)
                    true
                }
            }
            markers[city.id] = marker
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    private fun fitMapToCities() {
        val map = binding.mapView
        val points = cities.map { GeoPoint(it.lat, it.lng) }
        when {
            points.size == 1 -> {
                map.controller.setZoom(8.0)
                map.controller.setCenter(points[0])
            }
            points.size > 1 -> {
                val box = BoundingBox.fromGeoPoints(points)
                map.post { map.zoomToBoundingBox(box.increaseByScale(1.3f), true, 48) }
            }
        }
    }

    private fun centerOnCountry(c: Country) {
        val lat = c.lat ?: return
        val lng = c.lng ?: return
        binding.mapView.controller.setZoom(5.0)
        binding.mapView.controller.setCenter(GeoPoint(lat, lng))
    }

    private fun markerText(state: WeatherState): String = when (state) {
        WeatherState.Loading -> getString(R.string.weather_loading)
        is WeatherState.Success -> getString(R.string.marker_weather_fmt, state.info.tempC, state.info.condition)
        is WeatherState.Error -> getString(R.string.weather_unavailable)
    }

    // ---------------------------------------------------------------- Lista de ciudades

    private fun setupCityList() {
        cityAdapter = CityWeatherAdapter(
            onClick = { city -> selectCity(city, scrollToMap = true) },
            onRetryWeather = { city -> loadWeatherFor(city) }
        )
        binding.rvCities.layoutManager = GridLayoutManager(this, resources.getInteger(R.integer.city_columns))
        binding.rvCities.adapter = cityAdapter
    }

    /** Reconstruye la lista y sincroniza los marcadores con el clima actual de cada ciudad. */
    private fun renderCities() {
        val capitals = country?.capitals.orEmpty()
        val items = cities.map { city ->
            CityWeather(
                city = city,
                isCapital = city.isCapital || capitals.any { it.equals(city.name, ignoreCase = true) },
                selected = city.id == selectedCityId,
                weather = weather[city.id] ?: WeatherState.Loading
            )
        }
        cityAdapter.submitList(items)

        items.forEach { item ->
            markers[item.city.id]?.let { marker ->
                marker.title = if (item.isCapital) "★ ${item.city.name}" else item.city.name
                marker.snippet = markerText(item.weather)
                if (item.selected) marker.showInfoWindow()
            }
        }
        binding.mapView.invalidate()
    }

    /** Al elegir una ciudad: el mapa se centra en ella y se abre el globo con nombre y clima. */
    private fun selectCity(city: City, scrollToMap: Boolean) {
        selectedCityId = city.id
        val map = binding.mapView
        InfoWindow.closeAllInfoWindowsOn(map)
        if (map.zoomLevelDouble < 8.0) map.controller.setZoom(9.0)
        map.controller.animateTo(GeoPoint(city.lat, city.lng))
        renderCities()
        if (scrollToMap) binding.scroll.smoothScrollTo(0, binding.mapCard.top)
    }

    // ---------------------------------------------------------------- Carga de datos

    private fun loadCountry() {
        binding.progressCountry.isVisible = true
        binding.layoutCountryError.isGone = true
        lifecycleScope.launch {
            val result = countryController.getCountryDetail(alpha2)
            binding.progressCountry.isGone = true
            when (result) {
                is ApiResult.Success -> {
                    country = result.data
                    bindCountry(result.data)
                    renderCities()
                    if (cities.isEmpty()) centerOnCountry(result.data)
                }
                is ApiResult.Failure -> {
                    binding.tvCountryError.text = result.error.toMessage(this@CountryDetailActivity)
                    binding.layoutCountryError.isVisible = true
                }
            }
        }
    }

    private fun loadCities() {
        citiesJob?.cancel()
        binding.progressCities.isVisible = true
        binding.layoutCitiesError.isGone = true

        citiesJob = lifecycleScope.launch {
            val result = cityController.getMainCities(alpha2)
            binding.progressCities.isGone = true
            when (result) {
                is ApiResult.Success -> {
                    cities = result.data
                    selectedCityId = null
                    weather.clear()
                    cities.forEach { weather[it.id] = WeatherState.Loading }
                    addMarkers()
                    renderCities()
                    fitMapToCities()
                    // Clima por ciudad, en paralelo; cada fallo queda aislado en su tarjeta.
                    launch {
                        cityController.loadWeatherForCities(cities) { city, res -> applyWeather(city, res) }
                    }
                }
                is ApiResult.Failure -> {
                    cities = emptyList()
                    weather.clear()
                    addMarkers()
                    renderCities()
                    binding.tvCitiesError.text = result.error.toMessage(this@CountryDetailActivity)
                    binding.btnRetryCities.isVisible = result.error != AppError.NoCities
                    binding.layoutCitiesError.isVisible = true
                }
            }
        }
    }

    private fun loadWeatherFor(city: City) {
        weather[city.id] = WeatherState.Loading
        renderCities()
        lifecycleScope.launch { applyWeather(city, cityController.getWeather(city)) }
    }

    private fun applyWeather(city: City, result: ApiResult<WeatherInfo>) {
        weather[city.id] = when (result) {
            is ApiResult.Success -> WeatherState.Success(result.data)
            is ApiResult.Failure -> WeatherState.Error(result.error)
        }
        renderCities()
    }

    // ---------------------------------------------------------------- Información del país

    private fun bindCountry(c: Country) {
        binding.toolbar.title = c.commonName
        binding.tvCommonName.text = c.commonName
        binding.tvOfficialName.text = c.officialName.orEmpty()
        binding.ivFlag.loadUrl(c.flagUrl)

        binding.infoContainer.removeAllViews()
        addRow(R.string.label_capitals, c.capitals.joinToString(", "))
        addRow(R.string.label_region, c.region)
        addRow(R.string.label_subregion, c.subregion)
        addRow(R.string.label_continents, c.continents.joinToString(", "))
        addRow(R.string.label_population, c.population?.formatted())
        addRow(R.string.label_area, c.areaKm2?.let { getString(R.string.area_fmt, it.toLong().formatted()) })
        addRow(R.string.label_codes, getString(R.string.codes_fmt, c.alpha2, c.alpha3 ?: "-"))
        addRow(R.string.label_currencies, c.currencies.joinToString("\n"))
        addRow(R.string.label_languages, c.languages.joinToString(", "))
        if (c.lat != null && c.lng != null) {
            addRow(R.string.label_coordinates, String.format(Locale.US, "%.4f, %.4f", c.lat, c.lng))
        }
        addRow(R.string.label_timezones, c.timezones.joinToString(", "))
        addRow(R.string.label_calling_codes, c.callingCodes.joinToString(", ") { "+$it" })
        addRow(
            R.string.label_driving_side,
            when (c.drivingSide?.lowercase()) {
                "left" -> getString(R.string.driving_left)
                "right" -> getString(R.string.driving_right)
                else -> null
            }
        )
        addRow(R.string.label_tlds, c.tlds.joinToString(", "))
    }

    private fun addRow(@StringRes label: Int, value: String?) {
        if (value.isNullOrBlank()) return
        val row = ItemInfoRowBinding.inflate(layoutInflater, binding.infoContainer, false)
        row.tvLabel.setText(label)
        row.tvValue.text = value
        binding.infoContainer.addView(row.root)
    }

    // ---------------------------------------------------------------- Ciclo de vida del mapa

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) binding.mapView.onResume()
    }

    override fun onPause() {
        if (::binding.isInitialized) binding.mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (::binding.isInitialized) binding.mapView.onDetach()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_ALPHA2 = "extra_alpha2"
        private const val EXTRA_NAME = "extra_name"

        fun newIntent(context: Context, alpha2: String, name: String) =
            Intent(context, CountryDetailActivity::class.java)
                .putExtra(EXTRA_ALPHA2, alpha2)
                .putExtra(EXTRA_NAME, name)
    }
}

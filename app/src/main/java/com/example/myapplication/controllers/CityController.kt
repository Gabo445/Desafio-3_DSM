package com.example.myapplication.controllers

import com.example.myapplication.BuildConfig
import com.example.myapplication.models.ApiResult
import com.example.myapplication.models.ApiService
import com.example.myapplication.models.AppError
import com.example.myapplication.models.AppException
import com.example.myapplication.models.City
import com.example.myapplication.models.WeatherInfo
import com.example.myapplication.models.api.GeoStatusDto
import com.example.myapplication.models.mappers.toCity
import com.example.myapplication.models.mappers.toWeatherInfo
import com.example.myapplication.network.ErrorMapper
import com.example.myapplication.network.GeoNamesService
import com.example.myapplication.network.WeatherService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.Locale

class CityController(
    private val geoNames: GeoNamesService,
    private val weather: WeatherService
) {

    /** Ciudades principales (GeoNames): hasta 10, ordenadas por población. */
    suspend fun getMainCities(iso2: String): ApiResult<List<City>> {
        if (BuildConfig.GEONAMES_USERNAME.isBlank()) {
            return ApiResult.Failure(AppError.MissingCredentials(ApiService.GEONAMES))
        }
        return try {
            val response = geoNames.searchCities(
                iso2 = iso2,
                featureClass = "P",
                cities = "cities15000",
                maxRows = 10,
                orderBy = "population",
                username = BuildConfig.GEONAMES_USERNAME
            )
            response.status?.let { throw AppException(it.toAppError()) }
            val cities = response.geonames.orEmpty().mapNotNull { it.toCity() }
            if (cities.isEmpty()) ApiResult.Failure(AppError.NoCities) else ApiResult.Success(cities)
        } catch (t: Throwable) {
            ApiResult.Failure(ErrorMapper.map(t, ApiService.GEONAMES))
        }
    }

    /** Clima actual (WeatherAPI) de UNA ciudad, consultando por latitud,longitud. */
    suspend fun getWeather(city: City): ApiResult<WeatherInfo> {
        if (BuildConfig.WEATHERAPI_KEY.isBlank()) {
            return ApiResult.Failure(AppError.MissingCredentials(ApiService.WEATHER))
        }
        return try {
            val q = String.format(Locale.US, "%.4f,%.4f", city.lat, city.lng)
            val info = weather.getCurrent(BuildConfig.WEATHERAPI_KEY, q, "es").toWeatherInfo()
            if (info == null) ApiResult.Failure(AppError.BadResponse) else ApiResult.Success(info)
        } catch (t: Throwable) {
            ApiResult.Failure(ErrorMapper.map(t, ApiService.WEATHER))
        }
    }

    /**
     * Consulta el clima de todas las ciudades en paralelo (máx. 3 simultáneas).
     * Cada ciudad se resuelve de forma independiente: si una falla, las demás continúan.
     */
    suspend fun loadWeatherForCities(
        cities: List<City>,
        onEach: (City, ApiResult<WeatherInfo>) -> Unit
    ) = coroutineScope {
        val gate = Semaphore(3)
        cities.map { city ->
            async {
                val result = gate.withPermit { getWeather(city) }
                onEach(city, result)
            }
        }.awaitAll()
    }

    private fun GeoStatusDto.toAppError(): AppError = when (value) {
        10 -> AppError.InvalidCredentials(ApiService.GEONAMES)
        18, 19, 20 -> AppError.RateLimited(ApiService.GEONAMES)
        else -> AppError.Server(ApiService.GEONAMES, value?.toString(), message)
    }
}

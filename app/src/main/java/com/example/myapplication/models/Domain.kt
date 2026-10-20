package com.example.myapplication.models

import androidx.annotation.StringRes

data class Region(
    /** Valor exacto que se envía a REST Countries (?region=...). */
    val apiName: String,
    @StringRes val nameRes: Int,
    val emoji: String
)

data class Country(
    val alpha2: String,
    val alpha3: String?,
    val commonName: String,
    val officialName: String?,
    val capitals: List<String>,
    val region: String?,
    val subregion: String?,
    val continents: List<String>,
    val population: Long?,
    val areaKm2: Double?,
    val flagUrl: String,
    val lat: Double?,
    val lng: Double?,
    val currencies: List<String>,
    val languages: List<String>,
    val timezones: List<String>,
    val callingCodes: List<String>,
    val tlds: List<String>,
    val drivingSide: String?
)

data class City(
    val id: Long,
    val name: String,
    val population: Long,
    val lat: Double,
    val lng: Double,
    val isCapital: Boolean
)

data class WeatherInfo(
    val tempC: Double,
    val tempF: Double,
    val condition: String,
    val iconUrl: String?,
    val windKph: Double,
    val windMph: Double,
    val humidity: Int,
    val lastUpdated: String
)

sealed interface WeatherState {
    data object Loading : WeatherState
    data class Success(val info: WeatherInfo) : WeatherState
    data class Error(val error: AppError) : WeatherState
}

/** Elemento que pinta la lista de ciudades: ciudad + capital resaltada + clima independiente. */
data class CityWeather(
    val city: City,
    val isCapital: Boolean,
    val selected: Boolean,
    val weather: WeatherState
)

package com.example.myapplication.models.api

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val location: WeatherLocationDto?,
    val current: WeatherCurrentDto?
)

data class WeatherLocationDto(
    val name: String?,
    val country: String?,
    val localtime: String?
)

data class WeatherCurrentDto(
    @SerializedName("last_updated") val lastUpdated: String?,
    @SerializedName("temp_c") val tempC: Double?,
    @SerializedName("temp_f") val tempF: Double?,
    val condition: WeatherConditionDto?,
    @SerializedName("wind_kph") val windKph: Double?,
    @SerializedName("wind_mph") val windMph: Double?,
    val humidity: Int?
)

data class WeatherConditionDto(val text: String?, val icon: String?, val code: Int?)

/** Cuerpo de error de WeatherAPI: { "error": { "code": 2006, "message": "..." } } */
data class WeatherErrorEnvelope(val error: WeatherErrorBody?)

data class WeatherErrorBody(val code: Int?, val message: String?)

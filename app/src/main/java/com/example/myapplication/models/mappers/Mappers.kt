package com.example.myapplication.models.mappers

import com.example.myapplication.models.City
import com.example.myapplication.models.Country
import com.example.myapplication.models.WeatherInfo
import com.example.myapplication.models.api.CountryDto
import com.example.myapplication.models.api.GeoNameDto
import com.example.myapplication.models.api.WeatherResponse

private const val FLAG_CDN = "https://flags.restcountries.com/v5/w320/"

fun CountryDto.toDomain(): Country? {
    val alpha2 = codes?.alpha2?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: return null
    val capitalNames = capitals.orEmpty().mapNotNull { it.name?.trim()?.takeIf { n -> n.isNotEmpty() } }
    val capitalCoords = capitals.orEmpty().firstNotNullOfOrNull { JsonUtils.latLng(it.coordinates) }

    val spa = JsonUtils.spanishName(names)
    val common = spa?.first?.takeIf { it.isNotBlank() } ?: names?.common?.takeIf { it.isNotBlank() } ?: alpha2
    val official = spa?.second?.takeIf { it.isNotBlank() } ?: names?.official

    return Country(
        alpha2 = alpha2,
        alpha3 = codes?.alpha3,
        commonName = common,
        officialName = official,
        capitals = capitalNames,
        region = region,
        subregion = subregion,
        continents = continents.orEmpty(),
        population = population?.toLong(),
        areaKm2 = area?.kilometers,
        flagUrl = flag?.urlPng?.takeIf { it.isNotBlank() } ?: "$FLAG_CDN${alpha2.lowercase()}.png",
        lat = coordinates?.lat ?: capitalCoords?.first,
        lng = coordinates?.lng ?: capitalCoords?.second,
        currencies = JsonUtils.currencies(currencies),
        languages = JsonUtils.languages(languages),
        timezones = timezones.orEmpty(),
        callingCodes = callingCodes.orEmpty(),
        tlds = tlds.orEmpty(),
        drivingSide = cars?.drivingSide
    )
}

fun GeoNameDto.toCity(): City? {
    val latitude = lat?.toDoubleOrNull() ?: return null
    val longitude = lng?.toDoubleOrNull() ?: return null
    val cityName = name?.takeIf { it.isNotBlank() } ?: toponymName?.takeIf { it.isNotBlank() } ?: return null
    return City(
        id = geonameId ?: cityName.hashCode().toLong(),
        name = cityName,
        population = population ?: 0L,
        lat = latitude,
        lng = longitude,
        isCapital = fcode == "PPLC"
    )
}

fun WeatherResponse.toWeatherInfo(): WeatherInfo? {
    val c = current ?: return null
    val tempC = c.tempC ?: return null
    return WeatherInfo(
        tempC = tempC,
        tempF = c.tempF ?: (tempC * 9.0 / 5.0 + 32.0),
        condition = c.condition?.text.orEmpty(),
        iconUrl = c.condition?.icon,
        windKph = c.windKph ?: 0.0,
        windMph = c.windMph ?: 0.0,
        humidity = c.humidity ?: 0,
        lastUpdated = c.lastUpdated.orEmpty()
    )
}

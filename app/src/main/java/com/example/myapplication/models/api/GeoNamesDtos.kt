package com.example.myapplication.models.api

/** Respuesta de /searchJSON. GeoNames devuelve errores con HTTP 200 dentro de "status". */
data class GeoNamesResponse(
    val totalResultsCount: Int?,
    val geonames: List<GeoNameDto>?,
    val status: GeoStatusDto?
)

data class GeoStatusDto(val message: String?, val value: Int?)

/** lat y lng llegan como String en GeoNames. */
data class GeoNameDto(
    val geonameId: Long?,
    val name: String?,
    val toponymName: String?,
    val lat: String?,
    val lng: String?,
    val population: Long?,
    val fcode: String?,
    val countryCode: String?
)

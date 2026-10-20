package com.example.myapplication.models.api

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/** Envoltorio JSON:API de REST Countries v5: { "data": { "objects": [...], "meta": {...} } } */
data class CountriesEnvelope(
    val data: CountriesData?,
    val errors: List<ApiErrorItem>?
)

data class CountriesData(
    val objects: List<CountryDto>?,
    val meta: MetaDto?
)

data class MetaDto(
    val total: Int?,
    val count: Int?,
    val limit: Int?,
    val offset: Int?,
    val more: Boolean?
)

data class ApiErrorItem(val message: String?)

data class NamesDto(
    val common: String?,
    val official: String?,
    val translations: JsonElement? = null
)

/** La forma exacta de "coordinates" no está documentada con detalle: se parsea de forma defensiva. */
data class CapitalDto(val name: String?, val coordinates: JsonElement?)

data class CodesDto(
    @SerializedName("alpha_2") val alpha2: String?,
    @SerializedName("alpha_3") val alpha3: String?
)

data class FlagDto(
    val emoji: String?,
    @SerializedName("url_png") val urlPng: String?
)

data class AreaDto(val kilometers: Double?, val miles: Double?)

data class LatLngDto(val lat: Double?, val lng: Double?)

data class CarsDto(@SerializedName("driving_side") val drivingSide: String?)

data class CountryDto(
    val names: NamesDto?,
    val capitals: List<CapitalDto>?,
    val codes: CodesDto?,
    val flag: FlagDto?,
    val region: String?,
    val subregion: String?,
    val continents: List<String>?,
    val population: Double?,
    val area: AreaDto?,
    val coordinates: LatLngDto?,
    /** Objeto o arreglo según el esquema: se interpreta en JsonUtils. */
    val currencies: JsonElement?,
    val languages: JsonElement?,
    val timezones: List<String>?,
    @SerializedName("calling_codes") val callingCodes: List<String>?,
    val tlds: List<String>?,
    val cars: CarsDto?
)

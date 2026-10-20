package com.example.myapplication.network

import com.example.myapplication.models.api.CountriesEnvelope
import com.example.myapplication.models.api.GeoNamesResponse
import com.example.myapplication.models.api.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Base URL: https://api.restcountries.com/  — Auth: Authorization: Bearer <token> (interceptor). */
interface RestCountriesService {

    /** GET /countries/v5 (sin filtro): lista paginada de todos los países. */
    @GET("countries/v5")
    suspend fun getCountries(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("response_fields") fields: String
    ): CountriesEnvelope

    /** GET /countries/v5?region={region} */
    @GET("countries/v5")
    suspend fun getCountriesByRegion(
        @Query("region") region: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("response_fields") fields: String
    ): CountriesEnvelope

    /** GET /countries/v5/codes.alpha_2/{código} */
    @GET("countries/v5/codes.alpha_2/{code}")
    suspend fun getCountryByAlpha2(
        @Path("code") code: String,
        @Query("response_fields_omit") omit: String
    ): CountriesEnvelope
}

/** Base URL: https://secure.geonames.org/  — Auth: parámetro username. */
interface GeoNamesService {
    @GET("searchJSON")
    suspend fun searchCities(
        @Query("country") iso2: String,
        @Query("featureClass") featureClass: String,
        @Query("cities") cities: String,
        @Query("maxRows") maxRows: Int,
        @Query("orderby") orderBy: String,
        @Query("username") username: String,
        @Query("lang") lang: String = "es"
    ): GeoNamesResponse
}

/** Base URL: https://api.weatherapi.com/v1/  — Auth: parámetro key. */
interface WeatherService {
    @GET("current.json")
    suspend fun getCurrent(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("lang") lang: String
    ): WeatherResponse
}

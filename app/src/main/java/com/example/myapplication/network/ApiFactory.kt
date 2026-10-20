package com.example.myapplication.network

import com.example.myapplication.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiFactory {

    private const val REST_COUNTRIES_URL = "https://api.restcountries.com/"
    private const val GEONAMES_URL = "https://secure.geonames.org/"
    private const val WEATHER_URL = "https://api.weatherapi.com/v1/"

    private val baseClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val restCountries: RestCountriesService by lazy {
        val client = baseClient.newBuilder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer ${BuildConfig.RESTCOUNTRIES_API_TOKEN}")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            })
            .build()
        retrofit(REST_COUNTRIES_URL, client).create(RestCountriesService::class.java)
    }

    val geoNames: GeoNamesService by lazy {
        retrofit(GEONAMES_URL, baseClient).create(GeoNamesService::class.java)
    }

    val weather: WeatherService by lazy {
        retrofit(WEATHER_URL, baseClient).create(WeatherService::class.java)
    }
}

package com.example.myapplication.controllers

import com.example.myapplication.BuildConfig
import com.example.myapplication.models.ApiResult
import com.example.myapplication.models.ApiService
import com.example.myapplication.models.AppError
import com.example.myapplication.models.Country
import com.example.myapplication.models.api.CountryDto
import com.example.myapplication.models.mappers.toDomain
import com.example.myapplication.network.ErrorMapper
import com.example.myapplication.network.RestCountriesService

class CountryController(private val api: RestCountriesService) {

    private companion object {
        const val PAGE_SIZE = 100 // máximo del plan gratuito
        const val LIST_FIELDS = "names.common,names.official,names.translations,capitals,codes.alpha_2,codes.alpha_3,flag.url_png,region"
        const val DETAIL_OMIT = ""
    }

    suspend fun getCountriesByRegion(region: String): ApiResult<List<Country>> {
        if (BuildConfig.RESTCOUNTRIES_API_TOKEN.isBlank()) {
            return ApiResult.Failure(AppError.MissingCredentials(ApiService.REST_COUNTRIES))
        }
        return try {
            val all = mutableListOf<CountryDto>()
            var offset = 0
            do {
                val page = api.getCountriesByRegion(region, PAGE_SIZE, offset, LIST_FIELDS)
                val objects = page.data?.objects.orEmpty()
                all += objects
                offset += objects.size
                val hasMore = page.data?.meta?.more == true && objects.isNotEmpty()
            } while (hasMore)

            ApiResult.Success(all.mapNotNull { it.toDomain() }.sortedBy { it.commonName.lowercase() })
        } catch (t: Throwable) {
            ApiResult.Failure(ErrorMapper.map(t, ApiService.REST_COUNTRIES))
        }
    }

    suspend fun getCountryDetail(alpha2: String): ApiResult<Country> {
        if (BuildConfig.RESTCOUNTRIES_API_TOKEN.isBlank()) {
            return ApiResult.Failure(AppError.MissingCredentials(ApiService.REST_COUNTRIES))
        }
        return try {
            val response = api.getCountryByAlpha2(alpha2, DETAIL_OMIT)
            val country = response.data?.objects?.firstNotNullOfOrNull { it.toDomain() }
            if (country == null) ApiResult.Failure(AppError.CountryNotFound) else ApiResult.Success(country)
        } catch (t: Throwable) {
            ApiResult.Failure(ErrorMapper.map(t, ApiService.REST_COUNTRIES))
        }
    }
}

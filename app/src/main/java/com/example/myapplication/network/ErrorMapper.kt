package com.example.myapplication.network

import com.example.myapplication.models.ApiService
import com.example.myapplication.models.AppError
import com.example.myapplication.models.AppException
import com.example.myapplication.models.api.WeatherErrorEnvelope
import com.google.gson.Gson
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/** Traduce cualquier excepción de red/parseo a un AppError amigable. */
object ErrorMapper {

    private val gson = Gson()

    fun map(t: Throwable, service: ApiService): AppError = when (t) {
        is CancellationException -> throw t
        is AppException -> t.error
        is HttpException -> mapHttp(t, service)
        is SocketTimeoutException -> AppError.Timeout
        is InterruptedIOException -> AppError.Timeout
        is IOException -> AppError.NoConnection
        is JsonParseException -> AppError.BadResponse
        else -> AppError.Server(service, null, t.message)
    }

    private fun mapHttp(e: HttpException, service: ApiService): AppError {
        val code = e.code()
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()

        if (service == ApiService.WEATHER) {
            val apiCode = runCatching {
                gson.fromJson(body, WeatherErrorEnvelope::class.java)?.error?.code
            }.getOrNull()
            when (apiCode) {
                1002, 2006, 2008, 2009 -> return AppError.InvalidCredentials(service)
                2007 -> return AppError.RateLimited(service)
                1006 -> return AppError.LocationNotFound
            }
        }

        return when (code) {
            401 -> AppError.InvalidCredentials(service)
            403 ->
                if (service == ApiService.REST_COUNTRIES) AppError.RateLimited(service)
                else AppError.InvalidCredentials(service)
            404 ->
                if (service == ApiService.REST_COUNTRIES) AppError.CountryNotFound
                else AppError.LocationNotFound
            429 -> AppError.RateLimited(service)
            else -> AppError.Server(service, code.toString(), null)
        }
    }
}

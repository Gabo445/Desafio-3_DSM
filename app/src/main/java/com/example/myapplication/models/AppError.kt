package com.example.myapplication.models

enum class ApiService { REST_COUNTRIES, GEONAMES, WEATHER }

sealed class AppError {
    data object NoConnection : AppError()
    data object Timeout : AppError()
    data object NoCities : AppError()
    data object LocationNotFound : AppError()
    data object CountryNotFound : AppError()
    data object BadResponse : AppError()
    data class InvalidCredentials(val service: ApiService) : AppError()
    data class MissingCredentials(val service: ApiService) : AppError()
    data class RateLimited(val service: ApiService) : AppError()
    data class Server(val service: ApiService, val code: String?, val detail: String?) : AppError()
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val error: AppError) : ApiResult<Nothing>()
}

/** Se lanza internamente cuando una API responde HTTP 200 pero con un error en el cuerpo (GeoNames). */
class AppException(val error: AppError) : RuntimeException()

package com.example.myapplication.views

import android.content.Context
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import com.example.myapplication.R
import com.example.myapplication.models.ApiService
import com.example.myapplication.models.AppError
import android.view.View
import java.text.NumberFormat

/** Edge-to-edge (obligatorio con targetSdk 35+): evita que el contenido quede bajo las barras del sistema. */
fun View.applySystemBarsPadding() {
    val l = paddingLeft
    val t = paddingTop
    val r = paddingRight
    val b = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        v.setPadding(l + bars.left, t + bars.top, r + bars.right, b + bars.bottom)
        WindowInsetsCompat.CONSUMED
    }
}

/** Carga una imagen (bandera / ícono del clima) con Coil. Acepta URLs que empiezan por "//". */
fun ImageView.loadUrl(url: String?) {
    val normalized = when {
        url.isNullOrBlank() -> null
        url.startsWith("//") -> "https:$url"
        else -> url
    }
    if (normalized == null) {
        setImageDrawable(null)
    } else {
        load(normalized) { crossfade(true) }
    }
}

fun Long.formatted(): String = NumberFormat.getInstance().format(this)

fun ApiService.label(ctx: Context): String = ctx.getString(
    when (this) {
        ApiService.REST_COUNTRIES -> R.string.service_rest_countries
        ApiService.GEONAMES -> R.string.service_geonames
        ApiService.WEATHER -> R.string.service_weather
    }
)

fun AppError.toMessage(ctx: Context): String = when (this) {
    AppError.NoConnection -> ctx.getString(R.string.error_no_connection)
    AppError.Timeout -> ctx.getString(R.string.error_timeout)
    AppError.NoCities -> ctx.getString(R.string.error_no_cities)
    AppError.LocationNotFound -> ctx.getString(R.string.error_location_not_found)
    AppError.CountryNotFound -> ctx.getString(R.string.error_country_not_found)
    AppError.BadResponse -> ctx.getString(R.string.error_bad_response)
    is AppError.InvalidCredentials -> {
        if (service == ApiService.GEONAMES) {
            ctx.getString(R.string.error_geonames_not_enabled, com.example.myapplication.BuildConfig.GEONAMES_USERNAME)
        } else {
            ctx.getString(R.string.error_invalid_credentials, service.label(ctx))
        }
    }
    is AppError.MissingCredentials -> ctx.getString(R.string.error_missing_credentials, service.label(ctx))
    is AppError.RateLimited -> ctx.getString(R.string.error_rate_limited, service.label(ctx))
    is AppError.Server -> ctx.getString(R.string.error_server, service.label(ctx), code ?: detail ?: "?")
}

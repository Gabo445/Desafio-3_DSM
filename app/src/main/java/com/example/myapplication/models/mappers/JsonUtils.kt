package com.example.myapplication.models.mappers

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Parseo defensivo para los campos de REST Countries v5 cuya forma interna
 * (monedas, idiomas, coordenadas de capital) puede variar entre objeto, arreglo o texto.
 */
internal object JsonUtils {

    fun text(e: JsonElement?): String? =
        if (e != null && e.isJsonPrimitive) e.asString.trim().takeIf { it.isNotEmpty() } else null

    private fun number(o: JsonObject, vararg keys: String): Double? {
        for (k in keys) {
            val v = o.get(k)
            if (v != null && v.isJsonPrimitive) {
                val d = runCatching { v.asDouble }.getOrNull()
                if (d != null) return d
            }
        }
        return null
    }

    fun latLng(e: JsonElement?): Pair<Double, Double>? {
        if (e == null || e.isJsonNull) return null
        if (e.isJsonObject) {
            val o = e.asJsonObject
            val lat = number(o, "lat", "latitude")
            val lng = number(o, "lng", "lon", "long", "longitude")
            if (lat != null && lng != null) return lat to lng
        } else if (e.isJsonArray) {
            val a = e.asJsonArray
            if (a.size() >= 2) {
                val lat = runCatching { a[0].asDouble }.getOrNull()
                val lng = runCatching { a[1].asDouble }.getOrNull()
                if (lat != null && lng != null) return lat to lng
            }
        }
        return null
    }

    private fun currencyLabel(code: String?, o: JsonObject): String? {
        val name = text(o.get("name"))
        val symbol = text(o.get("symbol"))
        val c = code ?: text(o.get("code"))
        val base = listOfNotNull(name, symbol?.let { "($it)" }).joinToString(" ")
        return when {
            base.isNotEmpty() && c != null -> "$base [$c]"
            base.isNotEmpty() -> base
            else -> c
        }
    }

    fun currencies(e: JsonElement?): List<String> {
        if (e == null || e.isJsonNull) return emptyList()
        val out = mutableListOf<String>()
        if (e.isJsonObject) {
            for ((key, value) in e.asJsonObject.entrySet()) {
                if (value.isJsonObject) currencyLabel(key, value.asJsonObject)?.let { out.add(it) }
                else text(value)?.let { out.add("$it [$key]") }
            }
        } else if (e.isJsonArray) {
            for (item in e.asJsonArray) {
                if (item.isJsonObject) currencyLabel(null, item.asJsonObject)?.let { out.add(it) }
                else text(item)?.let { out.add(it) }
            }
        }
        return out
    }

    private val languageKeys = listOf("name", "spanish_name", "spanish", "english_name", "english", "name_english", "label", "native_name")

    private val languageTranslations = mapOf(
        "english" to "Inglés", "spanish" to "Español", "french" to "Francés",
        "german" to "Alemán", "italian" to "Italiano", "portuguese" to "Portugués",
        "russian" to "Ruso", "chinese" to "Chino", "japanese" to "Japonés",
        "arabic" to "Árabe", "catalan" to "Catalán", "dutch" to "Holandés",
        "greek" to "Griego", "korean" to "Coreano", "hindi" to "Hindi",
        "polish" to "Polaco", "swedish" to "Sueco", "turkish" to "Turco"
    )

    private fun translateLanguage(lang: String): String {
        val key = lang.trim().lowercase()
        return languageTranslations[key] ?: lang
    }

    private fun languageLabel(o: JsonObject): String? {
        for (k in languageKeys) {
            text(o.get(k))?.let { return it }
        }
        for ((_, v) in o.entrySet()) {
            text(v)?.let { return it }
        }
        return null
    }

    fun languages(e: JsonElement?): List<String> {
        if (e == null || e.isJsonNull) return emptyList()
        val out = mutableListOf<String>()
        if (e.isJsonArray) {
            for (item in e.asJsonArray) {
                if (item.isJsonObject) languageLabel(item.asJsonObject)?.let { out.add(it) }
                else text(item)?.let { out.add(it) }
            }
        } else if (e.isJsonObject) {
            for ((_, v) in e.asJsonObject.entrySet()) {
                if (v.isJsonObject) languageLabel(v.asJsonObject)?.let { out.add(it) }
                else text(v)?.let { out.add(it) }
            }
        }
        return out.map { translateLanguage(it) }.distinct()
    }

    fun spanishName(names: com.example.myapplication.models.api.NamesDto?): Pair<String?, String?>? {
        val e = names?.translations ?: return null
        if (!e.isJsonObject) return null
        val tObj = e.asJsonObject
        val spa = tObj.getAsJsonObject("spa")
            ?: tObj.getAsJsonObject("es")
            ?: tObj.getAsJsonObject("spanish")
            ?: return null
        val common = text(spa.get("common"))
        val official = text(spa.get("official"))
        return if (common != null || official != null) common to official else null
    }
}

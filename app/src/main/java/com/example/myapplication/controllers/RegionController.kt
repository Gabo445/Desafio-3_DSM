package com.example.myapplication.controllers

import com.example.myapplication.R
import com.example.myapplication.models.Region

/** REST Countries v5 no expone un endpoint de regiones, por lo que se usa el conjunto oficial de regiones. */
object RegionController {
    fun getRegions(): List<Region> = listOf(
        Region("Africa", R.string.region_africa, "🌍"),
        Region("Americas", R.string.region_americas, "🌎"),
        Region("Asia", R.string.region_asia, "🌏"),
        Region("Europe", R.string.region_europe, "🏰"),
        Region("Oceania", R.string.region_oceania, "🏝️"),
        Region("Antarctic", R.string.region_antarctic, "🧊")
    )
}

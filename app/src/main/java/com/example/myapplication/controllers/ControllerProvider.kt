package com.example.myapplication.controllers

import com.example.myapplication.network.ApiFactory

/** Punto único de acceso a los controladores (las Vistas nunca tocan Retrofit directamente). */
object ControllerProvider {
    val countryController: CountryController by lazy { CountryController(ApiFactory.restCountries) }
    val cityController: CityController by lazy { CityController(ApiFactory.geoNames, ApiFactory.weather) }
}

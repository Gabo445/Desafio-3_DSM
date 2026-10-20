package com.example.myapplication.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemCityBinding
import com.example.myapplication.models.City
import com.example.myapplication.models.CityWeather
import com.example.myapplication.models.WeatherState
import com.example.myapplication.views.formatted
import com.example.myapplication.views.loadUrl
import com.example.myapplication.views.toMessage

class CityWeatherAdapter(
    private val onClick: (City) -> Unit,
    private val onRetryWeather: (City) -> Unit
) : ListAdapter<CityWeather, CityWeatherAdapter.VH>(Diff) {

    class VH(val binding: ItemCityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val b = holder.binding
        val ctx = b.root.context

        b.tvCityName.text = item.city.name
        b.tvCapitalBadge.isVisible = item.isCapital
        b.tvPopulation.text = ctx.getString(R.string.population_fmt, item.city.population.formatted())
        val highlighted = item.selected || item.isCapital
        val density = ctx.resources.displayMetrics.density
        b.cardCity.strokeWidth = ((if (highlighted) 2 else 1) * density).toInt()
        b.cardCity.strokeColor = if (highlighted) {
            ContextCompat.getColor(ctx, R.color.brand)
        } else {
            MaterialColors.getColor(b.cardCity, com.google.android.material.R.attr.colorOutlineVariant)
        }

        val state = item.weather
        b.progressWeather.isVisible = state is WeatherState.Loading
        b.layoutWeather.isVisible = state is WeatherState.Success
        b.layoutWeatherError.isVisible = state is WeatherState.Error

        when (state) {
            is WeatherState.Success -> {
                val w = state.info
                b.ivWeatherIcon.loadUrl(w.iconUrl)
                b.tvTemp.text = ctx.getString(R.string.weather_temp_fmt, w.tempC, w.tempF)
                b.tvCondition.text = w.condition
                b.tvWeatherDetails.text = ctx.getString(R.string.weather_wind_fmt, w.windKph, w.windMph) +
                    " · " + ctx.getString(R.string.weather_humidity_fmt, w.humidity)
                b.tvUpdated.text = ctx.getString(R.string.weather_updated_fmt, w.lastUpdated)
            }
            is WeatherState.Error -> b.tvWeatherError.text = state.error.toMessage(ctx)
            WeatherState.Loading -> Unit
        }

        b.cardCity.setOnClickListener { onClick(item.city) }
        b.btnRetryWeather.setOnClickListener { onRetryWeather(item.city) }
    }

    private object Diff : DiffUtil.ItemCallback<CityWeather>() {
        override fun areItemsTheSame(a: CityWeather, b: CityWeather) = a.city.id == b.city.id
        override fun areContentsTheSame(a: CityWeather, b: CityWeather) = a == b
    }
}

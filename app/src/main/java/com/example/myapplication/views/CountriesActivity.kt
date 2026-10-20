package com.example.myapplication.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.controllers.ControllerProvider
import com.example.myapplication.databinding.ActivityCountriesBinding
import com.example.myapplication.models.ApiResult
import com.example.myapplication.views.adapters.CountryAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Pantalla 2: países de la región seleccionada (nombre, bandera y capital). */
class CountriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountriesBinding
    private lateinit var adapter: CountryAdapter
    private var loadJob: Job? = null
    private var regionApiName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityCountriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        regionApiName = intent.getStringExtra(EXTRA_REGION_API).orEmpty()
        binding.toolbar.title = intent.getStringExtra(EXTRA_REGION_TITLE).orEmpty()
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = CountryAdapter { country ->
            startActivity(CountryDetailActivity.newIntent(this, country.alpha2, country.commonName))
        }
        binding.rvCountries.layoutManager = GridLayoutManager(this, resources.getInteger(R.integer.country_columns))
        binding.rvCountries.adapter = adapter
        binding.btnRetry.setOnClickListener { loadCountries() }

        loadCountries()
    }

    private fun loadCountries() {
        loadJob?.cancel()
        binding.progress.isVisible = true
        binding.layoutError.isGone = true
        binding.rvCountries.isGone = true

        loadJob = lifecycleScope.launch {
            val result = ControllerProvider.countryController.getCountriesByRegion(regionApiName)
            binding.progress.isGone = true
            when (result) {
                is ApiResult.Success -> {
                    if (result.data.isEmpty()) {
                        showMessage(getString(R.string.empty_countries), showRetry = false)
                    } else {
                        binding.rvCountries.isVisible = true
                        adapter.submitList(result.data)
                    }
                }
                is ApiResult.Failure -> showMessage(result.error.toMessage(this@CountriesActivity), showRetry = true)
            }
        }
    }

    private fun showMessage(message: String, showRetry: Boolean) {
        binding.tvError.text = message
        binding.btnRetry.isVisible = showRetry
        binding.layoutError.isVisible = true
    }

    companion object {
        private const val EXTRA_REGION_API = "extra_region_api"
        private const val EXTRA_REGION_TITLE = "extra_region_title"

        fun newIntent(context: Context, regionApiName: String, regionTitle: String) =
            Intent(context, CountriesActivity::class.java)
                .putExtra(EXTRA_REGION_API, regionApiName)
                .putExtra(EXTRA_REGION_TITLE, regionTitle)
    }
}

package com.example.myapplication.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.controllers.RegionController
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.views.adapters.RegionAdapter

/** Pantalla 1: lista de regiones. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        val adapter = RegionAdapter { region ->
            startActivity(CountriesActivity.newIntent(this, region.apiName, getString(region.nameRes)))
        }
        binding.rvRegions.layoutManager = GridLayoutManager(this, resources.getInteger(R.integer.region_columns))
        binding.rvRegions.adapter = adapter
        adapter.submitList(RegionController.getRegions())
    }
}

package com.example.myapplication.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemCountryBinding
import com.example.myapplication.models.Country
import com.example.myapplication.views.loadUrl

class CountryAdapter(
    private val onClick: (Country) -> Unit
) : ListAdapter<Country, CountryAdapter.VH>(Diff) {

    class VH(val binding: ItemCountryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val country = getItem(position)
        val ctx = holder.binding.root.context
        holder.binding.tvCountryName.text = country.commonName
        holder.binding.tvCapital.text =
            if (country.capitals.isEmpty()) ctx.getString(R.string.capital_none)
            else ctx.getString(R.string.capital_fmt, country.capitals.joinToString(", "))
        holder.binding.ivFlag.loadUrl(country.flagUrl)
        holder.binding.root.setOnClickListener { onClick(country) }
    }

    private object Diff : DiffUtil.ItemCallback<Country>() {
        override fun areItemsTheSame(a: Country, b: Country) = a.alpha2 == b.alpha2
        override fun areContentsTheSame(a: Country, b: Country) = a == b
    }
}

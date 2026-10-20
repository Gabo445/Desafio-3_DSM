package com.example.myapplication.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemRegionBinding
import com.example.myapplication.models.Region

class RegionAdapter(
    private val onClick: (Region) -> Unit
) : ListAdapter<Region, RegionAdapter.VH>(Diff) {

    class VH(val binding: ItemRegionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRegionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val region = getItem(position)
        holder.binding.tvEmoji.text = region.emoji
        holder.binding.tvRegionName.setText(region.nameRes)
        holder.binding.root.setOnClickListener { onClick(region) }
    }

    private object Diff : DiffUtil.ItemCallback<Region>() {
        override fun areItemsTheSame(a: Region, b: Region) = a.apiName == b.apiName
        override fun areContentsTheSame(a: Region, b: Region) = a == b
    }
}

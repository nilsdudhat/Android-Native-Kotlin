package com.practical.crawlapps.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practical.crawlapps.databinding.ItemBoxBinding

class BoxAdapter : RecyclerView.Adapter<BoxAdapter.ViewHolder>() {

    private var map = mapOf<Int, MutableMap<String, Int>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val box = map[position]
        holder.binding.map = box
    }

    override fun getItemCount(): Int {
        return map.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMap(map: Map<Int, MutableMap<String, Int>>) {
        this.map = map
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemBoxBinding) : RecyclerView.ViewHolder(binding.root)
}
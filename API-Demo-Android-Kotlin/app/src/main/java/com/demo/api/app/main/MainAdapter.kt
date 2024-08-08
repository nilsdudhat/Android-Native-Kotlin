package com.demo.api.app.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.api.app.databinding.ItemMainBinding

class MainAdapter(private val viewModel: MainViewModel) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    private var list: MutableList<Map<String, String>> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val main = list[position]
        holder.binding.main = main
        holder.binding.viewModel = viewModel
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMainList(apiList: MutableList<Map<String, String>>) {
        list = apiList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemMainBinding): RecyclerView.ViewHolder(binding.root)
}
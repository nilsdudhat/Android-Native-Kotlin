package com.demo.api.app.university

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.api.app.databinding.ItemUniversityBinding

class UniversityAdapter : RecyclerView.Adapter<UniversityAdapter.ViewHolder>() {

    private var list = ArrayList<University>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemUniversityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val university = list[position]
        holder.binding.university = university
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: ArrayList<University>) {
        this.list = list
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemUniversityBinding) : RecyclerView.ViewHolder(binding.root)
}
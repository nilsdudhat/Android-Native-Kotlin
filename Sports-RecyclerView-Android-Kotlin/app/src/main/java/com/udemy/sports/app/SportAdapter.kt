package com.udemy.sports.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.udemy.sports.app.databinding.ItemSportBinding

class SportAdapter : RecyclerView.Adapter<SportAdapter.ViewHolder>() {

    private var sportsList = ArrayList<SportModel>()

    class ViewHolder(val binding: ItemSportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemSportBinding =
            ItemSportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return sportsList.size
    }

    fun setSportsList(sportsList: ArrayList<SportModel>) {
        this.sportsList = sportsList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sportModel: SportModel = sportsList[position]

        holder.binding.imgSport.setImageResource(sportModel.image)
        holder.binding.txtSport.text = sportModel.name
    }
}
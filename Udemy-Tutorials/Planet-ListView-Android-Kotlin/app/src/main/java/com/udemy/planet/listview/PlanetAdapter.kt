package com.udemy.planet.listview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.udemy.planet.listview.databinding.AdapterPlanetBinding
import java.lang.StringBuilder

class PlanetAdapter(context: Context, planetList: List<PlanetModel>) :
    ArrayAdapter<PlanetModel>(context, R.layout.adapter_planet, planetList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val planetModel: PlanetModel? = getItem(position)

        if (planetModel != null) {
            val binding = AdapterPlanetBinding.inflate(LayoutInflater.from(context), parent, false)

            binding.imgPlanet.setImageResource(planetModel.planetImage)
            binding.txtPlanetName.text = planetModel.planetName
            binding.txtNumberMoons.text = StringBuilder().append(planetModel.moonsCount).append(" Moons")

            return binding.root
        }

        return super.getView(position, convertView, parent)
    }
}
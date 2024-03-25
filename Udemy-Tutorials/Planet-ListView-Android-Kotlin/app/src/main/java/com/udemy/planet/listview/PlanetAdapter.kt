package com.udemy.planet.listview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import java.lang.StringBuilder

class PlanetAdapter(context: Context, planetList: List<PlanetModel>) :
    ArrayAdapter<PlanetModel>(context, R.layout.adapter_planet, planetList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val planetModel: PlanetModel? = getItem(position)

        if (planetModel != null) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.adapter_planet, parent, false)

            val myVieHolder = MyViewHolder()

            myVieHolder.imgPlanet = view.findViewById(R.id.img_planet)
            myVieHolder.txtPlanetName = view.findViewById(R.id.txt_planet_name)
            myVieHolder.txtNumberOfMoons = view.findViewById(R.id.txt_number_moons)

            myVieHolder.imgPlanet.setImageResource(planetModel.planetImage)
            myVieHolder.txtPlanetName.text = planetModel.planetName
            myVieHolder.txtNumberOfMoons.text = StringBuilder().append(planetModel.moonsCount).append(" Moons")

            return view
        }

        return super.getView(position, convertView, parent)
    }

    class MyViewHolder {
        lateinit var txtPlanetName: TextView
        lateinit var txtNumberOfMoons: TextView
        lateinit var imgPlanet: ImageView
    }
}
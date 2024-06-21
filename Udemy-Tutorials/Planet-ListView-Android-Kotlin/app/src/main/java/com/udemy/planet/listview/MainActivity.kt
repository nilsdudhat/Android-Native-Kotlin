package com.udemy.planet.listview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udemy.planet.listview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListView()
    }

    private fun setupListView() {
        val list: List<PlanetModel> = ArrayList(getPlanetList())

        val planetAdapter = PlanetAdapter(context = applicationContext, planetList = list)
        binding.listPlanet.adapter = planetAdapter
    }

    private fun getPlanetList(): MutableList<PlanetModel> {
        val planetModelList: MutableList<PlanetModel> = mutableListOf()

        val mercury = PlanetModel("Mercury", 0, R.drawable.mercury)
        val venus = PlanetModel("venus", 0, R.drawable.venus)
        val earth = PlanetModel("Earth", 1, R.drawable.earth)
        val mars = PlanetModel("Mars", 2, R.drawable.mars)
        val jupiter = PlanetModel("Jupiter", 79, R.drawable.jupiter)
        val saturn = PlanetModel("Saturn", 83, R.drawable.saturn)
        val uranus = PlanetModel("Uranus", 27, R.drawable.uranus)
        val neptune = PlanetModel("Neptune", 17, R.drawable.neptune)

        planetModelList.add(mercury)
        planetModelList.add(venus)
        planetModelList.add(earth)
        planetModelList.add(mars)
        planetModelList.add(jupiter)
        planetModelList.add(saturn)
        planetModelList.add(uranus)
        planetModelList.add(neptune)

        return planetModelList
    }
}
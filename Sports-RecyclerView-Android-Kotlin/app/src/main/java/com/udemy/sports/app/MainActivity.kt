package com.udemy.sports.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.udemy.sports.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private var sportAdapter: SportAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpRecyclerView()

        sportAdapter?.setSportsList(getSportsList())
    }

    private fun getSportsList(): ArrayList<SportModel> {
        val list = ArrayList<SportModel>()

        list.add(SportModel(name = "Volleyball", image = R.drawable.volleyball))
        list.add(SportModel(name = "Archery", image = R.drawable.archery))
        list.add(SportModel(name = "Badminton", image = R.drawable.badminton))
        list.add(SportModel(name = "Basketball", image = R.drawable.basketball))
        list.add(SportModel(name = "Chess", image = R.drawable.chess))
        list.add(SportModel(name = "Football", image = R.drawable.football))
        list.add(SportModel(name = "Golf", image = R.drawable.golf))
        list.add(SportModel(name = "Hockey", image = R.drawable.hockey))
        list.add(SportModel(name = "Soccer", image = R.drawable.soccer))
        list.add(SportModel(name = "Tennis", image = R.drawable.tennis))

        return list
    }

    private fun setUpRecyclerView() {
        if (binding.rvSports.layoutManager == null) {
            val layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            binding.rvSports.layoutManager = layoutManager
        }

        if (binding.rvSports.adapter == null) {
            sportAdapter = SportAdapter()
            binding.rvSports.adapter = sportAdapter
        }
    }
}
package com.udemy.tab.viewpager.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.udemy.tab.viewpager.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var binding: ActivityMainBinding

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

        viewPagerAdapter =
            ViewPagerAdapter(fragmentManager = supportFragmentManager, lifecycle = lifecycle)

        viewPagerAdapter.addFragment(FirstFragment())
        viewPagerAdapter.addFragment(SecondFragment())
        viewPagerAdapter.addFragment(ThirdFragment())

        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(
            binding.tabs,
            binding.viewPager,
            true,
            true
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.ic_home)
                    tab.text = "Home"
                }

                1 -> {
                    tab.setIcon(R.drawable.ic_dashboard)
                    tab.text = "Dashboard"
                }

                2 -> {
                    tab.setIcon(R.drawable.ic_settings)
                    tab.text = "Settings"
                }
            }
        }.attach()
    }
}
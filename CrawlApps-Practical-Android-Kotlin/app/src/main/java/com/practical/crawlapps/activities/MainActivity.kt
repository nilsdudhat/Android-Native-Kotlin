package com.practical.crawlapps.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.practical.crawlapps.R
import com.practical.crawlapps.utils.createFactory
import com.practical.crawlapps.databinding.ActivityMainBinding
import com.practical.crawlapps.viewmodels.MainFactory
import com.practical.crawlapps.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

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

        val viewModel = ViewModelProvider(
            this,
            MainViewModel(MainFactory(this, binding)).createFactory()
        )[MainViewModel::class.java]
        binding.viewModel = viewModel

        val boxSize = 5
        binding.size = boxSize.toString()
    }
}
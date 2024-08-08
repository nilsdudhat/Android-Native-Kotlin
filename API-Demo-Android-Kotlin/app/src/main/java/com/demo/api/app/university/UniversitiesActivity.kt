package com.demo.api.app.university

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.demo.api.app.R
import com.demo.api.app.createFactory
import com.demo.api.app.databinding.ActivityUniversitiesBinding

class UniversitiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUniversitiesBinding
    private val viewModel =
        lazy {
            ViewModelProvider(
                this,
                UniversityViewModel(UniversityFactory(this, binding)).createFactory()
            )[UniversityViewModel::class.java]
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUniversitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.country = ""
        binding.viewModel = viewModel.value
    }
}
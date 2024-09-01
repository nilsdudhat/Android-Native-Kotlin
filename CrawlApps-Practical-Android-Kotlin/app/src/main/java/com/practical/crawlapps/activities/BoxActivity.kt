package com.practical.crawlapps.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.practical.crawlapps.R
import com.practical.crawlapps.databinding.ActivityBoxBinding
import com.practical.crawlapps.utils.createFactory
import com.practical.crawlapps.viewmodels.BoxFactory
import com.practical.crawlapps.viewmodels.BoxViewModel

class BoxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoxBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val size = intent.getIntExtra("size", 5)
        binding.size = size

        val viewModel = ViewModelProvider(
            this,
            BoxViewModel(BoxFactory(this, binding)).createFactory()
        )[BoxViewModel::class.java]
        binding.viewModel = viewModel

        viewModel.initialiseRecyclerView(size)

        viewModel.createBoxes(size)
    }
}
package com.udemy.views.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.views.app.databinding.ActivityRadioButtonBinding

class RadioButtonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRadioButtonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRadioButtonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.btn_apple -> {
                    binding.txtValue.text = StringBuilder().append("Apple")
                }

                R.id.btn_banana -> {
                    binding.txtValue.text = StringBuilder().append("Banana")
                }

                R.id.btn_kiwi -> {
                    binding.txtValue.text = StringBuilder().append("Kiwi")
                }

                R.id.btn_watermelon -> {
                    binding.txtValue.text = StringBuilder().append("Watermelon")
                }
            }
        }
    }
}
package com.udemy.views.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.views.app.databinding.ActivityCheckboxBinding

class CheckboxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckboxBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckboxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.checkBox.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                binding.txtValue.text = StringBuilder().append("Checked")
            } else {
                binding.txtValue.text = StringBuilder().append("Unchecked")
            }
        }

        binding.btnCheck.setOnClickListener {
            binding.checkBox.isChecked = !binding.checkBox.isChecked
        }
    }
}
package com.udemy.views.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.views.app.databinding.ActivityMainBinding

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

        binding.btnCheckbox.setOnClickListener {
            startActivity(Intent(this, CheckboxActivity::class.java))
        }

        binding.btnRadio.setOnClickListener {
            startActivity(Intent(this, RadioButtonActivity::class.java))
        }

        binding.btnSpinner.setOnClickListener {
            startActivity(Intent(this, SpinnerActivity::class.java))
        }

        binding.btnTimePicker.setOnClickListener {
            startActivity(Intent(this, TimePickerActivity::class.java))
        }

        binding.btnDatePicker.setOnClickListener {
            startActivity(Intent(this, DatePickerActivity::class.java))
        }
    }
}
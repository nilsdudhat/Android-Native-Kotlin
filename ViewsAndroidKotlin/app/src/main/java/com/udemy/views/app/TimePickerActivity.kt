package com.udemy.views.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.views.app.databinding.ActivityTimePickerBinding
import java.util.Locale

class TimePickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimePickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTimePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.timePicker.setOnTimeChangedListener { timePicker, i, i2 ->
            // timePicker = view : represents the time picker itself
            // i = hour : selected hour in 24 hour format
            // i2 = minute : selected minute

            // formatting the selected time :                                     hh:mm
            val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", i, i2)
            binding.txtValue.text = selectedTime
        }
    }
}
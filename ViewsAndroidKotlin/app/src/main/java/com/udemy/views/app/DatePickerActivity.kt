package com.udemy.views.app

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.views.app.databinding.ActivityDatePickerBinding
import java.util.Locale

class DatePickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatePickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDatePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.datePicker.setOnDateChangedListener { datePicker, i, i2, i3 ->
                // datePicker = view : represents the date picker itself
                // i = year : selected year
                // i2 = month : month of year, starts from 0, 1, 2, 3, ...
                // i3 = day : day of month, starts from 1, 2, 3, ...

                // formatting the selected date :                                      dd/mm/yyyy
                val selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", i3, i2 + 1, i)
                binding.txtValue.text = selectedDate
            }
        }
    }
}
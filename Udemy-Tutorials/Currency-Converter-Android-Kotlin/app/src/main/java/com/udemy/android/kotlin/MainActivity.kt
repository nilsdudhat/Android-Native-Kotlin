package com.udemy.android.kotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.android.kotlin.databinding.ActivityMainBinding

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

        binding.btnConvert.setOnClickListener {
            if (binding.edtInput.text.toString().isEmpty()) {
                binding.edtInput.error = "Enter amount in USD"
            } else {
                val strAmount = binding.edtInput.text.toString()
                val amount = strAmount.toDouble()

                val amountInRupees = amount * 85
                binding.txtResult.text = StringBuilder().append("Rupees: ").append(amountInRupees)
            }
        }
    }
}
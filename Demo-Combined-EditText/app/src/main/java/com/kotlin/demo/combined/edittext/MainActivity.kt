package com.kotlin.demo.combined.edittext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.kotlin.demo.combined.edittext.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edtKilos.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.edtKilos.isCursorVisible && binding.edtKilos.hasFocus()) {
                    if (binding.edtKilos.text.isNotEmpty()) {
                        binding.edtPounds.setText(convertKiloToPounds(binding.edtKilos.text.toString().toDouble()).toString())
                    } else {
                        binding.edtPounds.setText("")
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.edtPounds.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.edtPounds.isCursorVisible && binding.edtPounds.hasFocus()) {
                    if (binding.edtPounds.text.isNotEmpty()) {
                        binding.edtKilos.setText(convertPoundToKilos(binding.edtPounds.text.toString().toDouble()).toString())
                    } else {
                        binding.edtKilos.setText("")
                    }
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })
    }

    private fun convertKiloToPounds(kilos : Double): Double {
        return kilos / 2.20462
    }

    private fun convertPoundToKilos(pounds : Double) : Double {
        return pounds * 2.20462
    }
}
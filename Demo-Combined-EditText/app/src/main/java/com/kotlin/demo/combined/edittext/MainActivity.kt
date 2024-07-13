package com.kotlin.demo.combined.edittext

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.demo.combined.edittext.databinding.ActivityMainBinding
import java.util.Locale

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
                        binding.edtPounds.setText(
                            convertKiloToPounds(binding.edtKilos.text.toString().toDouble())
                        )
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
                        binding.edtKilos.setText(
                            convertPoundToKilos(binding.edtPounds.text.toString().toDouble())
                        )
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

    private fun convertKiloToPounds(kilos: Double): String {
        val conversion = kilos / 2.20462
        return String.format(Locale.getDefault(), "%.2f", conversion)
    }

    private fun convertPoundToKilos(pounds: Double): String {
        val conversion = pounds * 2.20462
        return String.format(Locale.getDefault(), "%.2f", conversion)
    }
}
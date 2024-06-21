package com.udemy.volume.calculator.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.volume.calculator.R
import com.udemy.volume.calculator.databinding.ActivityPrismBinding

class PrismActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrismBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrismBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {

        binding.editArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding.areaContainer.error = "";
            }
        })

        binding.editHeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding.heightContainer.error = "";
            }
        })

        binding.btnCalculate.setOnClickListener(View.OnClickListener {
            if (binding.editArea.getText() == null || binding.editHeight.getText() == null) {
                return@OnClickListener
            }
            if (binding.editArea.text.toString().isEmpty()) {
                binding.areaContainer.error = "Enter Radius"

                return@OnClickListener
            }
            if (binding.editHeight.text.toString().isEmpty()) {
                binding.heightContainer.error = "Enter Height"

                return@OnClickListener
            }
            // V = area * height;

            val area = binding.editArea.text.toString().toDouble()
            val height = binding.editHeight.text.toString().toDouble()
            val volume = area * height

            binding.txtResult.text = StringBuilder()
                .append("Result: ")
                .append(volume)
                .append(" m^3")
        })
    }
}
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
import com.udemy.volume.calculator.databinding.ActivityCylinderBinding

class CylinderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCylinderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCylinderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {

        binding.editRadius.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding.radiusContainer.error = "";
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
            if (binding.editRadius.getText() == null || binding.editHeight.getText() == null) {
                return@OnClickListener
            }
            if (binding.editRadius.text.toString().isEmpty()) {
                binding.radiusContainer.error = "Enter Radius"

                return@OnClickListener
            }
            if (binding.editHeight.text.toString().isEmpty()) {
                binding.heightContainer.error = "Enter Height"

                return@OnClickListener
            }
            // V = 3.14159 * r * r * h;

            val radius = binding.editRadius.text.toString().toDouble()
            val height = binding.editRadius.text.toString().toDouble()
            val volume = 3.14159 * radius * radius * height

            binding.txtResult.text = StringBuilder()
                .append("Result: ")
                .append(volume)
                .append(" m^3")
        })
    }
}
package com.udemy.volume.calculator.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import com.udemy.volume.calculator.R

class PrismActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_prism)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        val areaContainer: TextInputLayout = findViewById(R.id.area_container)
        val edtArea: TextView = findViewById(R.id.edit_area)
        val heightContainer: TextInputLayout = findViewById(R.id.height_container)
        val edtHeight: TextView = findViewById(R.id.edit_height)
        val txtResult: TextView = findViewById(R.id.txt_result)
        val btnCalculate: Button = findViewById(R.id.btn_calculate)

        edtArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                areaContainer.error = "";
            }
        })

        edtHeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                heightContainer.error = "";
            }
        })

        btnCalculate.setOnClickListener(View.OnClickListener {
            if (edtArea.getText() == null || edtHeight.getText() == null) {
                return@OnClickListener
            }
            if (edtArea.text.toString().isEmpty()) {
                areaContainer.error = "Enter Radius"

                return@OnClickListener
            }
            if (edtHeight.text.toString().isEmpty()) {
                heightContainer.error = "Enter Height"

                return@OnClickListener
            }
            // V = area * height;

            val area = edtArea.text.toString().toDouble()
            val height = edtHeight.text.toString().toDouble()
            val volume = area * height

            txtResult.text = StringBuilder()
                .append("Result: ")
                .append(volume)
                .append(" m^3")
        })
    }
}
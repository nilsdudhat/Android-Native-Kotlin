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

class CylinderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cylinder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        val radiusContainer: TextInputLayout = findViewById(R.id.radius_container)
        val edtRadius: TextView = findViewById(R.id.edit_radius)
        val heightContainer: TextInputLayout = findViewById(R.id.height_container)
        val edtHeight: TextView = findViewById(R.id.edit_height)
        val txtResult: TextView = findViewById(R.id.txt_result)
        val btnCalculate: Button = findViewById(R.id.btn_calculate)

        edtRadius.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                radiusContainer.error = "";
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
            if (edtRadius.getText() == null || edtHeight.getText() == null) {
                return@OnClickListener
            }
            if (edtRadius.text.toString().isEmpty()) {
                radiusContainer.error = "Enter Radius"

                return@OnClickListener
            }
            if (edtHeight.text.toString().isEmpty()) {
                heightContainer.error = "Enter Height"

                return@OnClickListener
            }
            // V = 3.14159 * r * r * h;

            val radius = edtRadius.text.toString().toDouble()
            val height = edtRadius.text.toString().toDouble()
            val volume = 3.14159 * radius * radius * height

            txtResult.text = StringBuilder()
                .append("Result: ")
                .append(volume)
                .append(" m^3")
        })
    }
}
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

class CubeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cube)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        val lengthContainer: TextInputLayout = findViewById(R.id.length_container)
        val edtLength: TextView = findViewById(R.id.edit_length)
        val txtResult: TextView = findViewById(R.id.txt_result)
        val btnCalculate: Button = findViewById(R.id.btn_calculate)

        edtLength.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                lengthContainer.error = "";
            }
        })

        btnCalculate.setOnClickListener(View.OnClickListener {
            if ((edtLength.text == null) ||
                edtLength.text.toString().isEmpty()
            ) {
                lengthContainer.error = "Enter Radius"

                return@OnClickListener
            }
            // V = length ^ 3;

            val length = edtLength.text.toString().toDouble()
            val volume = length * length * length;

            txtResult.text = StringBuilder()
                .append("Result: ")
                .append(volume)
                .append(" m^3")
        })
    }
}
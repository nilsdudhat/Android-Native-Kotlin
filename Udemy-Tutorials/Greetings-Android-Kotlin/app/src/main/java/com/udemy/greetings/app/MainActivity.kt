package com.udemy.greetings.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.greetings.app.databinding.ActivityMainBinding

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

        binding.btnSubmit.setOnClickListener {
            if (binding.edtUserName.text.toString().isEmpty()) {
                binding.edtUserName.error = "User name is required"
            } else if (binding.edtLanguage.text.toString().isEmpty()) {
                binding.edtLanguage.error = "Language is required"
            } else if ((binding.edtLanguage.text.toString() != "Kotlin")
                && (binding.edtLanguage.text.toString() != "Java")
            ) {
                binding.edtLanguage.error = "Please enter Java or Kotlin"
            } else {
                val language = binding.edtLanguage.text.toString()

                if (language == "Java") {
                    binding.imgLanguage.setImageResource(R.drawable.java)
                } else {
                    binding.imgLanguage.setImageResource(R.drawable.kotlin)
                }
            }
        }
    }
}
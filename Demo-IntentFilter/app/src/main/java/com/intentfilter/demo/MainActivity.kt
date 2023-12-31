package com.intentfilter.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intentfilter.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND) // intent
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_EMAIL, "nilsdudhat14@gmail.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "This is a dummy message")
            intent.putExtra(Intent.EXTRA_TEXT, "Dummy test message")
            startActivity(intent)
        }

        binding.btnView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            startActivity(intent)
        }
    }
}
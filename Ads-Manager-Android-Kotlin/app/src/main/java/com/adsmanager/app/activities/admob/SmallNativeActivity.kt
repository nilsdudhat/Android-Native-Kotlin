package com.adsmanager.app.activities.admob

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.adsmanager.app.R
import com.adsmanager.app.databinding.ActivitySmallNativeBinding
import com.adsmanager.app.displaySmallNativeAd

class SmallNativeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmallNativeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySmallNativeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        displaySmallNativeAd(this, binding.adSmallNative)
    }
}
package com.eventbus.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.eventbus.app.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val appEventBus by inject<AppEventBus>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(ActivityMainBinding.inflate(layoutInflater).also { binding = it }.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        appEventBus.subscribe(lifecycleScope) { appEvent ->
            when (appEvent) {
                is AppEvent.MyEventWithData -> {
                    // Access event data here
                    appEvent.message
                }
                is AppEvent.MyEventWithoutData -> {
                    // Perform action here
                }
                else -> {}
            }
        }
    }
}
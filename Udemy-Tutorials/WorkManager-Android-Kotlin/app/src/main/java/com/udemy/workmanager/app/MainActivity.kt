package com.udemy.workmanager.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val data = Data.Builder()
            .putInt("max_limit", 1000)
            .build()

        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)

        WorkManager.getInstance(baseContext)
            .getWorkInfoByIdLiveData(workRequest.id)
            .observe(this) {
                if(it != null) {
                    Toast.makeText(baseContext, it.state.name, Toast.LENGTH_SHORT).show()

                    if (it.state.isFinished) {
                        val outputData = it.outputData
                        if (outputData.hasKeyWithValueOfType("status", String::class.java)) {
                            Log.d("--status--", "output: ${outputData.getString("status")}")
                        }
                    }
                }
            }
    }
}
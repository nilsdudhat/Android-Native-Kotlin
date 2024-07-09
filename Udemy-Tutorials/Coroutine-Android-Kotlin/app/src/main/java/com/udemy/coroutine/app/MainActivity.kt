package com.udemy.coroutine.app

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import com.udemy.coroutine.app.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val counterLiveData = MutableLiveData(0)

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

        counterLiveData.observeForever {
            binding.counter = it
        }

        binding.btnUpdateCount.setOnClickListener {
            // update counter value
            counterLiveData.value = counterLiveData.value?.plus(1)
        }

        binding.btnDownloadFile.setOnClickListener {
            // Download File
            CoroutineScope(Dispatchers.IO).launch {
                for (i in 1..1000000) {
                    withContext(Dispatchers.Main) {
                        binding.txtDownloadFile.text =
                            "value: $i, Thread Name: ${Thread.currentThread().name}"
                    }
                    Log.d("--tag--", "value: $i, Thread Name: ${Thread.currentThread().name}")
                }
            }
        }

        binding.btnPress.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("--fun--", "launch: start")

                // parallel execution : helps when values of both functions are independent from each function, saves time
                val fun1 = async {
                    fun1()
                }
                val fun2 = async {
                    fun2()
                }

                Log.d("--fun--", "launch: ${fun1.await() + fun2.await()}")

                /*
                // sequential execution : helps when values of first function is important for second function
                val fun1 = fun1()
                val fun2 = fun2()

                Log.d("--fun--", "launch: ${fun1 + fun2}")
                */
            }
        }
    }

    private suspend fun fun1(): Int {
        delay(5000)
        Log.d("--fun--", "fun1: Done")
        return 5
    }

    private suspend fun fun2(): Int {
        delay(11000)
        Log.d("--fun--", "fun2: Done")
        return 11
    }
}
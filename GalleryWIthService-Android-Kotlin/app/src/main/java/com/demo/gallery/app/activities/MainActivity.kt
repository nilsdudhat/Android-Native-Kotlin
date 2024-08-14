package com.demo.gallery.app.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import com.demo.gallery.app.R
import com.demo.gallery.app.utils.getAllMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    /* val mediaViewModel = ViewModelProvider(
        this@MainActivity,
        MediaViewModel(MediaFactory(applicationContext)).createFactory(),
    )[MediaViewModel::class.java] */

    override fun onCreate(savedInstanceState: Bundle?) {
        displaySplash()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (checkPermission()) {
            CoroutineScope(Dispatchers.IO).launch {
                getAllMedia(this@MainActivity)
            }
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        val readStoragePermission =
            ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        val writeStoragePermission =
            ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)

        return (readStoragePermission == PackageManager.PERMISSION_GRANTED) &&
                (writeStoragePermission == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        requestMultiplePermissions.launch(
            arrayOf(
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
            )
        )
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isApproved = false

        for (entry in permissions.entries) {
            if (!entry.value) {
                break
            }
        }

        if (isApproved) {
            CoroutineScope(Dispatchers.IO).launch {
                getAllMedia(this@MainActivity)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty()) {
                val readStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writeStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED

                if (readStorage && writeStorage) {
                    CoroutineScope(Dispatchers.IO).launch {
                        getAllMedia(this@MainActivity)
                    }
                }
            }
        }
    }

    private fun displaySplash() {
        val isSplash = MutableLiveData(true)

        installSplashScreen().setKeepOnScreenCondition(condition = SplashScreen.KeepOnScreenCondition {
            CoroutineScope(Dispatchers.Default).launch {
                delay(2000)
                isSplash.postValue(false)
            }
            return@KeepOnScreenCondition isSplash.value!!
        })
    }
}
package com.downloadmanager.app

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.downloadmanager.app.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var receiver: DownloadBroadcast
    private lateinit var viewModel: DownloadViewModel
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

        // initialise androidviewmodel
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(DownloadViewModel::class.java)

        binding.btnDownloadVideo.setOnClickListener {
            checkPermissionsAndStartDownload()
        }
        initBroadCastReceiver()
        bindViewModelObservers()
    }

    override fun onDestroy() {
        if (::receiver.isInitialized)
            unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun checkPermissionsAndStartDownload() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    Toast.makeText(
                        this,
                        "Please grant storage permission to download files.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                requestPermissionLauncher.launch(permission)
            } else {
                downloadVideo(
                    "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    "BigBuckBunny${System.currentTimeMillis()}",
                )
            }
        } else {
            downloadVideo(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "BigBuckBunny${System.currentTimeMillis()}",
            )
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                downloadVideo(
                    "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    "BigBuckBunny_${System.currentTimeMillis()}"
                )
            } else {
                Toast.makeText(this, "Permission denied. Cannot download file.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private fun initBroadCastReceiver() {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        receiver = DownloadBroadcast { downloadId ->
            Toast.makeText(this, "Download Completed ID - $downloadId", Toast.LENGTH_SHORT).show()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    private fun bindViewModelObservers() {
        viewModel.downloadId.observe(this) { downloadId ->
            // Handle the download ID here
            if (downloadId != null) {
                Toast.makeText(this, "Download started with ID: $downloadId", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.progress.observe(this) { progress ->
            updateProgress(progress)
        }
    }

    private fun downloadVideo(fileUrl: String, fileName: String) {
        viewModel.startDownload(fileUrl, fileName)
    }

    private fun updateProgress(progress: Double) {
        runOnUiThread {
            // Update progress as needed
            binding.progressHorizontal.progress = progress.toInt()
        }
    }
}
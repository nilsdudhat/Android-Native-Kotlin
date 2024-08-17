package com.demo.gallery.app.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.demo.gallery.app.R
import com.demo.gallery.app.adapters.MediaAdapter
import com.demo.gallery.app.databinding.ActivityMainBinding
import com.demo.gallery.app.room.Media
import com.demo.gallery.app.utils.createFactory
import com.demo.gallery.app.utils.externalStorageUri
import com.demo.gallery.app.viewmodels.MediaFactory
import com.demo.gallery.app.viewmodels.MediaViewModel
import com.demo.gallery.app.worker.JobWorker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class WorkerMainActivity : AppCompatActivity() {

    private lateinit var mediaViewModel: MediaViewModel

    private lateinit var binding: ActivityMainBinding

    private var hadPermissions = false

    private val workManager = WorkManager.getInstance(this)

    private var adapter: MediaAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        displaySplash()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mediaViewModel = ViewModelProvider(
            this,
            MediaViewModel(MediaFactory(applicationContext)).createFactory()
        )[MediaViewModel::class.java]

        if (checkPermission()) {
            hadPermissions = true
            Log.d("--permission--", ": permission already approved")

            val jobWorker = OneTimeWorkRequestBuilder<JobWorker>().build()
            workManager.enqueue(jobWorker)

            observeMediaStorage()
        } else {
            hadPermissions = false
            Log.d("--permission--", ": permission request")

            requestPermission()
        }

        mediaViewModel.getAllMedia().observe(this) {
            Log.d("--media--", ": ${it.size}")
            // want to display latest file on the top... write code accordingly
            val it = it.sortedByDescending { media -> media.dateAdded }

            for (media in it) {
                if (!File(media.path).exists()) {
                    mediaViewModel.delete(media)
                }
            }

            setUpRecyclerView(ArrayList(it))
        }
    }

    private fun setUpRecyclerView(list : ArrayList<Media>) {
        binding.rvMedia.apply {
            if (layoutManager == null) {
                layoutManager = GridLayoutManager(this@WorkerMainActivity, 3)
            }
            if (this@WorkerMainActivity.adapter == null) {
                this@WorkerMainActivity.adapter = MediaAdapter()
                adapter = this@WorkerMainActivity.adapter
            }
            this@WorkerMainActivity.adapter!!.setMediaList(list)
        }
    }

    private fun observeMediaStorage() {
        contentResolver.registerContentObserver(
            externalStorageUri,
            true,
            object : ContentObserver(Handler(HandlerThread("external_observer").apply {
                start()
            }.looper)) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)

                    val jobWorker = OneTimeWorkRequestBuilder<JobWorker>().build()
                    workManager.enqueue(jobWorker)

                    Log.d("--media_observe--", "Change detected at URI: $uri")
                }
            })
    }

    override fun onResume() {
        super.onResume()

        val hasPermission = checkPermission()

        if (hasPermission && !hadPermissions) {
            hadPermissions = true

            val jobWorker = OneTimeWorkRequestBuilder<JobWorker>().build()
            workManager.enqueue(jobWorker)

            observeMediaStorage()
        }
    }

    override fun onDestroy() {
        workManager.cancelAllWork()

        super.onDestroy()
    }

    private fun checkPermission(): Boolean {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(READ_MEDIA_IMAGES)
            permissions.add(READ_MEDIA_VIDEO)
            permissions.add(WRITE_EXTERNAL_STORAGE)
        } else {
            permissions.add(READ_EXTERNAL_STORAGE)
            permissions.add(WRITE_EXTERNAL_STORAGE)
        }

        var isGranted = false

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isGranted = false
                break
            } else {
                isGranted = true
            }
        }

        return isGranted
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestMultiplePermissions.launch(
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO,
                )
            )
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE,
                )
            )
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val notApprovedPermissions = mutableListOf<String>()

        for (entry in permissions.entries) {
            if (!entry.value) {
                notApprovedPermissions.add(entry.key)
            }
        }

        if (notApprovedPermissions.isEmpty()) {
            hadPermissions = true
            Log.d("--permission--", ": permission success")

            val jobWorker = OneTimeWorkRequestBuilder<JobWorker>().build()
            workManager.enqueue(jobWorker)

            observeMediaStorage()
        } else {
            hadPermissions = false

            for (permission in notApprovedPermissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // User has deny from permission dialog, we can ask it again
                    Snackbar.make(
                        binding.root, "Please allow the permission to use this app",
                        Snackbar.LENGTH_INDEFINITE,
                    )
                        .setAction("Grant") { _ -> requestPermission() }
                        .show()
                } else {
                    // User has deny permission and checked never show permission dialog so you can redirect to Application settings page
                    Snackbar.make(
                        binding.root, "Please allow the permission to use this app",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("Settings") { _ ->
                            val intent = Intent()
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts(
                                "package",
                                packageName,
                                null,
                            )
                            intent.setData(uri)
                            startActivity(intent)
                        }
                        .show()
                }
            }

            Log.d("--permission--", ": permission declined")
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
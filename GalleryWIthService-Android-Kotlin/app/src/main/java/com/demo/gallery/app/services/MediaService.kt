package com.demo.gallery.app.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import androidx.work.Configuration
import com.demo.gallery.app.room.MediaDatabase
import com.demo.gallery.app.utils.getAllMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaService : JobService() {

    init {
        Configuration.Builder().setJobSchedulerJobIdRange(0, 1000).build()
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        Log.d("--job--", "onStartJob: ")

        CoroutineScope(Dispatchers.IO).launch {

            val dao = MediaDatabase.getInstance(applicationContext).getMediaDao()

            val list = getAllMedia(applicationContext)

            for (media in list) {
                if (!dao.isMediaExistByPath(media.path)) {
                    dao.insert(media)
                }
            }

            jobFinished(p0, false)
        }

        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        Log.d("--job--", "onStopJob: ")
        return false
    }
}
package com.demo.gallery.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.demo.gallery.app.room.MediaDatabase
import com.demo.gallery.app.utils.getAllMedia

class JobWorker(val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        val dao = MediaDatabase.getInstance(context).getMediaDao()

        val list = getAllMedia(context)

        for (media in list) {
            if (!dao.isMediaExistByPath(media.path)) {
                dao.insert(media)
            }
        }

        return Result.success()
    }
}
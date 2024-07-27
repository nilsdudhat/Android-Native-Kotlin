package com.udemy.workmanager.app

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        var maxLimit = 0

        if (inputData.hasKeyWithValueOfType("max_limit", Int::class.java)) {
            maxLimit = inputData.getInt("max_limit", 0)
        }

        // run loop for maxLimit
        for (i in 1..maxLimit) {
            Log.d("--do_work--", "Count: $i")
        }

        val outputData = Data.Builder()
            .putString("status", "success")
            .build()

        return Result.success(outputData)
    }

}

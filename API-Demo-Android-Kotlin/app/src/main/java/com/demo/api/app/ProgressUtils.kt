package com.demo.api.app

import android.app.Activity
import android.app.Dialog
import android.util.Log

class ProgressUtils {
    companion object {
        @Volatile
        private var progressDialog: Dialog? = null

        fun showLoading(activity: Activity) {
            synchronized(this) {
                if (progressDialog == null) {
                    progressDialog = Dialog(activity)
                    progressDialog?.setContentView(R.layout.dialog_progress)
                    progressDialog?.setCancelable(false)
                    progressDialog?.window?.setDimAmount(0.5f)
                    progressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
                }
                progressDialog?.show()
                Log.d("--dialog--", "show")
            }
        }

        fun hideLoading() {
            progressDialog?.dismiss()
            progressDialog = null
            Log.d("--dialog--", "dismiss")
        }
    }
}
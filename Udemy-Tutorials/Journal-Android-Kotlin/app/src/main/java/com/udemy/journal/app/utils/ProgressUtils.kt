package com.udemy.journal.app.utils

import android.app.Dialog
import android.content.Context
import android.util.Log
import com.udemy.journal.app.R

class ProgressUtils {
    companion object {
        @Volatile
        private var progressDialog: Dialog? = null

        fun showLoading(context: Context) {
            synchronized(this) {
                if (progressDialog == null) {
                    progressDialog = Dialog(context)
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
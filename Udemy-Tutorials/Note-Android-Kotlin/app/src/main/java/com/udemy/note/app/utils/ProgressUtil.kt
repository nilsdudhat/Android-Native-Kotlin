package com.udemy.note.app.utils

import android.app.Dialog
import android.content.Context
import com.udemy.note.app.R

class ProgressUtil {

    companion object {
        @Volatile
        private var progressDialog: Dialog? = null

        fun showLoading(context: Context) {
            synchronized(this) {
                if (progressDialog == null) {
                    progressDialog = Dialog(context)
                    progressDialog?.setContentView(R.layout.dialog_progress)
                    progressDialog?.setCancelable(false)
                    progressDialog?.show()
                    progressDialog?.window?.setDimAmount(0.5f)
                    progressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
                }
            }
        }

        fun hideLoading() {
            progressDialog?.dismiss()
        }
    }
}
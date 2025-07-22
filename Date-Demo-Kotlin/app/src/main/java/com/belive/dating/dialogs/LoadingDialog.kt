package com.belive.dating.dialogs

import android.app.Activity
import android.app.Dialog
import com.belive.dating.R
import com.belive.dating.extensions.logger
import java.lang.ref.WeakReference

object LoadingDialog {

    @Volatile
    private var progressDialog: WeakReference<Dialog>? = null

    fun show(activity: Activity) {
        synchronized(this) {
            try {
                if (progressDialog == null) {
                    progressDialog = WeakReference(Dialog(activity))
                    progressDialog?.get()?.setContentView(R.layout.dialog_loading)
                    progressDialog?.get()?.setCancelable(false)
                    progressDialog?.get()?.window?.setDimAmount(0.85f)
                    progressDialog?.get()?.window?.setBackgroundDrawableResource(android.R.color.transparent)
                }
                progressDialog?.get()?.show()

                logger("--progress--", "show")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun hide() {
        logger("--progress--", "dismiss")

        if ((progressDialog?.get()?.window == null) ||
            (progressDialog?.get()?.window?.decorView == null) ||
            (progressDialog?.get()?.window?.decorView?.parent == null)
        ) {
            progressDialog = null
        } else {
            if ((progressDialog?.get() != null) && progressDialog?.get()?.isShowing == true) {
                progressDialog?.get()?.dismiss()
            }
            progressDialog = null
        }
    }
}
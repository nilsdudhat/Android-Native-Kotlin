package com.belive.dating.dialogs

import android.app.Dialog
import android.view.Window
import com.belive.dating.databinding.DialogInitFailedBinding
import com.belive.dating.extensions.getKoinActivity

object InitFailedDialog {

    fun showAppUpdateDialog(
        onTryAgain: () -> Unit,
        onReOpenApp: () -> Unit,
    ) {
        val dialogInitFailedBinding = DialogInitFailedBinding.inflate(getKoinActivity().layoutInflater)

        val failedDialog = Dialog(getKoinActivity())
        failedDialog.setCancelable(false)
        failedDialog.setCanceledOnTouchOutside(false)
        failedDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        failedDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        failedDialog.window?.setDimAmount(0.75f)
        failedDialog.setContentView(dialogInitFailedBinding.root)
        failedDialog.show()

        dialogInitFailedBinding.btnTryAgain.setOnClickListener {
            onTryAgain.invoke()
            failedDialog.dismiss()
        }
        dialogInitFailedBinding.btnReOpenApp.setOnClickListener {
            onReOpenApp.invoke()
            failedDialog.dismiss()
        }
    }
}
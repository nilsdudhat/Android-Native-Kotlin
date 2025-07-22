package com.belive.dating.dialogs

import android.app.Dialog
import android.view.Window
import com.belive.dating.databinding.DialogEnableGpsBinding
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.gsonString

object EnableGPSDialog {

    fun showEnableGPSDialog(
        onTurnOnLocation: () -> Unit,
        onError: () -> Unit,
    ) {
        try {
            val dialogEnableGPSBinding = DialogEnableGpsBinding.inflate(getKoinActivity().layoutInflater)

            val gpsDialog = Dialog(getKoinActivity())
            gpsDialog.setCancelable(false)
            gpsDialog.setCanceledOnTouchOutside(false)
            gpsDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            gpsDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            gpsDialog.window?.setDimAmount(0.75f)
            gpsDialog.setContentView(dialogEnableGPSBinding.root)
            gpsDialog.show()

            dialogEnableGPSBinding.btnTurnOnLocation.setOnClickListener {
                onTurnOnLocation.invoke()
                gpsDialog.dismiss()
            }
        } catch (e: Exception) {
            catchLog("showEnableGPSDialog: ${gsonString(e)}")

            onError.invoke()
        }
    }
}
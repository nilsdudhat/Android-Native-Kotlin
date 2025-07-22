package com.belive.dating.dialogs

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import com.belive.dating.databinding.DialogAppBinding
import com.belive.dating.extensions.gone
import com.belive.dating.extensions.visible

object AppDialog {

    fun showAppUpdateDialog(
        context: Context,
        isFlexible: Boolean = true,
        onClose: () -> Unit,
        onManage: () -> Unit,
    ) {
        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT,
        )
        dialog.window?.setDimAmount(0.75f)
        val binding: DialogAppBinding = DialogAppBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        binding.apply {
            binding.txtTitle.text = StringBuilder().append("Update")
            binding.btnManage.text = StringBuilder().append("Update Now")
            binding.txtMessage.text =
                StringBuilder().append("Please Update to continue using app. \\n We have Launched new and faster app.")

            if (isFlexible) btnClose.visible() else btnClose.gone()

            btnClose.setOnClickListener {
                dialog.dismiss()
                onClose.invoke()
            }
        }
        binding.btnManage.setOnClickListener {
            if (isFlexible) {
                dialog.dismiss()
            }
            onManage.invoke()
        }
        dialog.show()
    }

    fun showAppRedirectDialog(
        context: Context,
        onManage: () -> Unit,
    ) {
        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT,
        )
        dialog.window?.setDimAmount(0.75f)
        val binding: DialogAppBinding =
            DialogAppBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        binding.apply {
            binding.txtTitle.text = StringBuilder().append("Install")
            binding.btnManage.text = StringBuilder().append("Install Now")
            binding.txtMessage.text =
                StringBuilder().append("We have transferred our server, so install our new app by clicking the button below to enjoy the new features of app.")

            btnClose.gone()
        }
        binding.btnManage.setOnClickListener {
            onManage.invoke()
        }
        dialog.show()
    }
}
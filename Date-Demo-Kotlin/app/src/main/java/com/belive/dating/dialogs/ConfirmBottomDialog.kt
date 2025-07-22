package com.belive.dating.dialogs

import android.content.Context
import android.text.Spanned
import com.belive.dating.databinding.ConfirmBottomDialogBinding
import com.belive.dating.helpers.helper_views.dialog.RoundedBottomSheetDialog

class ConfirmBottomDialog(
    context: Context,
    private val title: String,
    private val btnText: String,
    private var message: Spanned? = null,
    private val callBack: OnConfirmListener,
) :
    RoundedBottomSheetDialog(context) {

    interface OnConfirmListener {
        fun onGenderConfirm()
    }

    private val binding: ConfirmBottomDialogBinding by lazy {
        ConfirmBottomDialogBinding.inflate(layoutInflater)
    }

    init {
        setContentView(binding.root)

        setUI()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnYes.setOnClickListener {
            dismiss()

            callBack.onGenderConfirm()
        }
    }

    private fun setUI() {
        binding.message = message
        binding.title = title
        binding.btnText = btnText
    }

    fun setMessage(message: Spanned) {
        binding.message = message
    }
}
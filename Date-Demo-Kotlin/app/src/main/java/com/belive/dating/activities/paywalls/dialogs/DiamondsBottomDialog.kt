package com.belive.dating.activities.paywalls.dialogs

import android.content.Context
import com.belive.dating.databinding.DiamondsBottomDialogBinding
import com.belive.dating.helpers.helper_views.dialog.RoundedBottomSheetDialog

class DiamondsBottomDialog(context: Context) : RoundedBottomSheetDialog(context) {

    private val binding: DiamondsBottomDialogBinding by lazy {
        DiamondsBottomDialogBinding.inflate(layoutInflater)
    }

    init {
        setContentView(binding.root)
    }
}
package com.belive.dating.activities.paywalls.dialogs

import android.content.Context
import com.belive.dating.activities.paywalls.topups.rewind.RewindPaywallViewModel
import com.belive.dating.databinding.RedeemRewindsBottomDialogBinding
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.helpers.helper_views.dialog.RoundedBottomSheetDialog

class RedeemRewindsBottomDialog(context: Context, val viewModel: RewindPaywallViewModel, val listener: DiamondToRewindConvertClickListener) :
    RoundedBottomSheetDialog(context) {

    interface DiamondToRewindConvertClickListener {
        fun onDiamondToRewindConvertClicked()
    }

    val binding: RedeemRewindsBottomDialogBinding by lazy {
        RedeemRewindsBottomDialogBinding.inflate(layoutInflater)
    }

    init {
        setContentView(binding.root)
    }

    private fun setClickListeners() {
        binding.btnLikes.setOnClickListener {
            dismiss()
            listener.onDiamondToRewindConvertClicked()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            setUI()
            setClickListeners()
        }
    }

    private fun setUI() {
        val isInfinite = getUserPrefs().isUnlimitedRewinds

        val conversion = viewModel.rewindLikeModel.get()

        binding.txtConversion.text = StringBuilder().append(conversion!!.spendedDiamond).append(" - ").append(conversion.purchaseItem)
        binding.txtDiamondCount.text = if (isInfinite) "" else StringBuilder().append(getUserPrefs().remainingDiamonds)

        if (conversion.balance < conversion.spendedDiamond) {
            binding.txtButton.text = StringBuilder().append("Get Diamonds")
        } else {
            binding.txtButton.text = StringBuilder().append("Get Rewinds")
        }
    }
}
package com.belive.dating.activities.paywalls.dialogs

import android.content.Context
import com.belive.dating.activities.paywalls.topups.like.LikePaywallViewModel
import com.belive.dating.databinding.RedeemLikesBottomDialogBinding
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.helpers.helper_views.dialog.RoundedBottomSheetDialog

class RedeemLikesBottomDialog(context: Context, val viewModel: LikePaywallViewModel, val listener: DiamondToLikeConvertClickListener) :
    RoundedBottomSheetDialog(context) {

    interface DiamondToLikeConvertClickListener {
        fun onDiamondToLikeConvertClicked()
    }

    val binding: RedeemLikesBottomDialogBinding by lazy {
        RedeemLikesBottomDialogBinding.inflate(layoutInflater)
    }

    init {
        setContentView(binding.root)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            setUI()
            setClickListeners()
        }
    }

    private fun setClickListeners() {
        binding.btnLikes.setOnClickListener {
            dismiss()
            listener.onDiamondToLikeConvertClicked()
        }
    }

    private fun setUI() {
        val isInfinite = getUserPrefs().isUnlimitedLikes

        val conversion = viewModel.rewindLikeModel.get()

        binding.txtConversion.text = StringBuilder().append(conversion!!.spendedDiamond).append(" = ").append(conversion.purchaseItem)
        binding.txtDiamondCount.text = if (isInfinite) "" else StringBuilder().append(getUserPrefs().remainingDiamonds)

        if (conversion.balance < conversion.spendedDiamond) {
            binding.txtButton.text = StringBuilder().append("Get Diamonds")
        } else {
            binding.txtButton.text = StringBuilder().append("Get Like")
        }
    }
}
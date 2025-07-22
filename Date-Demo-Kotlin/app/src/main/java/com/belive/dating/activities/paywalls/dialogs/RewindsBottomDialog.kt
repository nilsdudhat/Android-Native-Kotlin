package com.belive.dating.activities.paywalls.dialogs

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.belive.dating.activities.paywalls.topups.rewind.RewindPaywallActivity
import com.belive.dating.ads.BannerGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.RewindsBottomDialogBinding
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.swipeUp
import com.belive.dating.helpers.helper_views.dialog.RoundedBottomSheetDialog

class RewindsBottomDialog(context: Context) : RoundedBottomSheetDialog(context) {

    val binding: RewindsBottomDialogBinding by lazy {
        RewindsBottomDialogBinding.inflate(layoutInflater)
    }

    init {
        setContentView(binding.root)

        setUI()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnRewinds.setOnClickListener {
            getKoinActivity().startActivity(Intent(getKoinActivity(), RewindPaywallActivity::class.java))
            getKoinActivity().swipeUp()
            dismiss()
        }
    }

    private fun setUI() {
        ManageAds.showBannerAd(BannerGroup.MyBalance, binding.adBanner)

        val isInfinite = getUserPrefs().isUnlimitedRewinds
        binding.imgInfinity.isVisible = isInfinite
        binding.btnRewinds.isVisible = !isInfinite

        binding.txtRewindsCount.text =
            if (isInfinite) "" else StringBuilder().append(getUserPrefs().remainingRewinds)

        if (!isInfinite) {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    "#57DF5B".toColorInt(),
                    "#279A2B".toColorInt(),
                ),
            )
            gradientDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
            binding.btnRewinds.style.backgroundGradientDrawable = gradientDrawable
        }
    }
}
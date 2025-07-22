package com.belive.dating.activities.paywalls.dialogs

import android.content.Context
import android.content.Intent
import androidx.core.view.isVisible
import com.belive.dating.activities.paywalls.topups.like.LikePaywallActivity
import com.belive.dating.ads.BannerGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.LikesBottomDialogBinding
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.swipeUp
import com.belive.dating.helpers.helper_views.dialog.RoundedBottomSheetDialog

class LikesBottomDialog(context: Context) : RoundedBottomSheetDialog(context) {

    val binding: LikesBottomDialogBinding by lazy {
        LikesBottomDialogBinding.inflate(layoutInflater)
    }

    init {
        setContentView(binding.root)

        setUI()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnLikes.setOnClickListener {
            getKoinActivity().startActivity(Intent(getKoinActivity(), LikePaywallActivity::class.java))
            getKoinActivity().swipeUp()
            dismiss()
        }
    }

    private fun setUI() {
        val isInfinite = getUserPrefs().isUnlimitedLikes
        binding.imgInfinity.isVisible = isInfinite
        binding.btnLikes.isVisible = !isInfinite

        binding.txtLikesCount.text = if (isInfinite) "" else StringBuilder().append(getUserPrefs().remainingLikes)

        ManageAds.showBannerAd(BannerGroup.MyBalance, binding.adBanner)
    }
}
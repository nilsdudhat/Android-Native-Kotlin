package com.belive.dating.activities.paywalls.dialogs

import android.content.Context
import android.content.Intent
import com.belive.dating.activities.paywalls.topups.super_like.SuperLikePaywallActivity
import com.belive.dating.ads.BannerGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.SupeLikesBottomDialogBinding
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.swipeUp
import com.belive.dating.helpers.helper_views.dialog.RoundedBottomSheetDialog

class SuperLikesBottomDialog(context: Context) : RoundedBottomSheetDialog(context) {

    val binding: SupeLikesBottomDialogBinding by lazy {
        SupeLikesBottomDialogBinding.inflate(layoutInflater)
    }

    init {
        setContentView(binding.root)

        setUI()

        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnSuperLikes.setOnClickListener {
            getKoinActivity().startActivity(Intent(getKoinActivity(), SuperLikePaywallActivity::class.java))
            getKoinActivity().swipeUp()
            dismiss()
        }
    }

    private fun setUI() {
        binding.txtSuperLikesCount.text = StringBuilder().append(getUserPrefs().remainingSuperLikes)

        ManageAds.showBannerAd(BannerGroup.MyBalance, binding.adBanner)
    }
}
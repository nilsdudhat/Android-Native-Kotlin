package com.belive.dating.activities.paywalls.dialogs

import android.content.Context
import android.content.Intent
import com.belive.dating.activities.paywalls.topups.boost.BoostPaywallActivity
import com.belive.dating.ads.BannerGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.BoostsBottomDialogBinding
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.swipeUp
import com.belive.dating.helpers.helper_views.dialog.RoundedBottomSheetDialog

class BoostsBottomDialog(context: Context) : RoundedBottomSheetDialog(context) {

    private val binding: BoostsBottomDialogBinding by lazy {
        BoostsBottomDialogBinding.inflate(layoutInflater)
    }

    init {
        setContentView(binding.root)

        setUI()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnBoosts.setOnClickListener {
            getKoinActivity().startActivity(Intent(getKoinActivity(), BoostPaywallActivity::class.java))
            getKoinActivity().swipeUp()
            dismiss()
        }
    }

    private fun setUI() {
        binding.txtBoostCount.text = StringBuilder().append(getUserPrefs().remainingBoosts)

        ManageAds.showBannerAd(BannerGroup.MyBalance, binding.adBanner)
    }
}
package com.belive.dating.activities.diamond

import android.content.Context
import com.belive.dating.R
import com.belive.dating.databinding.DialogDailyCheckInBinding
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.throttleFirstClick
import com.belive.dating.extensions.visible
import com.belive.dating.helpers.helper_views.dialog.RoundedBottomSheetDialog
import com.bumptech.glide.Glide

class DailyCheckInBottomDialog(
    context: Context,
    private val dailyDiamondMap: Map<String, Int>,
    private val onClaimListener: OnClaimListener,
) :
    RoundedBottomSheetDialog(context) {

    interface OnClaimListener {
        fun onDailyDiamondClaim()
    }

    private val binding: DialogDailyCheckInBinding by lazy {
        DialogDailyCheckInBinding.inflate(layoutInflater)
    }

    init {
        setContentView(binding.root)

        setUI()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnClaimNow.throttleFirstClick {
            logger("--daily_check--", "setOnClickListener: IS_DAILY_CHECK_IN = ${getUserPrefs().isDailyCheckInAvailable}")
            dismiss()
            onClaimListener.onDailyDiamondClaim()
        }
    }

    private fun setUI() {

        binding.day1.text = StringBuilder().append(" +").append(dailyDiamondMap["1"])
        binding.day2.text = StringBuilder().append(" +").append(dailyDiamondMap["2"])
        binding.day3.text = StringBuilder().append(" +").append(dailyDiamondMap["3"])
        binding.day4.text = StringBuilder().append(" +").append(dailyDiamondMap["4"])
        binding.day5.text = StringBuilder().append(" +").append(dailyDiamondMap["5"])
        binding.day6.text = StringBuilder().append(" +").append(dailyDiamondMap["6"])
        binding.day7.text = StringBuilder().append(" +").append(dailyDiamondMap["7"])

        when (getUserPrefs().checkInDay) {
            1 -> {
                binding.img1.visible()
                binding.lay1.setBackgroundResource(R.drawable.bg_claimed_diamond)
            }

            2 -> {
                binding.img1.visible()
                binding.img2.visible()
                Glide.with(binding.imgDiamond1.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond1)
                binding.lay1.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay2.setBackgroundResource(R.drawable.bg_diamond_current_day)
            }

            3 -> {
                binding.img1.visible()
                binding.img2.visible()
                binding.img3.visible()
                Glide.with(binding.imgDiamond1.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond1)
                Glide.with(binding.imgDiamond2.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond2)
                binding.lay1.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay2.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay3.setBackgroundResource(R.drawable.bg_diamond_current_day)
            }

            4 -> {
                binding.img1.visible()
                binding.img2.visible()
                binding.img3.visible()
                binding.img4.visible()
                Glide.with(binding.imgDiamond1.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond1)
                Glide.with(binding.imgDiamond2.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond2)
                Glide.with(binding.imgDiamond3.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond3)
                binding.lay1.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay2.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay3.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay4.setBackgroundResource(R.drawable.bg_diamond_current_day)
            }

            5 -> {
                binding.img1.visible()
                binding.img2.visible()
                binding.img3.visible()
                binding.img4.visible()
                binding.img5.visible()
                Glide.with(binding.imgDiamond1.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond1)
                Glide.with(binding.imgDiamond2.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond2)
                Glide.with(binding.imgDiamond3.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond3)
                Glide.with(binding.imgDiamond4.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond4)
                binding.lay1.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay2.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay3.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay4.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay5.setBackgroundResource(R.drawable.bg_diamond_current_day)
            }

            6 -> {
                binding.img1.visible()
                binding.img2.visible()
                binding.img3.visible()
                binding.img4.visible()
                binding.img5.visible()
                binding.img6.visible()
                Glide.with(binding.imgDiamond1.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond1)
                Glide.with(binding.imgDiamond2.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond2)
                Glide.with(binding.imgDiamond3.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond3)
                Glide.with(binding.imgDiamond4.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond4)
                Glide.with(binding.imgDiamond5.context).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond5)
                binding.lay1.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay2.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay3.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay4.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay5.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay6.setBackgroundResource(R.drawable.bg_diamond_current_day)
            }

            7 -> {
                binding.img1.visible()
                binding.img2.visible()
                binding.img3.visible()
                binding.img4.visible()
                binding.img5.visible()
                binding.img6.visible()
                binding.img7.visible()
                Glide.with(binding.imgDiamond1).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond1)
                Glide.with(binding.imgDiamond2).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond2)
                Glide.with(binding.imgDiamond3).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond3)
                Glide.with(binding.imgDiamond4).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond4)
                Glide.with(binding.imgDiamond5).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond5)
                Glide.with(binding.imgDiamond6).load(R.drawable.ic_tick_diamond).into(binding.imgDiamond6)
                binding.lay1.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay2.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay3.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay4.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay5.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay6.setBackgroundResource(R.drawable.bg_claimed_diamond)
                binding.lay7.setBackgroundResource(R.drawable.bg_diamond_current_day)
            }
        }
    }
}
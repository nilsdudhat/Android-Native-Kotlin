package com.belive.dating.activities.dashboard.fragments.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.belive.dating.databinding.ItemBannerPaywallBinding
import com.belive.dating.extensions.logger

class PaywallBannerPager(private val context: Context, private val items: List<Int>) : PagerAdapter() {

    override fun getCount(): Int = items.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        logger("--paywall_banner--", position)

        val binding = ItemBannerPaywallBinding.inflate(LayoutInflater.from(context), container, false)
        binding.imgBanner.setImageResource(items[position])
        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
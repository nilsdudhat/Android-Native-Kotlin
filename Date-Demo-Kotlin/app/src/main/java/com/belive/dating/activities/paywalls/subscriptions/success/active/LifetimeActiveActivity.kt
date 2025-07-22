package com.belive.dating.activities.paywalls.subscriptions.success.active

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.R
import com.belive.dating.activities.paywalls.subscriptions.subscription.PlanFeatureAdapter
import com.belive.dating.databinding.ActivityLifetimeActiveBinding
import com.belive.dating.extensions.getGlide
import com.belive.dating.extensions.swipeDown
import com.belive.dating.helpers.helper_functions.linear_layout_manager.WrapLinearLayoutManager
import com.belive.dating.payment.PlanFeatureModel
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.DrawableImageViewTarget

class LifetimeActiveActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityLifetimeActiveBinding.inflate(layoutInflater)
    }

    val featureAdapter by lazy {
        PlanFeatureAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutHeader) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (v.layoutParams as ConstraintLayout.LayoutParams).topMargin = systemBars.top
            insets
        }

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                swipeDown()
            }
        })

        binding.imgClose.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initViews() {
        getGlide().load(R.drawable.bg_paywall).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(object : DrawableImageViewTarget(binding.imgStars) {
                override fun setResource(resource: Drawable?) {
                    if (resource is GifDrawable) {
                        resource.setLoopCount(GifDrawable.LOOP_FOREVER) // Infinite Loop
                    }
                    super.setResource(resource)
                }
            })

        if (binding.rvFeatures.layoutManager == null) {
            binding.rvFeatures.layoutManager = WrapLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvFeatures.adapter == null) {
            binding.rvFeatures.adapter = featureAdapter
        }

        featureAdapter.list = getLifetimeFeatureList()
    }

    fun getLifetimeFeatureList(): ArrayList<PlanFeatureModel> {
        val list = arrayListOf<PlanFeatureModel>()

        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_message_unselected,
                title = "Unlock Messaging",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_like_infi,
                title = "Unlimited Likes",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_rewinds_infi,
                title = "Unlimited Rewinds",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_diamond,
                highlight = "Infinite",
                highlightColor = R.color.diamond_color,
                title = "Diamonds",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "diamonds",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_super_like,
                highlight = "8",
                highlightColor = R.color.super_like_color,
                title = "Super Likes per Week",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "super_likes",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_boost,
                highlight = "4",
                highlightColor = R.color.yellow,
                title = "Boost per Month",
                titleColor = R.color.white,
                subTitle = "(Not included in Weekly Plan)",
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "boosts",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.lifetime_circle_badge,
                title = "Lifetime Badge",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_no_ads,
                title = "No Ads",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_eye,
                title = "See who likes you",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_location_filter,
                title = "Location Filter",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_ai_selected,
                title = "AI matchmaker",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        return list
    }
}
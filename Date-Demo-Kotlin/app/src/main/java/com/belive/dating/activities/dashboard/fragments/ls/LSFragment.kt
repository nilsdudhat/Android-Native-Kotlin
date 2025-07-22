package com.belive.dating.activities.dashboard.fragments.ls

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.belive.dating.R
import com.belive.dating.activities.EventBusFragment
import com.belive.dating.activities.dashboard.fragments.ls.fragments.like.LikeFragment
import com.belive.dating.activities.dashboard.fragments.ls.fragments.super_like.SuperLikeFragment
import com.belive.dating.activities.paywalls.subscriptions.subscription.SubscriptionActivity
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.FragmentLsBinding
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.setBackgroundAnimation
import com.belive.dating.extensions.swipeUp
import com.google.android.material.tabs.TabLayout

class LSFragment : EventBusFragment() {

    val binding: FragmentLsBinding by lazy {
        FragmentLsBinding.inflate(layoutInflater)
    }

    private var currentFragmentTag: String? = null

    override fun onDestroy() {
        super.onDestroy()

        logger("--fragment--", "onDestroy: LSFragment")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logger("--fragment--", "onViewCreated: LSFragment")

        initViews(savedInstanceState)

        clickListeners()

        listenEvents()
    }

    private fun clickListeners() {
        binding.layoutPremium.setOnClickListener {
            startActivity(Intent(getKoinActivity(), SubscriptionActivity::class.java).apply {
                putExtra("is_gold_available", false)
                putExtra("restriction_message", "You cannot see who like you with Gold Subscriptions")
            })
            getKoinActivity().swipeUp()
        }
    }

    private fun initViews(savedInstanceState: Bundle?) {
        binding.isAIMatchAvailable = getUserPrefs().isAiMatchMaker

        lsMessage()

        premiumBannerUI()

        setUpNavigation(savedInstanceState)
    }

    var currentIndex = 0

    val sentences = listOf(
        "Unlock Messaging",
        "Get Unlimited Likes",
        "Enjoy Unlimited Rewinds",
        "Receive More Gems Every Month",
        "Get More Super Likes Per Week",
        "Enjoy More Boost Per Month",
        "Earn an Exclusive Gold Badge",
        "Browse Without Ads",
        "See Who Likes You",
        "Use Location Filters",
        "Access the AI Matchmaker",
    )

    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {

            binding.txtFeature.post {

                val slideOut = ObjectAnimator.ofFloat(binding.txtFeature, "translationX", 0f, -binding.txtFeature.width.toFloat()).apply {
                    duration = 200
                }
                val slideIn = ObjectAnimator.ofFloat(binding.txtFeature, "translationX", binding.txtFeature.width.toFloat(), 0f).apply {
                    duration = 200
                }

                slideOut.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentIndex = (currentIndex + 1) % sentences.size
                        binding.feature = sentences[currentIndex] // Update text
                        binding.txtFeature.translationX = binding.txtFeature.width.toFloat() // Reset position
                        slideIn.start()
                    }
                })

                slideOut.start()
            }

            handler.postDelayed(this, 2000) // Next text update after 2s
        }
    }

    private fun premiumBannerUI() {
        val displayPremiumBanner: Boolean = !getUserPrefs().isAiMatchMaker

        binding.layoutPremium.clearAnimation()
        binding.txtFeature.clearAnimation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler.hasCallbacks(runnable)) {
                handler.removeCallbacks(runnable)
            }
        } else {
            handler.removeCallbacksAndMessages(null)
        }

        if (displayPremiumBanner) {
            val premiumTitle = "Enjoy Exclusive Features with Premium"
            val spannablePremiumTitle = SpannableString(premiumTitle)

            // Styling for "Enjoy Exclusive Features with Premium"
            val premiumTitleItalicStart = premiumTitle.indexOf(premiumTitle)
            val premiumTitleItalicEnd = premiumTitleItalicStart + premiumTitle.length
            spannablePremiumTitle.setSpan(
                StyleSpan(Typeface.ITALIC),
                premiumTitleItalicStart,
                premiumTitleItalicEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.premiumTitle = spannablePremiumTitle

            binding.feature = sentences[currentIndex]

            handler.postDelayed(runnable, 2000)

            binding.layoutPremium.setBackgroundAnimation(
                intArrayOf(
                    "#303F9F".toColorInt(),
                    "#1976D2".toColorInt(),
                    "#512DA8".toColorInt(),
                    "#C2185B".toColorInt(),
                    "#D32F2F".toColorInt(),
                )
            )
        }
    }

    private fun lsMessage() {
        val fullText = "Find out who likes you on\nbelive dating app."
        val spannable = SpannableString(fullText)

        // Styling for "who likes you"
        val boldPart = "who likes you"
        val boldStart = fullText.indexOf(boldPart)
        val boldEnd = boldStart + boldPart.length
        spannable.setSpan(StyleSpan(Typeface.BOLD), boldStart, boldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(StyleSpan(Typeface.ITALIC), boldStart, boldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            ForegroundColorSpan(requireContext().getColorFromAttr(android.R.attr.colorPrimary)),
            boldStart,
            boldEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        binding.message = spannable
    }

    private fun setUpNavigation(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            showFragment(LikeFragment::class.java.simpleName)
        }

        val likeTitle = if (getUserPrefs().likeCount == -1 || getUserPrefs().likeCount == 0) {
            "Likes"
        } else {
            "${getUserPrefs().likeCount} Likes"
        }
        val superLikeTitle = if (getUserPrefs().superLikeCount == -1 || getUserPrefs().superLikeCount == 0) {
            "Super Likes"
        } else {
            "${getUserPrefs().superLikeCount} Super Likes"
        }
        val fragmentTitles = listOf(likeTitle, superLikeTitle)

        for (title in fragmentTitles) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showFragment(LikeFragment::class.java.simpleName)
                    1 -> showFragment(SuperLikeFragment::class.java.simpleName)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showFragment(tag: String) {
        val fragmentManager = childFragmentManager
        val transaction = fragmentManager.beginTransaction()

        currentFragmentTag?.let { currentTag ->
            fragmentManager.findFragmentByTag(currentTag)?.let { currentFragment ->
                transaction.hide(currentFragment)
            }
        }

        val fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            transaction.show(fragment)
        } else {
            when (tag) {
                LikeFragment::class.java.simpleName -> {
                    transaction.add(R.id.fragment_container, LikeFragment(), tag)
                }

                SuperLikeFragment::class.java.simpleName -> {
                    transaction.add(R.id.fragment_container, SuperLikeFragment(), tag)
                }
            }
        }

        transaction.commitAllowingStateLoss()
        currentFragmentTag = tag
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.INIT_SUCCESS -> {
                val likeTitle = if (getUserPrefs().likeCount == -1 || getUserPrefs().likeCount == 0) {
                    "Likes"
                } else {
                    "${getUserPrefs().likeCount} Likes"
                }
                binding.tabLayout.getTabAt(0)?.text = likeTitle

                val superLikeTitle = if (getUserPrefs().superLikeCount == -1 || getUserPrefs().superLikeCount == 0) {
                    "Super Likes"
                } else {
                    "${getUserPrefs().superLikeCount} Super Likes"
                }
                binding.tabLayout.getTabAt(1)?.text = superLikeTitle

                binding.isAIMatchAvailable = getUserPrefs().isAiMatchMaker

                premiumBannerUI()
            }

            EventConstants.DISPLAY_LS_PROGRESS -> {
                binding.isLoading = (value as? Boolean) ?: false
            }

            EventConstants.UPDATE_PURCHASE -> {
                binding.isAIMatchAvailable = getUserPrefs().isAiMatchMaker

                premiumBannerUI()
            }
        }
    }
}
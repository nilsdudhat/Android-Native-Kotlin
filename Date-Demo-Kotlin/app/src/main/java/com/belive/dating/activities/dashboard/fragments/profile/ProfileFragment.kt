package com.belive.dating.activities.dashboard.fragments.profile

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import com.belive.dating.R
import com.belive.dating.activities.EventBusFragment
import com.belive.dating.activities.diamond.DiamondActivity
import com.belive.dating.activities.paywalls.dialogs.BoostsBottomDialog
import com.belive.dating.activities.paywalls.dialogs.DiamondsBottomDialog
import com.belive.dating.activities.paywalls.dialogs.LikesBottomDialog
import com.belive.dating.activities.paywalls.dialogs.RewindsBottomDialog
import com.belive.dating.activities.paywalls.dialogs.SuperLikesBottomDialog
import com.belive.dating.activities.paywalls.subscriptions.subscription.SubscriptionActivity
import com.belive.dating.activities.paywalls.subscriptions.success.active.LifetimeActiveActivity
import com.belive.dating.activities.profile.ProfileActivity
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.FragmentProfileBinding
import com.belive.dating.di.diamondViewModel
import com.belive.dating.di.profileViewModel
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.convertDateString
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getScreenWidth
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.navGraphViewModel
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.swipeUp
import com.belive.dating.payment.ProductType
import org.koin.core.context.loadKoinModules

class ProfileFragment : EventBusFragment() {

    val binding: FragmentProfileBinding by lazy {
        FragmentProfileBinding.inflate(layoutInflater)
    }

    val viewModel: ProfileViewModel by navGraphViewModel(R.id.navigation_profile)

    val paywallBannerPager: PaywallBannerPager by lazy {
        PaywallBannerPager(requireContext(), viewModel.paywallBanners)
    }

    private val boostsBottomSheet by lazy { BoostsBottomDialog(requireContext()) }
    private val likesBottomSheet by lazy { LikesBottomDialog(requireContext()) }
    private val superLikesBottomSheet by lazy { SuperLikesBottomDialog(requireContext()) }
    private val rewindsBottomSheet by lazy { RewindsBottomDialog(requireContext()) }
    private val diamondBottomSheet by lazy { DiamondsBottomDialog(requireContext()) }

    private var animator: ValueAnimator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isUserInteracting = false
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (!isUserInteracting) {
                val nextItem = (binding.paywallPager.currentItem + 1) % paywallBannerPager.count
                binding.paywallPager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 2000) // Change slide interval (3 seconds)
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoScrollRunnable) // Avoid memory leaks
        super.onDestroy()

        logger("--fragment--", "onDestroy: ProfileFragment")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.updateState()
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        viewModel.getState()
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.getState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logger("--fragment--", "onViewCreated: ProfileFragment")

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        listenEvents()

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        binding.profileTitle.setOnClickListener {
            loadKoinModules(profileViewModel)

            startActivity(Intent(getKoinActivity(), ProfileActivity::class.java))
            getKoinActivity().swipeRight()
        }

        binding.profilePercentageContainer.setOnClickListener {
            if (viewModel.profilePercentage.get() != 100) {
                loadKoinModules(profileViewModel)

                startActivity(Intent(getKoinActivity(), ProfileActivity::class.java))
                getKoinActivity().swipeRight()
            }
        }

        binding.imgDetails.setOnClickListener {
            if (viewModel.profilePercentage.get() != 100) {
                loadKoinModules(profileViewModel)

                startActivity(Intent(getKoinActivity(), ProfileActivity::class.java))
                getKoinActivity().swipeRight()
            }
        }

        binding.layoutGetPremium.setOnClickListener {
            startActivity(Intent(requireActivity(), SubscriptionActivity::class.java))
            requireActivity().swipeUp()
        }

        binding.frameLifetime.setOnClickListener {
            startActivity(Intent(requireActivity(), LifetimeActiveActivity::class.java))
            requireActivity().swipeUp()
        }

        binding.llBoost.setOnClickListener {
            boostsBottomSheet.show()
        }

        binding.llLike.setOnClickListener {
            likesBottomSheet.show()
        }

        binding.llSuperLike.setOnClickListener {
            superLikesBottomSheet.show()
        }

        binding.llRewind.setOnClickListener {
            rewindsBottomSheet.show()
        }

        binding.diamondsContainer.setOnClickListener {
            if (getUserPrefs().activePackage == "Lifetime") {
                diamondBottomSheet.show()
            } else {
                loadKoinModules(diamondViewModel)

                startActivity(Intent(requireActivity(), DiamondActivity::class.java))
                getKoinActivity().swipeRight()
            }
        }
    }

    private fun initViews() {
        val message = "Believe in dating, Belive for dating"
        val spannableMessage = SpannableString(message)

        // Styling for "Belive"
        val boldPart = "Belive"
        val boldStart = message.indexOf(boldPart)
        val boldEnd = boldStart + boldPart.length
        spannableMessage.setSpan(StyleSpan(Typeface.BOLD), boldStart, boldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableMessage.setSpan(
            ForegroundColorSpan(requireContext().getColorFromAttr(android.R.attr.colorPrimary)),
            boldStart,
            boldEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.message = spannableMessage

        updateProfileDetails()

        updateProfileBalance()

        initializePaywall()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializePaywall() {
        binding.paywallPager.adapter = paywallBannerPager
        binding.paywallPager.pageMargin = getKoinActivity().getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)

        // Enable auto-scroll
        handler.postDelayed(autoScrollRunnable, 2000)

        // Pause auto-scroll when the user is touching
        binding.paywallPager.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    isUserInteracting = true
                    handler.removeCallbacks(autoScrollRunnable)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isUserInteracting = false
                    handler.postDelayed(autoScrollRunnable, 2000) // Resume after delay
                }
            }
            false
        }

        binding.paywallPager.layoutParams.height =
            ((getScreenWidth() - (getKoinActivity().getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp) * 2)) * 182) / 335

        // Get image width dynamically after layout is measured
        binding.imgSlice.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.imgSlice.viewTreeObserver.removeOnGlobalLayoutListener(this)

                if (binding.imgSlice.width == 0) return // Ensure image width is obtained

                val totalScrollWidth = binding.imgSlice.width

                sliceAnimation(false, totalScrollWidth)
            }
        })
    }

    private fun sliceAnimation(isRestart: Boolean, totalScrollWidth: Int) {
        logger("--slice--", totalScrollWidth)

        val startTime = if (isRestart) ((totalScrollWidth * 455) / 2270) else 0

        animator = ValueAnimator.ofInt(startTime, totalScrollWidth).apply {
            duration = 1000 * 10 // Adjust speed (10000 seconds per cycle)
            interpolator = LinearInterpolator() // Smooth scrolling
            repeatCount = ValueAnimator.INFINITE // Infinite looping
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                logger("--slice--", animatedValue)

                if (animatedValue >= ((totalScrollWidth * 1200) / 2270)) {
                    animator?.cancel()
                    sliceAnimation(true, totalScrollWidth)
                } else {
                    binding.scrollSlice.scrollTo(animatedValue, 0)
                }
                if (!binding.scrollSlice.isVisible) {
                    animator?.cancel()
                }
            }
            start()
        }
    }

    override fun onResume() {
        super.onResume()

        animator?.resume()
    }

    override fun onPause() {
        super.onPause()

        animator?.pause()
    }

    private fun updateProfileDetails() {
        binding.root.post {
            try {
                viewModel.name.set(getUserPrefs().fullName)
                viewModel.age.set(getUserPrefs().age)
                viewModel.gender.set(getUserPrefs().gender)
                viewModel.profileImage.set(getUserPrefs().userImages?.get(0)?.image)
                viewModel.profilePercentage.set(getUserPrefs().completeProfilePercentage)
                when (getUserPrefs().activePackage) {
                    "Gold" -> {
                        viewModel.activePackage.set(ProductType.GOLD)
                    }

                    "Platinum" -> {
                        viewModel.activePackage.set(ProductType.PLATINUM)
                    }

                    "Lifetime" -> {
                        viewModel.activePackage.set(ProductType.LIFETIME)
                    }

                    else -> {
                        viewModel.activePackage.set(null)
                    }
                }
                viewModel.renewDate.set(getUserPrefs().purchaseEndDate?.let { convertDateString(it) })
                binding.executePendingBindings()
            } catch (e: Exception) {
                catchLog("updateProfileDetails: ${gsonString(e)}")
            }
        }
    }

    private fun updateProfileBalance() {
        binding.root.post {
            try {
                viewModel.diamonds.set(getUserPrefs().remainingDiamonds.toString())
                viewModel.rewinds.set(getUserPrefs().remainingRewinds.toString())
                viewModel.isUnlimitedRewinds.set(getUserPrefs().isUnlimitedRewinds)
                viewModel.likes.set(getUserPrefs().remainingLikes.toString())
                viewModel.isUnlimitedLikes.set(getUserPrefs().isUnlimitedLikes)
                viewModel.superLikes.set(getUserPrefs().remainingSuperLikes.toString())
                viewModel.boosts.set(getUserPrefs().remainingBoosts.toString())
                viewModel.isLifetimeMember.set(getUserPrefs().activePackage == "Lifetime")
            } catch (e: Exception) {
                catchLog("updateProfileBalance: ${gsonString(e)}")
            }
        }
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.INIT_SUCCESS -> {
                updateProfileDetails()
            }

            EventConstants.UPDATE_PROFILE_PERCENTAGE -> {
                viewModel.profilePercentage.set(getUserPrefs().completeProfilePercentage)
            }

            EventConstants.UPDATE_PURCHASE -> {
                logger("--visibility--", "observeEvents")
                when (getUserPrefs().activePackage) {
                    "Gold" -> {
                        viewModel.activePackage.set(ProductType.GOLD)
                    }

                    "Platinum" -> {
                        viewModel.activePackage.set(ProductType.PLATINUM)
                    }

                    "Lifetime" -> {
                        viewModel.activePackage.set(ProductType.LIFETIME)
                    }

                    else -> {
                        viewModel.activePackage.set(null)
                    }
                }
                viewModel.renewDate.set(getUserPrefs().purchaseEndDate?.let { convertDateString(it) })
                updateProfileBalance()
                binding.executePendingBindings()
            }

            EventConstants.UPDATE_PROFILE_BALANCE -> {
                updateProfileBalance()
            }

            EventConstants.UPDATE_DIAMOND_COUNT -> {
                viewModel.diamonds.set(getUserPrefs().remainingDiamonds.toString())
            }

            EventConstants.UPDATE_SUPER_LIKE_COUNT -> {
                viewModel.superLikes.set(getUserPrefs().remainingSuperLikes.toString())
            }

            EventConstants.UPDATE_BOOST_COUNT -> {
                viewModel.boosts.set(getUserPrefs().remainingBoosts.toString())
            }

            EventConstants.UPDATE_REWIND_COUNT -> {
                viewModel.rewinds.set(getUserPrefs().remainingRewinds.toString())
                viewModel.isUnlimitedRewinds.set(getUserPrefs().isUnlimitedRewinds)
            }

            EventConstants.UPDATE_LIKE_COUNT -> {
                viewModel.likes.set(getUserPrefs().remainingLikes.toString())
                viewModel.isUnlimitedLikes.set(getUserPrefs().isUnlimitedLikes)
                viewModel.superLikes.set(getUserPrefs().remainingSuperLikes.toString())
            }

            EventConstants.UPDATE_IMAGES -> {
                viewModel.profileImage.set(getUserPrefs().userImages?.get(0)?.image)
            }
        }
    }
}
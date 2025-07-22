package com.belive.dating.activities.paywalls.topups.super_like

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.paywalls.subscriptions.subscription.SubscriptionActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.api.user.models.payment.PaymentResponse
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivitySuperLikePaywallBinding
import com.belive.dating.di.paywallViewModels
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getGlide
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.setBackgroundAnimation
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeDown
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeUp
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import com.belive.dating.payment.PaymentUtils
import com.belive.dating.payment.ProductType
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.revenuecat.purchases.Offering
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.util.Calendar
import java.util.TimeZone

class SuperLikePaywallActivity : NetworkReceiverActivity(), SuperLikePaywallAdapter.OnOfferClickListener {

    val binding by lazy {
        ActivitySuperLikePaywallBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: SuperLikePaywallViewModel

    val adapter by lazy {
        SuperLikePaywallAdapter(viewModel, this)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(SuperLikePaywallActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(SuperLikePaywallActivity::class.java.simpleName)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.updateState()
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        viewModel.getState()
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (v.layoutParams as ConstraintLayout.LayoutParams).topMargin = systemBars.top
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.txtPrivacy) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (v.layoutParams as ConstraintLayout.LayoutParams).bottomMargin =
                systemBars.bottom + getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
            insets
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        viewModel = tryKoinViewModel(listOf(paywallViewModels))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()

            listenEvents()
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

        binding.layoutPremium.setOnClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
            swipeUp()
        }

        binding.btnPay.setOnClickListener {
            makePurchase()
        }
    }

    private fun makePurchase() {
        try {
            LoadingDialog.show(this@SuperLikePaywallActivity)

            PaymentUtils.makePurchase(
                productType = ProductType.SUPER_LIKE,
                storeProduct = viewModel.selectedOffer.get()!!.availablePackages[0].product,
                onError = { error, userCancelled ->
                    logger("--purchase--", "error: ${gsonString(error)}")
                    logger("--purchase--", "userCancelled: $userCancelled")

                    LoadingDialog.hide()
                },
                onSuccess = { storeTransaction, customerInfo, entitlementInfo ->
                    val map = mutableMapOf<String, Any?>()
                    map["is_topup"] = 1
                    map["top_up_type"] = 1
                    map["product_id"] = storeTransaction.productIds[0]
                    map["start_date"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Timestamp(storeTransaction.purchaseTime).toInstant().epochSecond
                    } else {
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = storeTransaction.purchaseTime
                        calendar.timeInMillis / 1000 // Convert milliseconds to seconds
                    }
                    map["is_guest"] = false

                    logger("--purchase--", "map: ${gsonString(map)}")

                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.makePurchase(map).collectLatest {
                            observePurchase(it)
                        }
                    }
                },
            )
        } catch (e: Exception) {
            catchLog("makePurchase: ${gsonString(e)}")
        }
    }

    private fun getSuperLikeOffers() {
        PaymentUtils.getOffers(productType = ProductType.SUPER_LIKE, isLoading = {
            if (it) {
                viewModel.isLoading.set(true)
                LoadingDialog.show(this@SuperLikePaywallActivity)
            } else {
                LoadingDialog.hide()
            }
        }, onError = { error ->
            Toast.makeText(this@SuperLikePaywallActivity, error.message, Toast.LENGTH_SHORT).show()
        }, onSuccess = { offers ->

            if (offers.isEmpty()) {
                Toast.makeText(this@SuperLikePaywallActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                onBackPressedDispatcher.onBackPressed()
                return@getOffers
            }

            val offerList = ArrayList<Offering>()
            offerList.addAll(offers)
            offerList.sortBy {
                try {
                    if (it.metadata.contains("sequence")) {
                        val sequence = it.metadata["sequence"]
                        (sequence!! as String).toInt()
                    } else {
                        0
                    }
                } catch (e: Exception) {
                    catchLog("sortBy: ${gsonString(e)}")
                    0
                }
            }

            viewModel.superLikePlanList.set(offerList)

            var defaultPosition = offerList.indexOfFirst {
                if (it.metadata.contains("default")) {
                    it.metadata["default"] == true
                } else {
                    false
                }
            }

            defaultPosition = if (defaultPosition == -1) 0 else defaultPosition

            viewModel.selectedOffer.set(viewModel.superLikePlanList.get()!![defaultPosition])

            adapter.setSelectedPosition(defaultPosition)

            updateSelectedPlan()

            logger("--super_likes--", gsonString(viewModel.superLikePlanList.get()?.map { it.metadata["title"] }?.toList()))

            adapter.paywallList = offerList

            viewModel.isLoading.set(false)
        })
    }

    private fun updateSelectedPlan() {
        val price = viewModel.selectedOffer.get()!!.availablePackages[0].product.price.formatted
        val title = viewModel.selectedOffer.get()!!.metadata["title"] as String

        viewModel.buttonText.set("Get $title for $price")
    }

    private fun setUpRecyclerView() {
        if (binding.rvSuperLikePlans.layoutManager == null) {
            binding.rvSuperLikePlans.layoutManager = LinearLayoutManager(this@SuperLikePaywallActivity, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvSuperLikePlans.adapter == null) {
            binding.rvSuperLikePlans.adapter = this@SuperLikePaywallActivity.adapter
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

        premiumBannerUI()

        setUpRecyclerView()

        viewModel.remainingSuperLikesCount.set(getUserPrefs().remainingSuperLikes.toString())
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
        binding.layoutPremium.clearAnimation()
        binding.txtFeature.clearAnimation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler.hasCallbacks(runnable)) {
                handler.removeCallbacks(runnable)
            }
        } else {
            handler.removeCallbacksAndMessages(null)
        }

        val premiumTitle = "Enjoy Exclusive Features with Premium"
        val spannablePremiumTitle = SpannableString(premiumTitle)

        // Styling for "Enjoy Exclusive Features with Premium"
        val premiumTitleItalicStart = premiumTitle.indexOf(premiumTitle)
        val premiumTitleItalicEnd = premiumTitleItalicStart + premiumTitle.length
        spannablePremiumTitle.setSpan(
            StyleSpan(Typeface.ITALIC), premiumTitleItalicStart, premiumTitleItalicEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
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

    private fun CoroutineScope.observePurchase(paymentResource: Resource<PaymentResponse?>) {
        launch(Dispatchers.Main) {
            when (paymentResource.status) {
                Status.LOADING -> {
                    LoadingDialog.show(this@SuperLikePaywallActivity)
                }

                Status.SIGN_OUT -> {
                    Toast.makeText(this@SuperLikePaywallActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ADMIN_BLOCKED -> {
                    Toast.makeText(this@SuperLikePaywallActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ERROR -> {
                    LoadingDialog.hide()

                    Toast.makeText(this@SuperLikePaywallActivity, paymentResource.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                }

                Status.SUCCESS -> {
                    LoadingDialog.hide()

                    val data = paymentResource.data?.payment

                    if (data != null) {
                        getUserPrefs().remainingLikes = data.profileBalanceModel.totalLikes
                        getUserPrefs().remainingDiamonds = data.profileBalanceModel.totalDiamonds
                        getUserPrefs().remainingSuperLikes = data.profileBalanceModel.totalSuperLikes
                        getUserPrefs().remainingBoosts = data.profileBalanceModel.totalBoosts
                        getUserPrefs().remainingRewinds = data.profileBalanceModel.totalRewinds

                        viewModel.remainingSuperLikesCount.set(getUserPrefs().remainingSuperLikes.toString())

                        val purchaseItem = viewModel.selectedOffer.get()?.metadata?.get("super_like_count")
                        Toast.makeText(this@SuperLikePaywallActivity, "$purchaseItem Super Likes has been Purchased", Toast.LENGTH_SHORT).show()

                        EventManager.postEvent(Event(EventConstants.UPDATE_SUPER_LIKE_COUNT, null))
                    } else {
                        Toast.makeText(this@SuperLikePaywallActivity, paymentResource.message ?: "Something went wrong...!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun authOut() {
        LoadingDialog.show(this)

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@SuperLikePaywallActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        if (viewModel.superLikePlanList.get() == null) {
            getSuperLikeOffers()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected && viewModel.superLikePlanList.get() == null) {
            getSuperLikeOffers()
        }
    }

    override fun onOfferClick(position: Int) {
        viewModel.selectedOffer.set(viewModel.superLikePlanList.get()!![position])
        updateSelectedPlan()
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        if (key == EventConstants.UPDATE_PURCHASE) {
            viewModel.remainingSuperLikesCount.set(getUserPrefs().remainingSuperLikes.toString())
        }
    }
}
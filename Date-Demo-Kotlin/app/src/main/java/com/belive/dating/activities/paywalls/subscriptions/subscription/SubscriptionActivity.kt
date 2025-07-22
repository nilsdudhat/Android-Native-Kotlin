package com.belive.dating.activities.paywalls.subscriptions.subscription

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.Purchase
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.paywalls.subscriptions.success.gold.GoldPaymentSuccessActivity
import com.belive.dating.activities.paywalls.subscriptions.success.lifetime.LifetimePaymentSuccessActivity
import com.belive.dating.activities.paywalls.subscriptions.success.platinum.PlatinumPaymentSuccessActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.api.cancel_subscription.GooglePlayRetrofitInstance
import com.belive.dating.api.user.models.payment.PaymentResponse
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivitySubscriptionBinding
import com.belive.dating.di.paywallViewModels
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.convertDateString
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getGlide
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.openBrowser
import com.belive.dating.extensions.safeApiCallResponse
import com.belive.dating.extensions.swipeDown
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import com.belive.dating.payment.PayLoad
import com.belive.dating.payment.PaymentUtils
import com.belive.dating.payment.ProductType
import com.belive.dating.payment.activePlan
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.revenuecat.purchases.Offering
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.util.Calendar
import java.util.TimeZone

class SubscriptionActivity : NetworkReceiverActivity(), PlanProductAdapter.OnPlanListener {

    val binding by lazy {
        ActivitySubscriptionBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: SubscriptionViewModel

    private val goldFeatureAdapter by lazy {
        PlanFeatureAdapter()
    }

    private val platinumFeatureAdapter by lazy {
        PlanFeatureAdapter()
    }

    private val lifetimeFeatureAdapter by lazy {
        PlanFeatureAdapter()
    }

    private val goldPlanAdapter by lazy {
        PlanProductAdapter(this, ProductType.GOLD)
    }

    private val platinumPlanAdapter by lazy {
        PlanProductAdapter(this, ProductType.PLATINUM)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(SubscriptionActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(SubscriptionActivity::class.java.simpleName)
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutHeader) { v, insets ->
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

        viewModel = tryKoinViewModel(listOf(paywallViewModels))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        if (intent.hasExtra("is_gold_available")) {
            viewModel.isGoldNotAvailable.set(intent.getBooleanExtra("is_gold_available", true))
        } else {
            viewModel.isGoldNotAvailable.set(true)
        }

        binding.root.post {
            observeNetwork()
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

        binding.tabGold.setOnClickListener {
            if (viewModel.isGoldNotAvailable.get() == false) {
                Toast.makeText(
                    this,
                    intent.getStringExtra("restriction_message") ?: "This plan does not contains feature you have selected.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            if (viewModel.selectedPlanType.get() != ProductType.GOLD) {
                viewModel.selectedPlanType.set(ProductType.GOLD)

                initSubscriptions()
            }
        }

        binding.tabPlatinum.setOnClickListener {
            if (viewModel.selectedPlanType.get() != ProductType.PLATINUM) {
                viewModel.selectedPlanType.set(ProductType.PLATINUM)

                setUpPlatinumPlans()
            }
        }

        binding.tabLifetime.setOnClickListener {
            if (viewModel.selectedPlanType.get() != ProductType.LIFETIME) {
                viewModel.selectedPlanType.set(ProductType.LIFETIME)

                setUpLifetimePlan()
            }
        }

        binding.btnPay.setOnClickListener {
            if (viewModel.buttonText.get() == "Manage Subscription") {
                showManageSubscriptionDialog()
            } else {
                makePurchase()
            }
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

        // Plan Features RecyclerView
        binding.root.post {
            setUpGoldFeatures()
            setUpPlatinumFeatures()
            setUpLifetimeFeatures()
        }
    }

    private fun makePurchase() {
        try {
            LoadingDialog.show(this@SubscriptionActivity)

            PaymentUtils.makePurchase(
                productType = viewModel.selectedPlanType.get()!!,
                storeProduct = viewModel.selectedPlan.get()!![viewModel.selectedPlanType.get()!!]!!.availablePackages[0].product,
                onError = { error, userCancelled ->
                    logger("--purchase--", "error: ${gsonString(error)}")
                    logger("--purchase--", "userCancelled: $userCancelled")

                    LoadingDialog.hide()
                },
                onSuccess = { storeTransaction, customerInfo, entitlementInfo ->
                    if ((activePlan != null) && (viewModel.selectedPlanType.get() == ProductType.LIFETIME)) {
                        PaymentUtils.getPurchaseHistory { activeSubscriptions ->
                            logger("--purchase--", "activeSubscriptions: ${gsonString(activeSubscriptions)}")

                            val payload: PayLoad? = (activeSubscriptions[0] as Purchase).originalJson.fromJson()

                            val apiService = GooglePlayRetrofitInstance.createGooglePlayApiService(
                                this@SubscriptionActivity
                            )

                            lifecycleScope.launch(Dispatchers.IO) { // only launch will not work
                                try {
                                    val response = safeApiCallResponse {
                                        apiService.cancelSubscription(packageName, payload?.productId, payload?.purchaseToken)
                                    }

                                    if (response.isSuccessful) {
                                        activePlan = entitlementInfo

                                        val map = mutableMapOf<String, Any?>()
                                        map["is_topup"] = 0
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

                                        lifecycleScope.launch(Dispatchers.IO) { // only launch will not work
                                            viewModel.makePurchase(map).collectLatest {
                                                observePurchase(it)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    logger("--catch--", "error: ${gsonString(e)}")

                                    lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                                        LoadingDialog.hide()

                                        Toast.makeText(this@SubscriptionActivity, "Something went wrong..!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    } else {
                        activePlan = entitlementInfo

                        val map = mutableMapOf<String, Any?>()
                        map["is_guest"] = false
                        map["is_topup"] = 0

                        if (viewModel.selectedPlanType.get() == ProductType.LIFETIME) {
                            map["product_id"] = storeTransaction.productIds[0]
                            map["start_date"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Timestamp(storeTransaction.purchaseTime).toInstant().epochSecond
                            } else {
                                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                calendar.timeInMillis = storeTransaction.purchaseTime
                                calendar.timeInMillis / 1000 // Convert milliseconds to seconds
                            }
                        } else {
                            map["product_id"] = entitlementInfo.productPlanIdentifier
                            if (entitlementInfo.expirationDate != null) {
                                map["end_date"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    Timestamp(entitlementInfo.expirationDate!!.time).toInstant().epochSecond
                                } else {
                                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                    calendar.timeInMillis = entitlementInfo.expirationDate!!.time
                                    calendar.timeInMillis / 1000 // Convert milliseconds to seconds
                                }
                            }
                            map["start_date"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Timestamp(entitlementInfo.latestPurchaseDate.time).toInstant().epochSecond
                            } else {
                                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                calendar.timeInMillis = entitlementInfo.latestPurchaseDate.time
                                calendar.timeInMillis / 1000 // Convert milliseconds to seconds
                            }
                        }

                        logger("--purchase--", "map: ${gsonString(map)}")

                        lifecycleScope.launch(Dispatchers.IO) { // only launch will not work
                            viewModel.makePurchase(map).collectLatest {
                                observePurchase(it)
                            }
                        }
                    }
                })
        } catch (e: Exception) {
            catchLog("makePurchase: ${gsonString(e)}")

            LoadingDialog.hide()
        }
    }

    private fun CoroutineScope.observePurchase(paymentResource: Resource<PaymentResponse?>) {
        launch(Dispatchers.Main) {
            when (paymentResource.status) {
                Status.LOADING -> {
                    LoadingDialog.show(this@SubscriptionActivity)
                }

                Status.SIGN_OUT -> {
                    Toast.makeText(this@SubscriptionActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ADMIN_BLOCKED -> {
                    Toast.makeText(this@SubscriptionActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ERROR -> {
                    Toast.makeText(this@SubscriptionActivity, paymentResource.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                }

                Status.SUCCESS -> {
                    LoadingDialog.hide()

                    val paymentData = paymentResource.data?.payment

                    if (paymentResource.data != null && paymentData?.purchaseModel?.isTopUp == 0) {

                        getUserPrefs().activePackage = paymentData.purchaseModel.planName
                        getUserPrefs().purchaseStartDate = paymentData.purchaseModel.startDate
                        getUserPrefs().purchaseEndDate = paymentData.purchaseModel.endDate
                        getUserPrefs().isUnlimitedLikes = paymentData.purchaseModel.unlimitedLikes
                        getUserPrefs().isUnlimitedRewinds = paymentData.purchaseModel.unlimitedRewinds
                        getUserPrefs().isLifetimeBadge = paymentData.purchaseModel.lifetimeBadge
                        getUserPrefs().isPlatinumBadge = paymentData.purchaseModel.platinumBadge
                        getUserPrefs().isGoldBadge = paymentData.purchaseModel.goldBadge
                        getUserPrefs().isLocationFilter = paymentData.purchaseModel.locationFilter
                        getUserPrefs().isAiMatchMaker = paymentData.purchaseModel.aiMatchmaker
                        getUserPrefs().isNoAds = paymentData.purchaseModel.noAds
                        getUserPrefs().remainingDiamonds = paymentData.profileBalanceModel.totalDiamonds
                        getUserPrefs().remainingBoosts = paymentData.profileBalanceModel.totalBoosts
                        getUserPrefs().remainingSuperLikes = paymentData.profileBalanceModel.totalSuperLikes

                        getUserPrefs().customLatitude = getUserPrefs().currentLatitude
                        getUserPrefs().customLongitude = getUserPrefs().currentLongitude
                        getUserPrefs().customLocationName = null
                        getUserPrefs().customCountry = getUserPrefs().currentCountry

                        EventManager.postEvent(Event(EventConstants.UPDATE_PURCHASE, null))

                        if (viewModel.selectedPlanType.get() == ProductType.GOLD) {
                            startActivity(Intent(this@SubscriptionActivity, GoldPaymentSuccessActivity::class.java))
                        } else if (viewModel.selectedPlanType.get() == ProductType.PLATINUM) {
                            startActivity(Intent(this@SubscriptionActivity, PlatinumPaymentSuccessActivity::class.java))
                        } else if (viewModel.selectedPlanType.get() == ProductType.LIFETIME) {
                            startActivity(Intent(this@SubscriptionActivity, LifetimePaymentSuccessActivity::class.java))
                        }
                        finish()
                        swipeRight()
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

                startActivity(Intent(this@SubscriptionActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    private fun setUpGoldFeatures() {
        goldFeatureAdapter.list = viewModel.getGoldFeatureList()

        if (binding.rvGoldFeatures.layoutManager == null) {
            binding.rvGoldFeatures.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }

        if (binding.rvGoldFeatures.adapter == null) {
            binding.rvGoldFeatures.adapter = goldFeatureAdapter
        }
    }

    private fun setUpPlatinumFeatures() {
        platinumFeatureAdapter.list = viewModel.getPlatinumFeatureList()

        if (binding.rvPlatinumFeatures.layoutManager == null) {
            binding.rvPlatinumFeatures.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }

        if (binding.rvPlatinumFeatures.adapter == null) {
            binding.rvPlatinumFeatures.adapter = platinumFeatureAdapter
        }
    }

    private fun setUpLifetimeFeatures() {
        lifetimeFeatureAdapter.list = viewModel.getLifetimeFeatureList()

        if (binding.rvLifetimeFeatures.layoutManager == null) {
            binding.rvLifetimeFeatures.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }

        if (binding.rvLifetimeFeatures.adapter == null) {
            binding.rvLifetimeFeatures.adapter = lifetimeFeatureAdapter
        }
    }

    private fun initSubscriptions() {
        if (viewModel.isGoldNotAvailable.get() == false) {
            viewModel.selectedPlanType.set(ProductType.PLATINUM)
            setUpPlatinumPlans()
        } else {
            viewModel.selectedPlanType.set(ProductType.GOLD)
            setUpGoldPlans()
        }
    }

    private fun setUpPlatinumPlans() {
        viewModel.isLoading.set(true)

        if (viewModel.platinumPlanList.get()?.isNotEmpty() == true) {
            viewModel.isLoading.set(false)

            if (binding.rvPlatinumPlans.layoutManager == null) {
                binding.rvPlatinumPlans.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            }
            if (binding.rvPlatinumPlans.adapter == null) {
                binding.rvPlatinumPlans.adapter = platinumPlanAdapter
            }

            platinumPlanAdapter.list = viewModel.platinumPlanList.get()!!
            updatePrice(viewModel.selectedPlan.get()!![ProductType.PLATINUM]!!)
        } else {
            lifecycleScope.launch(Dispatchers.IO) { // only launch will not work
                PaymentUtils.getOffers(
                    productType = ProductType.PLATINUM,
                    isLoading = { isLoading ->
                        lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                            if (isLoading) {
                                LoadingDialog.show(this@SubscriptionActivity)
                            }
                        }
                    },
                    onError = { error ->
                        logger("--subs--", "onError: ${gsonString(error)}")
                        lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                            LoadingDialog.hide()
                            Toast.makeText(this@SubscriptionActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSuccess = { offers ->
                        logger("--subs--", "onSuccess: ${gsonString(offers)}")

                        val weekPlan = offers.filter { it.availablePackages[0].product.period?.iso8601 == "P1W" }
                        val monthPlan = offers.filter { it.availablePackages[0].product.period?.iso8601 == "P1M" }
                        val sixMonthPlan = offers.filter { it.availablePackages[0].product.period?.iso8601 == "P6M" }
                        val yearPlan = offers.filter { it.availablePackages[0].product.period?.iso8601 == "P1Y" }

                        val list = ArrayList<Offering>()

                        if (weekPlan.isNotEmpty()) {
                            list.add(weekPlan[0])
                        }
                        if (monthPlan.isNotEmpty()) {
                            list.add(monthPlan[0])
                        }
                        if (sixMonthPlan.isNotEmpty()) {
                            list.add(sixMonthPlan[0])
                        }
                        if (yearPlan.isNotEmpty()) {
                            list.add(yearPlan[0])
                        }

                        viewModel.platinumPlanList.set(list)

                        lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                            platinumPlanAdapter.list = viewModel.platinumPlanList.get()!!

                            if (binding.rvPlatinumPlans.layoutManager == null) {
                                binding.rvPlatinumPlans.layoutManager =
                                    LinearLayoutManager(this@SubscriptionActivity, LinearLayoutManager.HORIZONTAL, false)
                            }

                            if (binding.rvPlatinumPlans.adapter == null) {
                                binding.rvPlatinumPlans.adapter = platinumPlanAdapter
                            }

                            var defaultPlan: Offering

                            try {
                                val defaultIndex = viewModel.platinumPlanList.get()!!.indexOfFirst {
                                    if (it.metadata.containsKey("default")) {
                                        it.metadata["default"] as Boolean
                                    } else {
                                        false
                                    }
                                }

                                if (defaultIndex >= 0) {
                                    defaultPlan = viewModel.platinumPlanList.get()!![defaultIndex]
                                    updateSelectedPlan(defaultPlan, ProductType.PLATINUM)

                                    LoadingDialog.hide()
                                    viewModel.isLoading.set(false)
                                    binding.executePendingBindings()

                                    binding.rvPlatinumPlans.post {
                                        binding.rvPlatinumPlans.scrollToPosition(defaultIndex)
                                        platinumPlanAdapter.setSelectedPosition(defaultIndex)
                                    }
                                } else {
                                    defaultPlan = viewModel.platinumPlanList.get()!![0]
                                    updateSelectedPlan(defaultPlan, ProductType.PLATINUM)

                                    LoadingDialog.hide()
                                    viewModel.isLoading.set(false)
                                    binding.executePendingBindings()
                                }
                            } catch (e: Exception) {
                                catchLog("setUpPlatinumPlans: ${gsonString(e)}")

                                defaultPlan = viewModel.platinumPlanList.get()!![0]
                                updateSelectedPlan(defaultPlan, ProductType.PLATINUM)

                                LoadingDialog.hide()
                                viewModel.isLoading.set(false)
                                binding.executePendingBindings()
                            }
                        }
                    },
                )
            }
        }
    }

    private fun setUpGoldPlans() {
        viewModel.isLoading.set(true)

        if (viewModel.goldPlanList.get()?.isNotEmpty() == true) {
            viewModel.isLoading.set(false)

            if (binding.rvGoldPlans.layoutManager == null) {
                binding.rvGoldPlans.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            }
            if (binding.rvGoldPlans.adapter == null) {
                binding.rvGoldPlans.adapter = goldPlanAdapter
            }

            goldPlanAdapter.list = viewModel.goldPlanList.get()!!
            updatePrice(viewModel.selectedPlan.get()!![ProductType.GOLD]!!)
        } else {
            lifecycleScope.launch(Dispatchers.IO) { // only launch will not work
                PaymentUtils.getOffers(
                    productType = ProductType.GOLD,
                    isLoading = { isLoading ->
                        lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                            if (isLoading) {
                                LoadingDialog.show(this@SubscriptionActivity)
                            }
                        }
                    },
                    onError = { error ->
                        logger("--subs--", "onError: ${gsonString(error)}")

                        lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                            LoadingDialog.hide()
                            Toast.makeText(this@SubscriptionActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSuccess = { offers ->
                        logger("--subs--", "onSuccess: ${gsonString(offers)}")

                        val weekPlan = offers.filter { it.availablePackages[0].product.period?.iso8601 == "P1W" }
                        val monthPlan = offers.filter { it.availablePackages[0].product.period?.iso8601 == "P1M" }
                        val sixMonthPlan = offers.filter { it.availablePackages[0].product.period?.iso8601 == "P6M" }
                        val yearPlan = offers.filter { it.availablePackages[0].product.period?.iso8601 == "P1Y" }

                        val list = ArrayList<Offering>()

                        if (weekPlan.isNotEmpty()) {
                            list.add(weekPlan[0])
                        }
                        if (monthPlan.isNotEmpty()) {
                            list.add(monthPlan[0])
                        }
                        if (sixMonthPlan.isNotEmpty()) {
                            list.add(sixMonthPlan[0])
                        }
                        if (yearPlan.isNotEmpty()) {
                            list.add(yearPlan[0])
                        }

                        viewModel.goldPlanList.set(list)

                        lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                            goldPlanAdapter.list = viewModel.goldPlanList.get()!!

                            if (binding.rvGoldPlans.layoutManager == null) {
                                binding.rvGoldPlans.layoutManager =
                                    LinearLayoutManager(this@SubscriptionActivity, LinearLayoutManager.HORIZONTAL, false)
                            }

                            if (binding.rvGoldPlans.adapter == null) {
                                binding.rvGoldPlans.adapter = goldPlanAdapter
                            }

                            var defaultPlan: Offering

                            try {
                                val defaultIndex = viewModel.goldPlanList.get()!!.indexOfFirst {
                                    if (it.metadata.containsKey("default")) {
                                        it.metadata["default"] as Boolean
                                    } else {
                                        false
                                    }
                                }

                                if (defaultIndex >= 0) {
                                    defaultPlan = viewModel.goldPlanList.get()!![defaultIndex]
                                    updateSelectedPlan(defaultPlan, ProductType.GOLD)

                                    LoadingDialog.hide()
                                    viewModel.isLoading.set(false)
                                    binding.executePendingBindings()

                                    binding.rvGoldPlans.post {
                                        binding.rvGoldPlans.scrollToPosition(defaultIndex)
                                        goldPlanAdapter.setSelectedPosition(defaultIndex)
                                    }
                                } else {
                                    defaultPlan = viewModel.goldPlanList.get()!![0]
                                    updateSelectedPlan(defaultPlan, ProductType.GOLD)

                                    LoadingDialog.hide()
                                    viewModel.isLoading.set(false)
                                    binding.executePendingBindings()
                                }
                            } catch (e: Exception) {
                                catchLog("setUpGoldPlans: ${gsonString(e)}")

                                defaultPlan = viewModel.goldPlanList.get()!![0]
                                updateSelectedPlan(defaultPlan, ProductType.GOLD)

                                LoadingDialog.hide()
                                viewModel.isLoading.set(false)
                                binding.executePendingBindings()
                            }
                        }
                    },
                )
            }
        }
    }

    private fun setUpLifetimePlan() {
        viewModel.isLoading.set(true)

        if (viewModel.lifetimePlan.get() != null) {
            viewModel.isLoading.set(false)

            updatePrice(viewModel.lifetimePlan.get()!!)
        } else {
            lifecycleScope.launch(Dispatchers.IO) { // only launch will not work
                PaymentUtils.getOffers(
                    productType = ProductType.LIFETIME,
                    isLoading = { isLoading ->
                        lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                            if (isLoading) {
                                LoadingDialog.show(this@SubscriptionActivity)
                            } else {
                                LoadingDialog.hide()
                            }
                        }
                    },
                    onError = { error ->
                        logger("--subs--", "onError: ${gsonString(error)}")

                        lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                            LoadingDialog.hide()
                            Toast.makeText(this@SubscriptionActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSuccess = { offers ->
                        logger("--subs--", "onSuccess: ${gsonString(offers)}")

                        viewModel.lifetimePlan.set(offers[0])

                        lifecycleScope.launch(Dispatchers.Main) { // only launch will not work
                            viewModel.isLoading.set(false)
                            binding.executePendingBindings()

                            updateSelectedPlan(viewModel.lifetimePlan.get()!!, ProductType.LIFETIME)
                        }
                    },
                )
            }
        }
    }

    private fun updateSelectedPlan(offer: Offering, productType: ProductType) {
        try {
            viewModel.selectedPlan.apply {
                val map = get() ?: mutableMapOf()
                map[viewModel.selectedPlanType.get()!!] = offer
                set(map)
            }

            val isActive = if (activePlan == null) {
                false
            } else {
                try {
                    offer.availablePackages[0].product.id.contains(activePlan!!.productPlanIdentifier!!)
                } catch (e: Exception) {
                    false
                }
            }
            if (isActive) {
                viewModel.buttonText.set("Manage Subscription")
            } else {
                val price = offer.availablePackages[0].product.price.formatted
                val unit = offer.availablePackages[0].product.period?.unit.toString().lowercase().let {
                    it.replaceFirstChar { char -> char.uppercaseChar() }
                }

                val title = if (unit.equals("null", true) && (offer.availablePackages[0].product.period?.value == null)) {
                    StringBuilder().append("Lifetime")
                } else {
                    StringBuilder().append(offer.availablePackages[0].product.period?.value).append(" ").append(unit)
                }

                viewModel.buttonText.set("Get $title for $price")
            }

            when (productType) {
                ProductType.GOLD -> {
                    goldFeatureAdapter.update(viewModel.selectedPlan.get()!![ProductType.GOLD]!!, ProductType.GOLD)
                }

                ProductType.PLATINUM -> {
                    platinumFeatureAdapter.update(viewModel.selectedPlan.get()!![ProductType.PLATINUM]!!, ProductType.PLATINUM)
                }

                ProductType.LIFETIME -> {
                    lifetimeFeatureAdapter.update(viewModel.selectedPlan.get()!![ProductType.LIFETIME]!!, ProductType.LIFETIME)
                }

                else -> {

                }
            }
        } catch (e: Exception) {
            catchLog("updateSelectedPlan: ${gsonString(e)}")

            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun updatePrice(offer: Offering) {
        if (viewModel.selectedPlanType.get() == ProductType.GOLD) {
            updateSelectedPlan(offer, ProductType.GOLD)

            val index = viewModel.goldPlanList.get()?.indexOf(offer)
            index?.let { binding.rvGoldPlans.scrollToPosition(it) }
        } else if (viewModel.selectedPlanType.get() == ProductType.PLATINUM) {
            updateSelectedPlan(offer, ProductType.PLATINUM)

            val index = viewModel.platinumPlanList.get()?.indexOf(offer)
            index?.let { binding.rvGoldPlans.scrollToPosition(it) }
        } else if (viewModel.selectedPlanType.get() == ProductType.LIFETIME) {
            updateSelectedPlan(offer, ProductType.LIFETIME)
        }
    }

    private fun showManageSubscriptionDialog() {
        logger("--manage--", "showManageSubscriptionDialog: ")

        val manageDialog = Dialog(this)
        manageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        manageDialog.setCancelable(false)
        manageDialog.setContentView(R.layout.dialog_manage_subscription)
        manageDialog.setCanceledOnTouchOutside(true)
        val window = manageDialog.window
        window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setDimAmount(0.75f)
        manageDialog.show()

        val txtDesc1 = manageDialog.findViewById<TextView>(R.id.txt_desc_1)
        val txtDesc2 = manageDialog.findViewById<TextView>(R.id.txt_desc_2)
        val txtNegative = manageDialog.findViewById<TextView>(R.id.txt_negative)
        val txtPositive = manageDialog.findViewById<TextView>(R.id.txt_positive)

        val renewDate = convertDateString(getUserPrefs().purchaseEndDate!!)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtDesc1.text = Html.fromHtml("Your subscription ends <b>$renewDate</b>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            txtDesc1.text = Html.fromHtml("Your subscription ends <b>$renewDate</b>")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtDesc2.text = Html.fromHtml("Manage now on the <b>Play Store</b>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            txtDesc2.text = Html.fromHtml("Manage now on the <b>Play Store</b>")
        }

        txtNegative.setOnClickListener {
            manageDialog.dismiss()
        }

        txtPositive.setOnClickListener {
            manageDialog.dismiss()

            openBrowser("https://play.google.com/store/account/subscriptions?package=${packageName}")
        }
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        if (viewModel.selectedPlan.get() == null) {
            initSubscriptions()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected && viewModel.selectedPlan.get() == null) {
            initSubscriptions()
        }
    }

    override fun onClickCallBack(offer: Offering) {
        viewModel.selectedPlan.apply {
            val map = get() ?: mutableMapOf()
            map[viewModel.selectedPlanType.get()!!] = offer
            set(map)
        }

        updatePrice(offer)
    }
}
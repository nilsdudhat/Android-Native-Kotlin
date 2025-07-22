package com.belive.dating.payment

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getDeviceID
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.onesignal.OneSignal
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.EntitlementInfo
import com.revenuecat.purchases.EntitlementVerificationMode
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.Store
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.models.GoogleReplacementMode
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

var goldProductList = ArrayList<Offering>()
var platinumProductList = ArrayList<Offering>()
var boostProductList = ArrayList<Offering>()
var diamondProductList = ArrayList<Offering>()
var superLikeProductList = ArrayList<Offering>()
var likeProductList = ArrayList<Offering>()
var rewindProductList = ArrayList<Offering>()
var lifetimeProductList = ArrayList<Offering>()

var activePlan: EntitlementInfo? = null
var otherActivePlans: ArrayList<EntitlementInfo> = ArrayList()

enum class ProductType(val value: String) {
    GOLD("Gold"),
    PLATINUM("Platinum"),
    LIFETIME("Lifetime"),
    BOOST("Boost"),
    DIAMOND("Diamond"),
    SUPER_LIKE("SuperLike"),
    LIKE("Like"),
    REWIND("Rewind"),
}

object PaymentUtils {

    init {
        if (!Purchases.isConfigured) {
            Purchases.logLevel = LogLevel.DEBUG

            try {
                Purchases.configure(
                    PurchasesConfiguration.Builder(
                        getKoinContext(),
                        getGistPrefs().revenueCatKey,
                    ).entitlementVerificationMode(EntitlementVerificationMode.INFORMATIONAL).appUserID(getUserPrefs().userId.toString())
                        .store(Store.PLAY_STORE).build()
                )
                Purchases.sharedInstance.setAttributes(
                    mapOf(
                        "email" to "${getUserPrefs().emailId}",
                        "androidId" to "${getKoinContext().getDeviceID()}",
                        "fcmTokens" to "${getUserPrefs().fcmToken}",
                        "mixpanelDistinctId" to "${getKoinContext().getDeviceID()}", // as mixpanel distinct id assigned as device id
                        "onesignalUserId" to OneSignal.User.onesignalId,
                    )
                )
            } catch (e: Exception) {
                catchLog("PaymentUtils-init: ${gsonString(e)}")
            }
        }
    }

    fun reInit(onSuccess: () -> Unit, onError: (PurchasesError) -> Unit) {
        Purchases.sharedInstance.logOut(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                onError.invoke(error)
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                Purchases.logLevel = LogLevel.DEBUG
                Purchases.configure(
                    PurchasesConfiguration.Builder(
                        getKoinContext(),
                        getGistPrefs().revenueCatKey,
                    ).entitlementVerificationMode(EntitlementVerificationMode.INFORMATIONAL).appUserID(getUserPrefs().userId.toString())
                        .store(Store.PLAY_STORE).build()
                )
                Purchases.sharedInstance.setAttributes(
                    mapOf(
                        "email" to "${getUserPrefs().emailId}",
                        "androidId" to "${getKoinContext().getDeviceID()}",
                        "fcmTokens" to "${getUserPrefs().fcmToken}",
                        "mixpanelDistinctId" to "${getKoinContext().getDeviceID()}", // as mixpanel distinct id assigned as device id
                        "onesignalUserId" to OneSignal.User.onesignalId,
                    )
                )
                onSuccess.invoke()
            }
        })
    }

    fun getOffers(
        productType: ProductType,
        isLoading: (Boolean) -> Unit,
        onError: (PurchasesError) -> Unit,
        onSuccess: (ArrayList<Offering>) -> Unit,
    ) {
        isLoading.invoke(true)

        var productList = ArrayList<Offering>()

        when (productType) {
            ProductType.GOLD -> {
                productList = goldProductList
            }

            ProductType.PLATINUM -> {
                productList = platinumProductList
            }

            ProductType.LIFETIME -> {
                productList = lifetimeProductList
            }

            ProductType.BOOST -> {
                productList = boostProductList
            }

            ProductType.DIAMOND -> {
                productList = diamondProductList
            }

            ProductType.SUPER_LIKE -> {
                productList = superLikeProductList
            }

            ProductType.LIKE -> {
                productList = likeProductList
            }

            ProductType.REWIND -> {
                productList = rewindProductList
            }
        }

        if (productList.isNotEmpty()) {
            onSuccess.invoke(productList)
            isLoading.invoke(false)
        } else {
            Purchases.sharedInstance.getOfferingsWith(
                onSuccess = { offers ->
                    isLoading.invoke(false)

                    logger("--subs--", "getOffers: ${gsonString(offers)}")

                    // gold product offers
                    val goldKeys = offers.all.keys.filter { it.contains("gold") }

                    val goldOffers = arrayListOf<Offering>()
                    for (key in goldKeys) {
                        goldOffers.add(offers.all[key]!!)
                    }
                    goldProductList = goldOffers

                    // platinum product offers
                    val platinumKeys = offers.all.keys.filter { it.contains("platinum") }

                    val platinumOffers = arrayListOf<Offering>()
                    for (key in platinumKeys) {
                        platinumOffers.add(offers.all[key]!!)
                    }
                    platinumProductList = platinumOffers

                    // lifetime product offer
                    val lifetimeKeys = offers.all.keys.filter { it.contains("lifetime") }

                    val lifetimeOffers = arrayListOf<Offering>()
                    for (key in lifetimeKeys) {
                        lifetimeOffers.add(offers.all[key]!!)
                    }
                    lifetimeProductList = lifetimeOffers

                    // boost product offers
                    val boostKeys = offers.all.keys.filter { it.contains("boost") }

                    val boostOffers = arrayListOf<Offering>()
                    for (key in boostKeys) {
                        boostOffers.add(offers.all[key]!!)
                    }
                    boostProductList = boostOffers

                    // like product offers
                    val likeKeys = offers.all.keys.filter { it.contains("offer_like") }

                    val likeOffers = arrayListOf<Offering>()
                    for (key in likeKeys) {
                        likeOffers.add(offers.all[key]!!)
                    }
                    likeProductList = likeOffers

                    // rewind product offers
                    val rewindKeys = offers.all.keys.filter { it.contains("rewind") }

                    val rewindOffers = arrayListOf<Offering>()
                    for (key in rewindKeys) {
                        rewindOffers.add(offers.all[key]!!)
                    }
                    rewindProductList = rewindOffers

                    // super like product offers
                    val superLikeKeys = offers.all.keys.filter { it.contains("superlike") }

                    val superLikeOffers = arrayListOf<Offering>()
                    for (key in superLikeKeys) {
                        superLikeOffers.add(offers.all[key]!!)
                    }
                    superLikeProductList = superLikeOffers

                    // diamond product offers
                    val diamondKeys = offers.all.keys.filter { it.contains("diamond") }

                    val diamondOffers = arrayListOf<Offering>()
                    for (key in diamondKeys) {
                        diamondOffers.add(offers.all[key]!!)
                    }
                    diamondProductList = diamondOffers

                    when (productType) {
                        ProductType.GOLD -> {
                            onSuccess.invoke(goldProductList)
                        }

                        ProductType.PLATINUM -> {
                            onSuccess.invoke(platinumProductList)
                        }

                        ProductType.LIFETIME -> {
                            onSuccess.invoke(lifetimeProductList)
                        }

                        ProductType.BOOST -> {
                            onSuccess.invoke(boostProductList)
                        }

                        ProductType.SUPER_LIKE -> {
                            onSuccess.invoke(superLikeProductList)
                        }

                        ProductType.LIKE -> {
                            onSuccess.invoke(likeProductList)
                        }

                        ProductType.REWIND -> {
                            onSuccess.invoke(rewindProductList)
                        }

                        ProductType.DIAMOND -> {
                            onSuccess.invoke(diamondProductList)
                        }
                    }
                },
                onError = { error: PurchasesError ->
                    onError.invoke(error)
                    isLoading.invoke(false)
                }
            )
        }
    }

    fun getActiveSubscription(onSuccess: (EntitlementInfo?) -> Unit) {
        Purchases.sharedInstance.getCustomerInfoWith {
            logger("--subs--", "getPurchaseInfo: ${gsonString(it)}")
            logger("--subs--", "managementURL: ${it.managementURL}")

            val activeSubscriptions = ArrayList(it.entitlements.active.values)
            if (activeSubscriptions.isNotEmpty()) {
                var goldPackage: EntitlementInfo? = null
                var platinumPackage: EntitlementInfo? = null
                var lifetimePackage: EntitlementInfo? = null

                activeSubscriptions.forEach { entitlementInfo ->
                    when (entitlementInfo.identifier) {
                        "gold" -> {
                            goldPackage = entitlementInfo
                        }

                        "platinum" -> {
                            platinumPackage = entitlementInfo
                        }

                        "lifetime" -> {
                            lifetimePackage = entitlementInfo
                        }
                    }
                }

                if (lifetimePackage != null) {
                    onSuccess.invoke(lifetimePackage)
                    if (platinumPackage != null) {
                        otherActivePlans.add(platinumPackage!!)
                    }
                    if (goldPackage != null) {
                        otherActivePlans.add(goldPackage!!)
                    }
                } else if (platinumPackage != null) {
                    onSuccess.invoke(platinumPackage)
                } else if (goldPackage != null) {
                    onSuccess.invoke(goldPackage)
                }
            } else {
                onSuccess.invoke(null)
            }
        }
    }

    fun makePurchase(
        productType: ProductType,
        storeProduct: StoreProduct,
        onError: (String?, userCancelled: Boolean) -> Unit,
        onSuccess: (StoreTransaction, CustomerInfo, EntitlementInfo) -> Unit,
    ) {
        logger("--purchase--", "basePlanId: ${storeProduct.id}")
        logger("--purchase--", "storeProduct: ${gsonString(storeProduct)}")

        if ((activePlan == null) || storeProduct.type == com.revenuecat.purchases.ProductType.INAPP) {
            // Making Direct Purchase ====================================================
            Purchases.sharedInstance.purchase(
                PurchaseParams.Builder(getKoinActivity(), storeProduct)
//                    .googleReplacementMode(GoogleReplacementMode.WITHOUT_PRORATION)
                    .build(),
                object : PurchaseCallback {
                    override fun onCompleted(
                        storeTransaction: StoreTransaction,
                        customerInfo: CustomerInfo,
                    ) {
                        logger("--purchase--", "purchaseWith onSuccess storeTransaction: ${gsonString(storeTransaction)}")
                        logger("--purchase--", "purchaseWith onSuccess customerInfo: ${gsonString(customerInfo)}")

                        val entitlementKey = when (productType) {
                            ProductType.GOLD -> "gold"
                            ProductType.PLATINUM -> "platinum"
                            ProductType.LIFETIME -> "lifetime"
                            ProductType.SUPER_LIKE -> "super_likes"
                            ProductType.DIAMOND -> "diamonds"
                            ProductType.BOOST -> "boosts"
                            ProductType.LIKE -> "likes"
                            ProductType.REWIND -> "rewinds"
                        }
                        val entitlement: EntitlementInfo? =
                            customerInfo.entitlements[entitlementKey]
                        if (entitlement != null && entitlement.isActive) {
                            logger("--purchase--", "purchaseWith onSuccess activePlan: ${gsonString(entitlement)}")
                            onSuccess.invoke(storeTransaction, customerInfo, entitlement)
                        }
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        logger("--purchase--", "purchaseWith onError error: ${gsonString(error)}")
                        logger("--purchase--", "purchaseWith onError userCancelled: $userCancelled")

                        onError.invoke(error.message, userCancelled)
                    }
                }
            )
        } else {
            // Making Purchase with Upgrade / Downgrade Plan
            Purchases.sharedInstance.purchase(
                PurchaseParams.Builder(getKoinActivity(), storeProduct)
                    .oldProductId("${activePlan!!.productIdentifier}:${activePlan!!.productPlanIdentifier}")
                    .googleReplacementMode(GoogleReplacementMode.WITH_TIME_PRORATION)
                    .build(),
                object : PurchaseCallback {
                    override fun onCompleted(
                        storeTransaction: StoreTransaction,
                        customerInfo: CustomerInfo,
                    ) {
                        logger("--purchase--", "purchaseWith onSuccess storeTransaction: ${gsonString(storeTransaction)}")
                        logger("--purchase--", "purchaseWith onSuccess customerInfo: ${gsonString(customerInfo)}")

                        val entitlementKey = when (productType) {
                            ProductType.GOLD -> "gold"
                            ProductType.PLATINUM -> "platinum"
                            else -> ""
                        }
                        val entitlement: EntitlementInfo? =
                            customerInfo.entitlements[entitlementKey]
                        if (entitlement != null && entitlement.isActive) {
                            logger("--purchase--", "purchaseWith onSuccess activePlan: ${gsonString(entitlement)}")
                            onSuccess.invoke(storeTransaction, customerInfo, entitlement)
                        }
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        logger("--purchase--", "purchaseWith onError error: ${gsonString(error)}")
                        logger("--purchase--", "purchaseWith onError userCancelled: $userCancelled")

                        onError.invoke(error.message, userCancelled)
                    }
                }
            )
        }
    }

    private suspend fun queryPurchaseSubsActive(client: BillingClient, onListReceived: (List<Purchase>) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(params) { _, purchaseList ->
            logger("--purchases--", "queryPurchaseSubsActive: $purchaseList")
            onListReceived.invoke(purchaseList)
        }
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { _, _ ->

    }

    fun getPurchaseHistory(purchaseCallback: (ArrayList<Any>) -> Unit) {
        val client =
            BillingClient.newBuilder(getKoinActivity())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                )
                .build()

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    CoroutineScope(Dispatchers.IO).launch {
                        queryPurchaseSubsActive(client) { subscriptionsList ->
                            client.endConnection()

                            val purchaseList = arrayListOf<Any>()

                            subscriptionsList.let {
                                purchaseList.addAll(subscriptionsList)
                            }

                            purchaseCallback.invoke(purchaseList)
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                client.endConnection()
            }
        })
    }
}
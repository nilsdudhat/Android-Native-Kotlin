package com.belive.dating.preferences.pref_helpers

import com.belive.dating.ads.custom.CustomAd
import com.belive.dating.constants.AdsConstants
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.preferences.pref_utils.AdPrefUtils
import com.google.gson.JsonObject

object AdsPrefs {

    private val adPrefUtils = getKoinObject().get<AdPrefUtils>()
    private val adPrefsEditor = adPrefUtils.getPrefEditor()

    var admobBannerIds: List<String>? = listOf("ca-app-pub-3940256099942544/9214589741")
        get() {
            return adPrefUtils.getStringList(AdsConstants.ADMOB_BANNER_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.ADMOB_BANNER_ID, value)
        }

    var admobNativeIds: List<String>? = listOf("ca-app-pub-3940256099942544/2247696110")
        get() {
            return adPrefUtils.getStringList(AdsConstants.ADMOB_NATIVE_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.ADMOB_NATIVE_ID, value)
        }

    var admobInterstitialIds: List<String>? = listOf("ca-app-pub-3940256099942544/1033173712")
        get() {
            return adPrefUtils.getStringList(AdsConstants.ADMOB_INTERSTITIAL_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.ADMOB_INTERSTITIAL_ID, value)
        }

    var admobRewardedIds: List<String>? = listOf("ca-app-pub-3940256099942544/5224354917")
        get() {
            return adPrefUtils.getStringList(AdsConstants.ADMOB_REWARDED_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.ADMOB_REWARDED_ID, value)
        }

    var admobAppOpenIds: List<String>? = listOf("ca-app-pub-3940256099942544/9257395921")
        get() {
            return adPrefUtils.getStringList(AdsConstants.ADMOB_APP_OPEN_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.ADMOB_APP_OPEN_ID, value)
        }

    var facebookBannerIds: List<String>? = listOf("IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID")
        get() {
            return adPrefUtils.getStringList(AdsConstants.FACEBOOK_BANNER_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.FACEBOOK_BANNER_ID, value)
        }

    var facebookNativeIds: List<String>? = listOf("YOUR_PLACEMENT_ID")
        get() {
            return adPrefUtils.getStringList(AdsConstants.FACEBOOK_NATIVE_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.FACEBOOK_NATIVE_ID, value)
        }

    var facebookNativeBannerIds: List<String>? = listOf("YOUR_PLACEMENT_ID")
        get() {
            return adPrefUtils.getStringList(AdsConstants.FACEBOOK_NATIVE_BANNER_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.FACEBOOK_NATIVE_BANNER_ID, value)
        }

    var facebookInterstitialIds: List<String>? = listOf("YOUR_PLACEMENT_ID")
        get() {
            return adPrefUtils.getStringList(AdsConstants.FACEBOOK_INTERSTITIAL_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.FACEBOOK_INTERSTITIAL_ID, value)
        }

    var facebookRewardedIds: List<String>? = listOf("YOUR_PLACEMENT_ID")
        get() {
            return adPrefUtils.getStringList(AdsConstants.FACEBOOK_REWARDED_ID)
        }
        set(value) {
            field = value
            adPrefUtils.setStringList(AdsConstants.FACEBOOK_REWARDED_ID, value)
        }

    var displayAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_ADS, value)
        }

    var displayBannerAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_BANNER_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_BANNER_ADS, value)
        }

    var displayNativeAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_NATIVE_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_NATIVE_ADS, value)
        }

    var displayInterstitialAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_INTERSTITIAL_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_INTERSTITIAL_ADS, value)
        }

    var displayRewardedAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_REWARDED_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_REWARDED_ADS, value)
        }

    var adPriorityList: String? = null // google, facebook, custom
        get() {
            return adPrefUtils.getString(AdsConstants.AD_PRIORITY)
        }
        set(value) {
            field = value
            adPrefUtils.setString(AdsConstants.AD_PRIORITY, value)
        }

    var isAlternative: Boolean = true
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_ALTERNATIVE, true)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_ALTERNATIVE, value)
        }

    var interstitialAdInterval: Int = 5
        get() {
            return adPrefUtils.getInt(AdsConstants.INTERSTITIAL_AD_INTERVAL, 5)
        }
        set(value) {
            field = value
            adPrefUtils.setInt(AdsConstants.INTERSTITIAL_AD_INTERVAL, value)
        }

    var displayAdmobAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_ADMOB_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_ADMOB_ADS, value)
        }

    var displayAdmobBannerAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_ADMOB_BANNER_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_ADMOB_BANNER_ADS, value)
        }

    var displayAdmobNativeAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_ADMOB_NATIVE_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_ADMOB_NATIVE_ADS, value)
        }

    var displayAdmobInterstitialAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_ADMOB_INTERSTITIAL_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_ADMOB_INTERSTITIAL_ADS, value)
        }

    var displayAdmobRewardedAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_ADMOB_REWARDED_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_ADMOB_REWARDED_ADS, value)
        }

    var displayAdmobAppOpenAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_ADMOB_APP_OPEN_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_ADMOB_APP_OPEN_ADS, value)
        }

    var displayFacebookAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_FACEBOOK_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_FACEBOOK_ADS, value)
        }

    var displayFacebookBannerAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_FACEBOOK_BANNER_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_FACEBOOK_BANNER_ADS, value)
        }

    var displayFacebookNativeAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_FACEBOOK_NATIVE_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_FACEBOOK_NATIVE_ADS, value)
        }

    var displayFacebookInterstitialAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_FACEBOOK_INTERSTITIAL_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_FACEBOOK_INTERSTITIAL_ADS, value)
        }

    var displayFacebookRewardedAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_FACEBOOK_REWARDED_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_FACEBOOK_REWARDED_ADS, value)
        }

    var displayCustomAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_CUSTOM_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_CUSTOM_ADS, value)
        }

    var displayCustomBannerAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_CUSTOM_BANNER_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_CUSTOM_BANNER_ADS, value)
        }

    var displayCustomNativeAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_CUSTOM_NATIVE_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_CUSTOM_NATIVE_ADS, value)
        }

    var displayCustomInterstitialAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_CUSTOM_INTERSTITIAL_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_CUSTOM_INTERSTITIAL_ADS, value)
        }

    var customAdList: List<CustomAd>?
        get() {
            try {
                val userImageArrayList: List<CustomAd>? = if (adPrefUtils.getString(AdsConstants.CUSTOM_ADS) == null) {
                    null
                } else {
                    adPrefUtils.getString(AdsConstants.CUSTOM_ADS)!!.fromJson()
                }
                return userImageArrayList
            } catch (e: Exception) {
                return null
            }
        }
        set(value) {
            adPrefUtils.setString(AdsConstants.CUSTOM_ADS, gsonString(value))
        }

    var displayHomeCardsAds: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.DISPLAY_HOME_CARDS_ADS, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.DISPLAY_HOME_CARDS_ADS, value)
        }

    var swipeCardsAdInterval: Int = 5 // if 0 or less than 0 no ads will display
        get() {
            return adPrefUtils.getInt(AdsConstants.SWIPE_CARD_AD_INTERVAL, 5)
        }
        set(value) {
            field = value
            adPrefUtils.setInt(AdsConstants.SWIPE_CARD_AD_INTERVAL, value)
        }

    var isGroupIntroSmallNative: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_INTRO_SMALL_NATIVE, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_INTRO_SMALL_NATIVE, value)
        }

    var isGroupIntroInterstitial: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_INTRO_INTER, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_INTRO_INTER, value)
        }

    var isGroupMainBanner: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_MAIN_BANNER, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_MAIN_BANNER, value)
        }

    var isGroupMainBigNative: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_MAIN_BIG_NATIVE, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_MAIN_BIG_NATIVE, value)
        }

    var isGroupDetailInterstitial: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_DETAIL_INTER, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_DETAIL_INTER, value)
        }

    var isGroupDetailSmallNative: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_DETAIL_SMALL_NATIVE, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_DETAIL_SMALL_NATIVE, value)
        }

    var isGroupFilterInterstitial: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_FILTER_INTER, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_FILTER_INTER, value)
        }

    var isGroupFilterSmallNative: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_FILTER_SMALL_NATIVE, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_FILTER_SMALL_NATIVE, value)
        }

    var isGroupFilterBigNative: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_FILTER_BIG_NATIVE, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_FILTER_BIG_NATIVE, value)
        }

    var isGroupProfileSmallNative: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_PROFILE_SMALL_NATIVE, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_PROFILE_SMALL_NATIVE, value)
        }

    var isGroupDiamondSmallNative: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_DIAMOND_SMALL_NATIVE, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_DIAMOND_SMALL_NATIVE, value)
        }

    var isGroupDiamondBanner: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_DIAMOND_BANNER, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_DIAMOND_BANNER, value)
        }

    var isGroupDiamondInterstitial: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_DIAMOND_INTER, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_DIAMOND_INTER, value)
        }

    var isGroupMyBalanceBanner: Boolean = false
        get() {
            return adPrefUtils.getBoolean(AdsConstants.IS_GROUP_MY_BALANCE_BANNER, false)
        }
        set(value) {
            field = value
            adPrefUtils.setBoolean(AdsConstants.IS_GROUP_MY_BALANCE_BANNER, value)
        }

    fun setAdsSettings(rootJson: JsonObject) {
        try {
            if (!rootJson.has("data")) {
                return
            }
            val dataJson = rootJson.getAsJsonObject("data")

            if (dataJson.has("customAds")) {
                val customJson = dataJson.getAsJsonArray("customAds")
                customAdList = if (customJson.isEmpty) null else gsonString(customJson).fromJson()
            }

            if (!dataJson.has("adsSetting")) {
                return
            }
            val adsSettingJson = dataJson.getAsJsonObject("adsSetting")

            if (adsSettingJson.has(AdsConstants.ADMOB_BANNER_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.ADMOB_BANNER_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("ca-app-pub-3940256099942544/9214589741")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                admobBannerIds = list
            }
            if (adsSettingJson.has(AdsConstants.ADMOB_NATIVE_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.ADMOB_NATIVE_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("ca-app-pub-3940256099942544/2247696110")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                admobNativeIds = list
            }
            if (adsSettingJson.has(AdsConstants.ADMOB_INTERSTITIAL_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.ADMOB_INTERSTITIAL_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("ca-app-pub-3940256099942544/9214589741")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                admobInterstitialIds = list
            }
            if (adsSettingJson.has(AdsConstants.ADMOB_REWARDED_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.ADMOB_REWARDED_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("ca-app-pub-3940256099942544/5224354917")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                admobRewardedIds = list
            }
            if (adsSettingJson.has(AdsConstants.ADMOB_APP_OPEN_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.ADMOB_APP_OPEN_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("ca-app-pub-3940256099942544/9257395921")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                admobAppOpenIds = list
            }
            if (adsSettingJson.has(AdsConstants.FACEBOOK_BANNER_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.FACEBOOK_BANNER_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                facebookBannerIds = list
            }
            if (adsSettingJson.has(AdsConstants.FACEBOOK_NATIVE_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.FACEBOOK_NATIVE_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("YOUR_PLACEMENT_ID")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                facebookNativeIds = list
            }
            if (adsSettingJson.has(AdsConstants.FACEBOOK_NATIVE_BANNER_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.FACEBOOK_NATIVE_BANNER_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("YOUR_PLACEMENT_ID")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                facebookNativeBannerIds = list
            }
            if (adsSettingJson.has(AdsConstants.FACEBOOK_INTERSTITIAL_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.FACEBOOK_INTERSTITIAL_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("YOUR_PLACEMENT_ID")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                facebookInterstitialIds = list
            }
            if (adsSettingJson.has(AdsConstants.FACEBOOK_REWARDED_ID)) {
                val ids = adsSettingJson.getAsJsonPrimitive(AdsConstants.FACEBOOK_REWARDED_ID).asString

                val list = ArrayList<String>()
                if (ids.isNullOrEmpty()) {
                    list.add("YOUR_PLACEMENT_ID")
                } else if (ids.contains(",")) {
                    val split = ids.split(",")
                    list.addAll(split)
                } else {
                    list.add(ids)
                }
                facebookRewardedIds = list
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_ADS)) {
                displayAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_BANNER_ADS)) {
                displayBannerAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_BANNER_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_INTERSTITIAL_ADS)) {
                displayInterstitialAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_INTERSTITIAL_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_NATIVE_ADS)) {
                displayNativeAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_NATIVE_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_REWARDED_ADS)) {
                displayRewardedAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_REWARDED_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_ALTERNATIVE)) {
                isAlternative = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_ALTERNATIVE).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.INTERSTITIAL_AD_INTERVAL)) {
                interstitialAdInterval = adsSettingJson.getAsJsonPrimitive(AdsConstants.INTERSTITIAL_AD_INTERVAL).asInt
            }
            if (adsSettingJson.has(AdsConstants.AD_PRIORITY)) {
                val priorities = adsSettingJson.getAsJsonArray(AdsConstants.AD_PRIORITY)
                adPriorityList = priorities.toList().joinToString(",") { it.asString }
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_ADMOB_ADS)) {
                displayAdmobAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_ADMOB_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_ADMOB_BANNER_ADS)) {
                displayAdmobBannerAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_ADMOB_BANNER_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_ADMOB_NATIVE_ADS)) {
                displayAdmobNativeAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_ADMOB_NATIVE_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_ADMOB_INTERSTITIAL_ADS)) {
                displayAdmobInterstitialAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_ADMOB_INTERSTITIAL_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_ADMOB_REWARDED_ADS)) {
                displayAdmobRewardedAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_ADMOB_REWARDED_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_ADMOB_APP_OPEN_ADS)) {
                displayAdmobAppOpenAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_ADMOB_APP_OPEN_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_FACEBOOK_ADS)) {
                displayFacebookAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_FACEBOOK_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_FACEBOOK_BANNER_ADS)) {
                displayFacebookBannerAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_FACEBOOK_BANNER_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_FACEBOOK_NATIVE_ADS)) {
                displayFacebookNativeAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_FACEBOOK_NATIVE_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_FACEBOOK_INTERSTITIAL_ADS)) {
                displayFacebookInterstitialAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_FACEBOOK_INTERSTITIAL_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_FACEBOOK_REWARDED_ADS)) {
                displayFacebookRewardedAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_FACEBOOK_REWARDED_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_CUSTOM_ADS)) {
                displayCustomAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_CUSTOM_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_CUSTOM_BANNER_ADS)) {
                displayCustomBannerAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_CUSTOM_BANNER_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_CUSTOM_NATIVE_ADS)) {
                displayCustomNativeAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_CUSTOM_NATIVE_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_CUSTOM_INTERSTITIAL_ADS)) {
                displayCustomInterstitialAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_CUSTOM_INTERSTITIAL_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.DISPLAY_HOME_CARDS_ADS)) {
                displayHomeCardsAds = adsSettingJson.getAsJsonPrimitive(AdsConstants.DISPLAY_HOME_CARDS_ADS).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.SWIPE_CARD_AD_INTERVAL)) {
                swipeCardsAdInterval = adsSettingJson.getAsJsonPrimitive(AdsConstants.SWIPE_CARD_AD_INTERVAL).asInt
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_INTRO_SMALL_NATIVE)) {
                isGroupIntroSmallNative = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_INTRO_SMALL_NATIVE).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_INTRO_INTER)) {
                isGroupIntroInterstitial = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_INTRO_INTER).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_MAIN_BANNER)) {
                isGroupMainBanner = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_MAIN_BANNER).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_MAIN_BIG_NATIVE)) {
                isGroupMainBigNative = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_MAIN_BIG_NATIVE).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_DETAIL_INTER)) {
                isGroupDetailInterstitial = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_DETAIL_INTER).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_DETAIL_SMALL_NATIVE)) {
                isGroupDetailSmallNative = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_DETAIL_SMALL_NATIVE).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_FILTER_INTER)) {
                isGroupFilterInterstitial = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_FILTER_INTER).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_FILTER_SMALL_NATIVE)) {
                isGroupFilterSmallNative = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_FILTER_SMALL_NATIVE).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_FILTER_BIG_NATIVE)) {
                isGroupFilterBigNative = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_FILTER_BIG_NATIVE).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_PROFILE_SMALL_NATIVE)) {
                isGroupProfileSmallNative = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_PROFILE_SMALL_NATIVE).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_DIAMOND_SMALL_NATIVE)) {
                isGroupDiamondSmallNative = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_DIAMOND_SMALL_NATIVE).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_DIAMOND_BANNER)) {
                isGroupDiamondBanner = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_DIAMOND_BANNER).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_DIAMOND_INTER)) {
                isGroupDiamondInterstitial = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_DIAMOND_INTER).asBoolean
            }
            if (adsSettingJson.has(AdsConstants.IS_GROUP_MY_BALANCE_BANNER)) {
                isGroupMyBalanceBanner = adsSettingJson.getAsJsonPrimitive(AdsConstants.IS_GROUP_MY_BALANCE_BANNER).asBoolean
            }
        } catch (e: Exception) {
            catchLog("getAdsSettings: ${gsonString(e)}")
        }
    }
}
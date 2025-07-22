package com.belive.dating.constants

enum class AdPriority(val value: String) {
    GOOGLE("Google"),
    FACEBOOK("Facebook"),
    CUSTOM("Custom"),
}

object AdsConstants {
    const val PREF_NAME = "ads_prefs"

    // admob ad ids
    const val ADMOB_BANNER_ID = "google_banner_id"
    const val ADMOB_NATIVE_ID = "google_native_id"
    const val ADMOB_INTERSTITIAL_ID = "google_inter_id"
    const val ADMOB_REWARDED_ID = "google_rewarded_id"
    const val ADMOB_APP_OPEN_ID = "google_app_open_id"

    // facebook ad ids
    const val FACEBOOK_BANNER_ID = "facebook_banner_id"
    const val FACEBOOK_NATIVE_ID = "facebook_native_id"
    const val FACEBOOK_NATIVE_BANNER_ID = "facebook_native_banner_id"
    const val FACEBOOK_INTERSTITIAL_ID = "facebook_inter_id"
    const val FACEBOOK_REWARDED_ID = "facebook_rewarded_id"

    // custom ads
    const val CUSTOM_ADS = "customAds"

    const val DISPLAY_ADS = "is_all_ads_on"

    const val DISPLAY_BANNER_ADS = "is_all_banner_on"
    const val DISPLAY_INTERSTITIAL_ADS = "is_all_inter_on"
    const val DISPLAY_NATIVE_ADS = "is_all_native_on"
    const val DISPLAY_REWARDED_ADS = "is_all_rewarded_on"

    const val IS_ALTERNATIVE = "is_alternative"
    const val INTERSTITIAL_AD_INTERVAL = "interstitial_ad_interval"
    const val AD_PRIORITY = "ad_priority"

    // display admob ads
    const val DISPLAY_ADMOB_ADS = "is_google_ads_on"
    const val DISPLAY_ADMOB_BANNER_ADS = "is_google_banner_on"
    const val DISPLAY_ADMOB_INTERSTITIAL_ADS = "is_google_inter_on"
    const val DISPLAY_ADMOB_NATIVE_ADS = "is_google_native_on"
    const val DISPLAY_ADMOB_REWARDED_ADS = "is_google_rewarded_on"
    const val DISPLAY_ADMOB_APP_OPEN_ADS = "is_google_app_open_on"

    // display facebook ads
    const val DISPLAY_FACEBOOK_ADS = "is_facebook_ads_on"
    const val DISPLAY_FACEBOOK_BANNER_ADS = "is_facebook_banner_on"
    const val DISPLAY_FACEBOOK_INTERSTITIAL_ADS = "is_facebook_inter_on"
    const val DISPLAY_FACEBOOK_NATIVE_ADS = "is_facebook_native_on"
    const val DISPLAY_FACEBOOK_REWARDED_ADS = "is_facebook_rewarded_on"

    // display custom ads
    const val DISPLAY_CUSTOM_ADS = "is_custom_ads_on"
    const val DISPLAY_CUSTOM_BANNER_ADS = "is_custom_banner_on"
    const val DISPLAY_CUSTOM_INTERSTITIAL_ADS = "is_custom_inter_on"
    const val DISPLAY_CUSTOM_NATIVE_ADS = "is_custom_native_on"

    // home cards ad
    const val DISPLAY_HOME_CARDS_ADS = "is_display_home_card_ads"
    const val SWIPE_CARD_AD_INTERVAL = "swipe_card_ad_interval"

    // group ads
    const val IS_GROUP_INTRO_SMALL_NATIVE = "is_group_intro_small_native"
    const val IS_GROUP_INTRO_INTER = "is_group_intro_inter"
    const val IS_GROUP_MAIN_BANNER = "is_group_main_banner"
    const val IS_GROUP_MAIN_BIG_NATIVE = "is_group_main_big_native"
    const val IS_GROUP_DETAIL_INTER = "is_group_detail_inter"
    const val IS_GROUP_DETAIL_SMALL_NATIVE = "is_group_detail_small_native"
    const val IS_GROUP_FILTER_INTER = "is_group_filter_inter"
    const val IS_GROUP_FILTER_SMALL_NATIVE = "is_group_filter_small_native"
    const val IS_GROUP_FILTER_BIG_NATIVE = "is_group_filter_big_native"
    const val IS_GROUP_PROFILE_SMALL_NATIVE = "is_group_profile_small_native"
    const val IS_GROUP_DIAMOND_SMALL_NATIVE = "is_group_diamond_small_native"
    const val IS_GROUP_DIAMOND_BANNER = "is_group_diamond_banner"
    const val IS_GROUP_DIAMOND_INTER = "is_group_diamond_inter"
    const val IS_GROUP_MY_BALANCE_BANNER = "is_group_my_balance_banner"
}
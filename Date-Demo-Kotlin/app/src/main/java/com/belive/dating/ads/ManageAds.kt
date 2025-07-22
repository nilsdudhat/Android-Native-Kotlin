package com.belive.dating.ads

import android.widget.FrameLayout
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.ads.custom.CustomAds
import com.belive.dating.ads.facebook.FacebookAds
import com.belive.dating.constants.AdPriority
import com.belive.dating.extensions.getAdsPrefs
import com.belive.dating.extensions.gone

enum class SmallNativeGroup(val value: String) {
    Introduction("introduction"),
    Detail("detail"),
    Filter("filter"),
    Profile("profile"),
    Diamond("diamond"),
}

enum class BigNativeGroup(val value: String) {
    Main("main"),
    Filter("filter"),
}

enum class InterstitialGroup(val value: String) {
    Intro("introduction"),
    Detail("detail"),
    Filter("filter"),
    Diamond("diamond"),
}

enum class BannerGroup(val value: String) {
    Main("main"),
    Diamond("diamond"),
    MyBalance("my_balance"),
}

object ManageAds {

    var adPriorityList: ArrayList<AdPriority>? = null

    var bannerAdPriority: AdPriority? = null
    var nativeAdPriority: AdPriority? = null
    var interstitialAdPriority: AdPriority? = null
    var rewardedAdPriority: AdPriority? = null

    var currentInterstitialAdIndex = -1
    var isFullScreenAdShown = false

    var startLoadingAds: Boolean = false

    var isAdPriorityUpdatedOnce = false

    fun loadAds() {
        if (!getAdsPrefs().displayAds) {
            return
        }

        if (!startLoadingAds) {
            return
        }

        if (!isAdPriorityUpdatedOnce) {
            isAdPriorityUpdatedOnce = true
            forceUpdatePriorities()
        }

        AdmobAds.loadAds()
        FacebookAds.loadAds()
        CustomAds.loadAds()
    }

    fun forceUpdatePriorities() {
        adPriorityList = ArrayList()

        val list = getAdsPrefs().adPriorityList?.split(",")

        list?.forEachIndexed { index, value ->
            when (value) {
                AdPriority.GOOGLE.value -> adPriorityList?.add(index, AdPriority.GOOGLE)
                AdPriority.FACEBOOK.value -> adPriorityList?.add(index, AdPriority.FACEBOOK)
                AdPriority.CUSTOM.value -> adPriorityList?.add(index, AdPriority.CUSTOM)
            }
        }

        var priorityIndex = 0

        adPriorityList?.forEachIndexed { index, value ->
            if ((value == AdPriority.GOOGLE) && getAdsPrefs().displayAdmobAds) {
                priorityIndex = index
                return@forEachIndexed
            } else if ((value == AdPriority.FACEBOOK) && getAdsPrefs().displayFacebookAds) {
                priorityIndex = index
                return@forEachIndexed
            } else if ((value == AdPriority.CUSTOM) && getAdsPrefs().displayCustomAds) {
                priorityIndex = index
                return@forEachIndexed
            }
        }

        bannerAdPriority = adPriorityList?.get(priorityIndex)
        nativeAdPriority = adPriorityList?.get(priorityIndex)
        interstitialAdPriority = adPriorityList?.get(priorityIndex)
        rewardedAdPriority = adPriorityList?.get(priorityIndex)
    }

    fun changeBannerAdPriority() {
        if (!getAdsPrefs().displayAds) {
            return
        }
        if (!getAdsPrefs().isAlternative) {
            return
        }
        if (adPriorityList.isNullOrEmpty() || (bannerAdPriority == null)) {
            return
        }

        val indexOfPriority = adPriorityList!!.indexOf(bannerAdPriority)
        bannerAdPriority = if (indexOfPriority < (adPriorityList!!.size - 1)) {
            adPriorityList!![indexOfPriority + 1]
        } else {
            adPriorityList!![0]
        }

        if (bannerAdPriority == AdPriority.GOOGLE) {
            if (!getAdsPrefs().displayAdmobAds || !getAdsPrefs().displayAdmobBannerAds) {
                changeBannerAdPriority()
                return
            }
        }

        if (bannerAdPriority == AdPriority.FACEBOOK) {
            if (!getAdsPrefs().displayFacebookAds || !getAdsPrefs().displayFacebookBannerAds) {
                changeBannerAdPriority()
                return
            }
        }

        if (bannerAdPriority == AdPriority.CUSTOM) {
            if (!getAdsPrefs().displayCustomAds || !getAdsPrefs().displayCustomBannerAds) {
                changeBannerAdPriority()
                return
            }
        }
    }

    fun changeNativeAdPriority() {
        if (!getAdsPrefs().displayAds) {
            return
        }
        if (!getAdsPrefs().isAlternative) {
            return
        }
        if (adPriorityList.isNullOrEmpty() || (nativeAdPriority == null)) {
            return
        }

        val indexOfPriority = adPriorityList!!.indexOf(nativeAdPriority)
        nativeAdPriority = if (indexOfPriority < (adPriorityList!!.size - 1)) {
            adPriorityList!![indexOfPriority + 1]
        } else {
            adPriorityList!![0]
        }

        if (nativeAdPriority == AdPriority.GOOGLE) {
            if (!getAdsPrefs().displayAdmobAds || !getAdsPrefs().displayAdmobNativeAds) {
                changeNativeAdPriority()
                return
            }
        }

        if (nativeAdPriority == AdPriority.FACEBOOK) {
            if (!getAdsPrefs().displayFacebookAds || !getAdsPrefs().displayFacebookNativeAds) {
                changeNativeAdPriority()
                return
            }
        }

        if (nativeAdPriority == AdPriority.CUSTOM) {
            if (!getAdsPrefs().displayCustomAds || !getAdsPrefs().displayCustomNativeAds) {
                changeNativeAdPriority()
                return
            }
        }
    }

    fun changeInterstitialAdPriority() {
        if (!getAdsPrefs().displayAds) {
            return
        }
        if (!getAdsPrefs().isAlternative) {
            return
        }
        if (adPriorityList.isNullOrEmpty() || (interstitialAdPriority == null)) {
            return
        }

        val indexOfPriority = adPriorityList!!.indexOf(interstitialAdPriority)
        interstitialAdPriority = if (indexOfPriority < (adPriorityList!!.size - 1)) {
            adPriorityList!![indexOfPriority + 1]
        } else {
            adPriorityList!![0]
        }

        if (interstitialAdPriority == AdPriority.GOOGLE) {
            if (!getAdsPrefs().displayAdmobAds || !getAdsPrefs().displayAdmobInterstitialAds) {
                changeInterstitialAdPriority()
                return
            }
        }

        if (interstitialAdPriority == AdPriority.FACEBOOK) {
            if (!getAdsPrefs().displayFacebookAds || !getAdsPrefs().displayFacebookInterstitialAds) {
                changeInterstitialAdPriority()
                return
            }
        }

        if (interstitialAdPriority == AdPriority.CUSTOM) {
            if (!getAdsPrefs().displayCustomAds || !getAdsPrefs().displayCustomInterstitialAds) {
                changeInterstitialAdPriority()
                return
            }
        }
    }

    fun isHomeCardAdsEnabled(): Boolean {
        if (!getAdsPrefs().displayAds) {
            return false
        }
        if (!getAdsPrefs().displayNativeAds) {
            return false
        }
        if (!getAdsPrefs().displayAdmobAds && !getAdsPrefs().displayFacebookAds && !getAdsPrefs().displayCustomAds) {
            return false
        }
        if (!getAdsPrefs().displayHomeCardsAds) {
            return false
        }
        if (!getAdsPrefs().isGroupMainBigNative) {
            return false
        }
        if (getAdsPrefs().swipeCardsAdInterval < 1) {
            return false
        }
        return true
    }

    fun isBannerAdsEnabled(): Boolean {
        if (!getAdsPrefs().displayAds) {
            return false
        }
        if (!getAdsPrefs().displayBannerAds) {
            return false
        }
        if (!getAdsPrefs().displayAdmobAds && !getAdsPrefs().displayFacebookAds && !getAdsPrefs().displayCustomAds) {
            return false
        }
        return true
    }

    fun showNativeFullScreenAd(groupScreen: BigNativeGroup, frameLayout: FrameLayout) {
        if (!getAdsPrefs().displayAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayAdmobAds && !getAdsPrefs().displayFacebookAds && !getAdsPrefs().displayCustomAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayNativeAds) {
            frameLayout.gone()
            return
        }

        if ((AdmobAds.nativeAd == null) && (FacebookAds.nativeAd == null) && (CustomAds.customAd == null)) {
            frameLayout.gone()
            return
        }

        if (groupScreen == BigNativeGroup.Main) {
            if (!getAdsPrefs().isGroupMainBigNative) {
                frameLayout.gone()
                return
            }
        } else if (groupScreen == BigNativeGroup.Filter) {
            if (!getAdsPrefs().isGroupFilterBigNative) {
                frameLayout.gone()
                return
            }
        }

        if (nativeAdPriority?.name == AdPriority.GOOGLE.name) {
            if (AdmobAds.nativeAd != null) {
                AdmobAds.showNativeFullScreenAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showNativeFullScreenAd(groupScreen, frameLayout)
            }
        } else if (nativeAdPriority?.name == AdPriority.FACEBOOK.name) {
            if (FacebookAds.nativeAd != null) {
                FacebookAds.showNativeFullScreenAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showNativeFullScreenAd(groupScreen, frameLayout)
            }
        } else if (nativeAdPriority?.name == AdPriority.CUSTOM.name) {
            if (CustomAds.customAd != null) {
                CustomAds.showNativeFullScreenAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showNativeFullScreenAd(groupScreen, frameLayout)
            }
        } else {
            frameLayout.gone()
        }
    }

    fun showNativeSquareAd(groupScreen: BigNativeGroup, frameLayout: FrameLayout) {
        if (!getAdsPrefs().displayAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayAdmobAds && !getAdsPrefs().displayFacebookAds && !getAdsPrefs().displayCustomAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayNativeAds) {
            frameLayout.gone()
            return
        }

        if ((AdmobAds.nativeAd == null) && (FacebookAds.nativeAd == null) && (CustomAds.customAd == null)) {
            frameLayout.gone()
            return
        }

        if (groupScreen == BigNativeGroup.Main) {
            if (!getAdsPrefs().isGroupMainBigNative) {
                frameLayout.gone()
                return
            }
        } else if (groupScreen == BigNativeGroup.Filter) {
            if (!getAdsPrefs().isGroupFilterBigNative) {
                frameLayout.gone()
                return
            }
        }

        if (nativeAdPriority?.name == AdPriority.GOOGLE.name) {
            if (AdmobAds.nativeAd != null) {
                AdmobAds.showNativeSquareAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showNativeSquareAd(groupScreen, frameLayout)
            }
        } else if (nativeAdPriority?.name == AdPriority.FACEBOOK.name) {
            if (FacebookAds.nativeAd != null) {
                FacebookAds.showNativeSquareAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showNativeSquareAd(groupScreen, frameLayout)
            }
        } else if (nativeAdPriority?.name == AdPriority.CUSTOM.name) {
            if (CustomAds.customAd != null) {
                CustomAds.showNativeSquareAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showNativeSquareAd(groupScreen, frameLayout)
            }
        } else {
            frameLayout.gone()
        }
    }

    fun showNativeInGridAd(groupScreen: BigNativeGroup, frameLayout: FrameLayout) {
        if (!getAdsPrefs().displayAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayAdmobAds && !getAdsPrefs().displayFacebookAds && !getAdsPrefs().displayCustomAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayNativeAds) {
            frameLayout.gone()
            return
        }

        if ((AdmobAds.nativeAd == null) && (FacebookAds.nativeAd == null) && (CustomAds.customAd == null)) {
            frameLayout.gone()
            return
        }

        if (groupScreen == BigNativeGroup.Main) {
            if (!getAdsPrefs().isGroupMainBigNative) {
                frameLayout.gone()
                return
            }
        } else if (groupScreen == BigNativeGroup.Filter) {
            if (!getAdsPrefs().isGroupFilterBigNative) {
                frameLayout.gone()
                return
            }
        }

        if (nativeAdPriority?.name == AdPriority.GOOGLE.name) {
            if (AdmobAds.nativeAd != null) {
                AdmobAds.showNativeInGridAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showNativeInGridAd(groupScreen, frameLayout)
            }
        } else if (nativeAdPriority?.name == AdPriority.FACEBOOK.name) {
            if (FacebookAds.nativeAd != null) {
                FacebookAds.showNativeInGridAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showNativeInGridAd(groupScreen, frameLayout)
            }
        } else if (nativeAdPriority?.name == AdPriority.CUSTOM.name) {
            if (CustomAds.customAd != null) {
                CustomAds.showNativeInGridAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showNativeInGridAd(groupScreen, frameLayout)
            }
        } else {
            frameLayout.gone()
        }
    }

    fun showSmallNativeAd(groupScreen: SmallNativeGroup, frameLayout: FrameLayout) {
        if (!getAdsPrefs().displayAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayAdmobAds && !getAdsPrefs().displayFacebookAds && !getAdsPrefs().displayCustomAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayNativeAds) {
            frameLayout.gone()
            return
        }

        if ((AdmobAds.nativeAd == null) && (FacebookAds.smallNativeAd == null) && (CustomAds.customAd == null)) {
            frameLayout.gone()
            return
        }

        if (groupScreen == SmallNativeGroup.Introduction) {
            if (!getAdsPrefs().isGroupIntroSmallNative) {
                frameLayout.gone()
                return
            }
        } else if (groupScreen == SmallNativeGroup.Detail) {
            if (!getAdsPrefs().isGroupDetailSmallNative) {
                frameLayout.gone()
                return
            }
        } else if (groupScreen == SmallNativeGroup.Filter) {
            if (!getAdsPrefs().isGroupFilterSmallNative) {
                frameLayout.gone()
                return
            }
        } else if (groupScreen == SmallNativeGroup.Profile) {
            if (!getAdsPrefs().isGroupProfileSmallNative) {
                frameLayout.gone()
                return
            }
        } else if (groupScreen == SmallNativeGroup.Diamond) {
            if (!getAdsPrefs().isGroupDiamondSmallNative) {
                frameLayout.gone()
                return
            }
        }

        if (nativeAdPriority?.name == AdPriority.GOOGLE.name) {
            if (AdmobAds.nativeAd != null) {
                AdmobAds.showSmallNativeAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showSmallNativeAd(groupScreen, frameLayout)
            }
        } else if (nativeAdPriority?.name == AdPriority.FACEBOOK.name) {
            if (FacebookAds.smallNativeAd != null) {
                FacebookAds.showSmallNativeAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showSmallNativeAd(groupScreen, frameLayout)
            }
        } else if (nativeAdPriority?.name == AdPriority.CUSTOM.name) {
            if (CustomAds.customAd != null) {
                CustomAds.showSmallNativeAd(frameLayout)
            } else {
                changeNativeAdPriority()
                showSmallNativeAd(groupScreen, frameLayout)
            }
        } else {
            frameLayout.gone()
        }
    }

    fun showBannerAd(groupScreen: BannerGroup, frameLayout: FrameLayout) {
        if (!getAdsPrefs().displayAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayAdmobAds && !getAdsPrefs().displayFacebookAds && !getAdsPrefs().displayCustomAds) {
            frameLayout.gone()
            return
        }

        if (!getAdsPrefs().displayBannerAds) {
            frameLayout.gone()
            return
        }

        if ((AdmobAds.bannerAd == null) && (FacebookAds.bannerAd == null) && (CustomAds.customAd == null)) {
            frameLayout.gone()
            return
        }

        if (groupScreen == BannerGroup.Main) {
            if (!getAdsPrefs().isGroupMainBanner) {
                frameLayout.gone()
                return
            }
        } else if (groupScreen == BannerGroup.Diamond) {
            if (!getAdsPrefs().isGroupDiamondBanner) {
                frameLayout.gone()
                return
            }
        } else if (groupScreen == BannerGroup.MyBalance) {
            if (!getAdsPrefs().isGroupMyBalanceBanner) {
                frameLayout.gone()
                return
            }
        }

        if (bannerAdPriority?.name == AdPriority.GOOGLE.name) {
            if (AdmobAds.bannerAd != null) {
                AdmobAds.showBannerAd(frameLayout)
            } else {
                changeBannerAdPriority()
                showBannerAd(groupScreen, frameLayout)
            }
        } else if (bannerAdPriority?.name == AdPriority.FACEBOOK.name) {
            if (FacebookAds.bannerAd != null) {
                FacebookAds.showBannerAd(frameLayout)
            } else {
                changeBannerAdPriority()
                showBannerAd(groupScreen, frameLayout)
            }
        } else if (bannerAdPriority?.name == AdPriority.CUSTOM.name) {
            if (CustomAds.customAd != null) {
                CustomAds.showBannerAd(frameLayout)
            } else {
                changeBannerAdPriority()
                showBannerAd(groupScreen, frameLayout)
            }
        } else {
            frameLayout.gone()
        }
    }

    fun showInterstitialAd(groupScreen: InterstitialGroup, isRetry: Boolean = false, onDismiss: () -> Unit) {
        if (!getAdsPrefs().displayAds) {
            onDismiss.invoke()
            return
        }

        if (!getAdsPrefs().displayAdmobAds && !getAdsPrefs().displayFacebookAds && !getAdsPrefs().displayCustomAds) {
            onDismiss.invoke()
            return
        }

        if (!getAdsPrefs().displayInterstitialAds) {
            onDismiss.invoke()
            return
        }

        if ((AdmobAds.interstitialAd == null) && (FacebookAds.interstitialAd == null) && (CustomAds.customAd == null)) {
            onDismiss.invoke()
            return
        }

        if (groupScreen == InterstitialGroup.Detail) {
            if (!getAdsPrefs().isGroupDetailInterstitial) {
                onDismiss.invoke()
                return
            }
        } else if (groupScreen == InterstitialGroup.Intro) {
            if (!getAdsPrefs().isGroupIntroInterstitial) {
                onDismiss.invoke()
                return
            }
        } else if (groupScreen == InterstitialGroup.Filter) {
            if (!getAdsPrefs().isGroupFilterInterstitial) {
                onDismiss.invoke()
                return
            }
        } else if (groupScreen == InterstitialGroup.Diamond) {
            if (!getAdsPrefs().isGroupDiamondInterstitial) {
                onDismiss.invoke()
                return
            }
        }

        if (!isRetry) {
            if (currentInterstitialAdIndex == -1) {
                currentInterstitialAdIndex = 0
            }
            currentInterstitialAdIndex++

            if (currentInterstitialAdIndex != getAdsPrefs().interstitialAdInterval) {
                onDismiss.invoke()
                return
            }

            if (currentInterstitialAdIndex >= getAdsPrefs().interstitialAdInterval) {
                currentInterstitialAdIndex = 0
            }
        }

        if (interstitialAdPriority?.name == AdPriority.GOOGLE.name) {
            if (AdmobAds.interstitialAd != null) {
                AdmobAds.showInterstitialAd(onDismiss)
            } else {
                changeInterstitialAdPriority()
                showInterstitialAd(groupScreen, true, onDismiss)
            }
        } else if (interstitialAdPriority?.name == AdPriority.FACEBOOK.name) {
            if (FacebookAds.interstitialAd != null) {
                FacebookAds.showInterstitialAd(onDismiss)
            } else {
                changeInterstitialAdPriority()
                showInterstitialAd(groupScreen, true, onDismiss)
            }
        } else if (interstitialAdPriority?.name == AdPriority.CUSTOM.name) {
            if (CustomAds.customAd != null) {
                CustomAds.showInterstitialAd(onDismiss)
            } else {
                changeInterstitialAdPriority()
                showInterstitialAd(groupScreen, true, onDismiss)
            }
        } else {
            onDismiss.invoke()
        }
    }
}
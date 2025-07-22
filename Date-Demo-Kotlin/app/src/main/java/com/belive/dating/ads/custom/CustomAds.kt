package com.belive.dating.ads.custom

import android.content.Intent
import android.os.Parcelable
import android.widget.FrameLayout
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.AdCustomBannerBinding
import com.belive.dating.databinding.AdCustomNativeFullScreenBinding
import com.belive.dating.databinding.AdCustomNativeGridBinding
import com.belive.dating.databinding.AdCustomNativeSmallBinding
import com.belive.dating.databinding.AdCustomNativeSquareBinding
import com.belive.dating.extensions.getAdsPrefs
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.gone
import com.belive.dating.extensions.noAnimation
import com.belive.dating.extensions.openBrowser
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomAd(
    @SerializedName("logo_url") val logoUrl: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("poster_url") val posterUrl: String,
    @SerializedName("btn_text") val btnText: String,
    @SerializedName("redirect_url") val redirectUrl: String,
    @SerializedName("info_url") val infoUrl: String,
) : Parcelable

object CustomAds {

    var customAd: CustomAd? = null
    var isLoading: Boolean = false

    var currentCustomAdIndex = -1

    var onFullScreenAdDismissed: (() -> Unit)? = null

    fun loadAds() {
        if (!getAdsPrefs().displayCustomAds) {
            return
        }

        if ((customAd != null) || isLoading) {
            return
        }

        if (getAdsPrefs().customAdList.isNullOrEmpty()) {
            return
        }

        synchronized(this) {
            isLoading = true

            if (!getAdsPrefs().displayCustomNativeAds && !getAdsPrefs().displayCustomInterstitialAds) {
                return
            } else {
                if (currentCustomAdIndex == -1) {
                    currentCustomAdIndex = 0
                } else {
                    currentCustomAdIndex++
                    if (currentCustomAdIndex == getAdsPrefs().customAdList?.size) {
                        currentCustomAdIndex = 0
                    }
                }
            }

            isLoading = false

            customAd = getAdsPrefs().customAdList?.get(currentCustomAdIndex)
        }
    }

    fun showNativeFullScreenAd(frameLayout: FrameLayout) {
        if ((customAd == null) ||
            customAd?.redirectUrl.isNullOrEmpty() ||
            customAd?.infoUrl.isNullOrEmpty() ||
            customAd?.btnText.isNullOrEmpty() ||
            customAd?.body.isNullOrEmpty() ||
            customAd?.title.isNullOrEmpty() ||
            customAd?.posterUrl.isNullOrEmpty()
        ) {
            frameLayout.gone()
            loadAds()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayCustomNativeAds) {
            frameLayout.gone()
            return
        }

        val customNative = customAd!!

        val binding = AdCustomNativeFullScreenBinding.inflate(getKoinActivity().layoutInflater)

        binding.logoUrl = getGistPrefs().customImagesURL + customNative.logoUrl
        binding.title = customNative.title
        binding.body = customNative.body
        binding.posterUrl = getGistPrefs().customImagesURL + customNative.posterUrl
        binding.buttonText = customNative.btnText

        binding.btnOpen.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }
        binding.btnInfo.setOnClickListener {
            getKoinActivity().openBrowser(customNative.infoUrl)
        }
        binding.root.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }

        frameLayout.removeAllViews()
        frameLayout.addView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            ManageAds.changeNativeAdPriority()

            customAd = null
            loadAds()
        }
    }

    fun showNativeSquareAd(frameLayout: FrameLayout) {
        if ((customAd == null) ||
            customAd?.redirectUrl.isNullOrEmpty() ||
            customAd?.infoUrl.isNullOrEmpty() ||
            customAd?.btnText.isNullOrEmpty() ||
            customAd?.body.isNullOrEmpty() ||
            customAd?.title.isNullOrEmpty() ||
            customAd?.posterUrl.isNullOrEmpty()
        ) {
            frameLayout.gone()
            loadAds()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayCustomNativeAds) {
            frameLayout.gone()
            return
        }

        val customNative = customAd!!

        val binding = AdCustomNativeSquareBinding.inflate(getKoinActivity().layoutInflater)

        binding.logoUrl = getGistPrefs().customImagesURL + customNative.logoUrl
        binding.title = customNative.title
        binding.body = customNative.body
        binding.posterUrl = getGistPrefs().customImagesURL + customNative.posterUrl
        binding.buttonText = customNative.btnText

        binding.btnOpen.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }
        binding.btnInfo.setOnClickListener {
            getKoinActivity().openBrowser(customNative.infoUrl)
        }
        binding.root.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }

        frameLayout.removeAllViews()
        frameLayout.addView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            ManageAds.changeNativeAdPriority()

            customAd = null
            loadAds()
        }
    }

    fun showNativeInGridAd(frameLayout: FrameLayout) {
        if ((customAd == null) ||
            customAd?.redirectUrl.isNullOrEmpty() ||
            customAd?.infoUrl.isNullOrEmpty() ||
            customAd?.btnText.isNullOrEmpty() ||
            customAd?.body.isNullOrEmpty() ||
            customAd?.title.isNullOrEmpty() ||
            customAd?.posterUrl.isNullOrEmpty()
        ) {
            frameLayout.gone()
            loadAds()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayCustomNativeAds) {
            frameLayout.gone()
            return
        }

        val customNative = customAd!!

        val binding = AdCustomNativeGridBinding.inflate(getKoinActivity().layoutInflater)

        binding.logoUrl = getGistPrefs().customImagesURL + customNative.logoUrl
        binding.title = customNative.title
        binding.body = customNative.body
        binding.posterUrl = getGistPrefs().customImagesURL + customNative.posterUrl
        binding.buttonText = customNative.btnText

        binding.btnOpen.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }
        binding.btnInfo.setOnClickListener {
            getKoinActivity().openBrowser(customNative.infoUrl)
        }
        binding.root.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }

        frameLayout.removeAllViews()
        frameLayout.addView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            ManageAds.changeNativeAdPriority()

            customAd = null
            loadAds()
        }
    }

    fun showSmallNativeAd(frameLayout: FrameLayout) {
        if ((customAd == null) ||
            customAd?.redirectUrl.isNullOrEmpty() ||
            customAd?.infoUrl.isNullOrEmpty() ||
            customAd?.btnText.isNullOrEmpty() ||
            customAd?.body.isNullOrEmpty() ||
            customAd?.title.isNullOrEmpty()
        ) {
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayCustomNativeAds) {
            frameLayout.gone()
            return
        }

        val customNative = customAd!!

        val binding = AdCustomNativeSmallBinding.inflate(getKoinActivity().layoutInflater)

        binding.logoUrl = getGistPrefs().customImagesURL + customNative.logoUrl
        binding.title = customNative.title
        binding.body = customNative.body
        binding.buttonText = customNative.btnText

        binding.btnOpen.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }
        binding.btnInfo.setOnClickListener {
            getKoinActivity().openBrowser(customNative.infoUrl)
        }
        binding.root.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }

        frameLayout.removeAllViews()
        frameLayout.addView(binding.root)

        customAd = null

        CoroutineScope(Dispatchers.IO).launch {
            ManageAds.changeNativeAdPriority()
            loadAds()
        }
    }

    fun showBannerAd(frameLayout: FrameLayout) {
        if ((customAd == null) ||
            customAd?.redirectUrl.isNullOrEmpty() ||
            customAd?.infoUrl.isNullOrEmpty() ||
            customAd?.btnText.isNullOrEmpty() ||
            customAd?.body.isNullOrEmpty() ||
            customAd?.title.isNullOrEmpty()
        ) {
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayCustomNativeAds) {
            frameLayout.gone()
            return
        }

        val customNative = customAd!!

        val binding = AdCustomBannerBinding.inflate(getKoinActivity().layoutInflater)

        binding.logoUrl = getGistPrefs().customImagesURL + customNative.logoUrl
        binding.title = customNative.title
        binding.body = customNative.body
        binding.buttonText = customNative.btnText

        binding.btnOpen.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }
        binding.btnInfo.setOnClickListener {
            getKoinActivity().openBrowser(customNative.infoUrl)
        }
        binding.root.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }

        frameLayout.removeAllViews()
        frameLayout.addView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            ManageAds.changeBannerAdPriority()

            customAd = null
            loadAds()
        }
    }

    fun showInterstitialAd(onDismiss: () -> Unit) {
        if ((customAd == null) ||
            customAd?.redirectUrl.isNullOrEmpty() ||
            customAd?.infoUrl.isNullOrEmpty() ||
            customAd?.btnText.isNullOrEmpty() ||
            customAd?.body.isNullOrEmpty() ||
            customAd?.title.isNullOrEmpty()
        ) {
            onDismiss.invoke()
            return
        }

        if (!getAdsPrefs().displayInterstitialAds || !getAdsPrefs().displayCustomInterstitialAds) {
            onDismiss.invoke()
            return
        }

        onFullScreenAdDismissed = onDismiss

        getKoinActivity().startActivity(Intent(getKoinActivity(), CustomInterstitialAdActivity::class.java))
        getKoinActivity().noAnimation()
    }
}
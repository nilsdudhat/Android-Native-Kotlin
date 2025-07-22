package com.belive.dating.ads.facebook

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.belive.dating.BuildConfig
import com.belive.dating.R
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.extensions.getAdsPrefs
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.gone
import com.belive.dating.extensions.logger
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdOptionsView
import com.facebook.ads.AdSettings
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.facebook.ads.MediaView
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdBase
import com.facebook.ads.NativeAdLayout
import com.facebook.ads.NativeAdListener
import com.facebook.ads.NativeBannerAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FacebookAds {

    var isAdsInitialized = false

    var nativeAd: NativeAd? = null
    var isNativeLoading = false

    var smallNativeAd: NativeBannerAd? = null
    var isSmallNativeLoading = false

    var bannerAd: AdView? = null
    var isBannerLoading = false

    var interstitialAd: InterstitialAd? = null
    var onFullScreenAdDismissed: (() -> Unit)? = null
    var isInterstitialLoading = false

    fun loadAds() {
        if (!getAdsPrefs().displayFacebookAds) {
            return
        }

        if (!isAdsInitialized) {
            AudienceNetworkAds.initialize(getKoinContext().applicationContext)
            if (BuildConfig.DEBUG) {
                AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CRASH_DEBUG_MODE)
                AdSettings.addTestDevice("5355d449-8e99-4628-aabd-f91b85b81b4e")
                AdSettings.setTestMode(true)
            }
            isAdsInitialized = true
        }

        loadBannerAd()
        loadNativeAd()
        loadSmallNativeAd()
        loadInterstitialAd()
    }

    private fun loadBannerAd() {
        if ((bannerAd != null) || isBannerLoading) {
            return
        }

        if (getAdsPrefs().facebookBannerIds.isNullOrEmpty() == true) {
            return
        }

        if (!getAdsPrefs().displayBannerAds) {
            return
        } else if (!getAdsPrefs().displayFacebookBannerAds) {
            return
        }

        val id = getAdsPrefs().facebookBannerIds!![0]
        loadBannerAd(id)
    }

    private fun loadBannerAd(id: String) {
        isBannerLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            synchronized(this) {
                val adView = AdView(getKoinContext(), id, AdSize.BANNER_HEIGHT_50)

                val adListener: AdListener = object : AdListener {
                    override fun onError(ad: Ad?, adError: AdError) {
                        logger("--facebook_banner--", "loadBannerAd onError: ${adError.errorMessage}")

                        bannerAd = null
                        isBannerLoading = false

                        val idIndex = getAdsPrefs().facebookBannerIds!!.indexOf(id)
                        if (idIndex < (getAdsPrefs().facebookBannerIds!!.size - 1)) {
                            loadBannerAd(getAdsPrefs().facebookBannerIds!![idIndex + 1])
                        }
                    }

                    override fun onAdLoaded(ad: Ad?) {
                        logger("--facebook_banner--", "loadBannerAd onAdLoaded")

                        bannerAd = adView
                        isBannerLoading = false
                    }

                    override fun onAdClicked(ad: Ad?) {
                        logger("--facebook_banner--", "loadBannerAd onAdClicked")
                    }

                    override fun onLoggingImpression(ad: Ad?) {
                        logger("--facebook_banner--", "loadBannerAd onLoggingImpression")

                        ManageAds.changeBannerAdPriority()

                        bannerAd = null
                        loadBannerAd()
                    }
                }

                adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build())
            }
        }
    }

    fun showBannerAd(frameLayout: FrameLayout) {
        if (bannerAd == null) {
            loadBannerAd()
            return
        }

        if (!getAdsPrefs().displayBannerAds || !getAdsPrefs().displayFacebookBannerAds) {
            frameLayout.gone()
            return
        }

        frameLayout.removeAllViews()
        frameLayout.addView(bannerAd)
        bannerAd = null
    }

    private fun loadNativeAd() {
        if ((nativeAd != null) || isNativeLoading) {
            return
        }

        if (getAdsPrefs().facebookNativeIds.isNullOrEmpty() == true) {
            return
        }

        if (!getAdsPrefs().displayNativeAds) {
            return
        } else if (!getAdsPrefs().displayFacebookNativeAds) {
            return
        }

        val id = getAdsPrefs().facebookNativeIds!![0]
        loadNativeAd(id)
    }

    private fun loadNativeAd(id: String) {
        isNativeLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            synchronized(this) {
                val nativeAd = NativeAd(getKoinContext(), id)

                val nativeAdListener: NativeAdListener = object : NativeAdListener {
                    override fun onMediaDownloaded(ad: Ad?) {
                        logger("--facebook_native--", "Native ad finished downloading all assets.")
                    }

                    override fun onError(ad: Ad?, adError: AdError) {
                        logger("--facebook_native--", "Native ad failed to load: " + adError.errorMessage)

                        isNativeLoading = false
                        this@FacebookAds.nativeAd = null

                        val idIndex = getAdsPrefs().facebookNativeIds!!.indexOf(id)
                        if (idIndex < (getAdsPrefs().facebookNativeIds!!.size - 1)) {
                            AdmobAds.loadNativeAd(getAdsPrefs().facebookNativeIds!![idIndex + 1])
                        }
                    }

                    override fun onAdLoaded(ad: Ad?) {
                        logger("--facebook_native--", "Native ad is loaded and ready to be displayed!")

                        isNativeLoading = false
                        this@FacebookAds.nativeAd = nativeAd
                    }

                    override fun onAdClicked(ad: Ad?) {
                        logger("--facebook_native--", "Native ad clicked!")
                    }

                    override fun onLoggingImpression(ad: Ad?) {
                        logger("--facebook_native--", "Native ad impression logged!")

                        ManageAds.changeNativeAdPriority()
                        this@FacebookAds.nativeAd = null
                        loadNativeAd()
                    }
                }

                // Request an ad
                nativeAd.loadAd(
                    nativeAd.buildLoadAdConfig()
                        .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                        .withAdListener(nativeAdListener)
                        .build()
                )
            }
        }
    }

    fun showNativeFullScreenAd(frameLayout: FrameLayout) {
        if (nativeAd == null) {
            loadNativeAd()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayFacebookNativeAds) {
            frameLayout.gone()
            return
        }

        val nativeAdLayout = LayoutInflater.from(getKoinActivity()).inflate(R.layout.ad_facebook_native_full_screen, null) as NativeAdLayout
        inflateNativeAd(false, nativeAd!!, nativeAdLayout)
        frameLayout.removeAllViews()
        frameLayout.addView(nativeAdLayout)
    }

    fun showNativeInGridAd(frameLayout: FrameLayout) {
        if (nativeAd == null) {
            loadNativeAd()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayFacebookNativeAds) {
            frameLayout.gone()
            return
        }

        val nativeAdLayout = LayoutInflater.from(getKoinActivity()).inflate(R.layout.ad_facebook_native_grid, null) as NativeAdLayout
        inflateNativeInGridAd(nativeAd!!, nativeAdLayout)
        frameLayout.removeAllViews()
        frameLayout.addView(nativeAdLayout)
    }

    private fun inflateNativeInGridAd(nativeAd: NativeAd, nativeAdLayout: NativeAdLayout) {
        nativeAd.unregisterView()

        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        val adView = nativeAdLayout.findViewById<ConstraintLayout>(R.id.ad_unit)

        nativeAdLayout.post {
            (adView.layoutParams as FrameLayout.LayoutParams).height = ((nativeAdLayout.measuredWidth * 1.40) / 2).toInt()
        }

        // Add the AdOptionsView
        val adChoicesContainer: LinearLayout = adView.findViewById(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(getKoinActivity(), nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.
        val nativeAdIcon: MediaView? = adView.findViewById(R.id.native_ad_icon)
        val nativeAdTitle: TextView = adView.findViewById(R.id.ad_headline)
        val nativeAdMedia: MediaView? = adView.findViewById(R.id.native_ad_media)
        val sponsoredLabel: TextView = adView.findViewById(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction: Button = adView.findViewById(R.id.ad_call_to_action)

        // Set the Text.
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdCallToAction.visibility = if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation

        // Create a list of clickable views
        val clickableViews: MutableList<View?> = ArrayList<View?>()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
            adView, nativeAdMedia, nativeAdIcon, clickableViews
        )
    }

    fun showNativeSquareAd(frameLayout: FrameLayout) {
        if (nativeAd == null) {
            loadNativeAd()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayFacebookNativeAds) {
            frameLayout.gone()
            return
        }

        val nativeAdLayout = LayoutInflater.from(getKoinActivity()).inflate(R.layout.ad_facebook_native_square, null) as NativeAdLayout
        inflateNativeAd(true, nativeAd!!, nativeAdLayout)
        frameLayout.removeAllViews()
        frameLayout.addView(nativeAdLayout)
    }

    private fun inflateNativeAd(isSquare: Boolean, nativeAd: NativeAd, nativeAdLayout: NativeAdLayout) {
        nativeAd.unregisterView()

        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        val adView = nativeAdLayout.findViewById<ConstraintLayout>(R.id.ad_unit)

        if (isSquare) {
            nativeAdLayout.post {
                (adView.layoutParams as FrameLayout.LayoutParams).height = nativeAdLayout.measuredWidth
            }
        }

        // Add the AdOptionsView
        val adChoicesContainer: LinearLayout = adView.findViewById(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(getKoinActivity(), nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.
        val nativeAdIcon: MediaView? = adView.findViewById(R.id.native_ad_icon)
        val nativeAdTitle: TextView = adView.findViewById(R.id.ad_headline)
        val nativeAdMedia: MediaView? = adView.findViewById(R.id.native_ad_media)
        val nativeAdSocialContext: TextView = adView.findViewById(R.id.native_ad_social_context)
        val nativeAdBody: TextView = adView.findViewById(R.id.native_ad_body)
        val sponsoredLabel: TextView = adView.findViewById(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction: Button = adView.findViewById(R.id.ad_call_to_action)

        // Set the Text.
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility = if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation

        // Create a list of clickable views
        val clickableViews: MutableList<View?> = ArrayList<View?>()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
            adView, nativeAdMedia, nativeAdIcon, clickableViews
        )
    }

    private fun loadSmallNativeAd() {
        if ((smallNativeAd != null) || isSmallNativeLoading) {
            return
        }

        if (getAdsPrefs().facebookNativeBannerIds.isNullOrEmpty() == true) {
            return
        }

        if (!getAdsPrefs().displayNativeAds) {
            return
        } else if (!getAdsPrefs().displayFacebookNativeAds) {
            return
        }

        val id = getAdsPrefs().facebookNativeBannerIds!![0]
        loadSmallNativeAd(id)
    }

    private fun loadSmallNativeAd(id: String) {
        isSmallNativeLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            synchronized(this) {
                val nativeAd = NativeBannerAd(getKoinContext(), id)

                val nativeAdListener: NativeAdListener = object : NativeAdListener {
                    override fun onMediaDownloaded(ad: Ad?) {
                        logger("--facebook_small_native--", "Native ad finished downloading all assets.")
                    }

                    override fun onError(ad: Ad?, adError: AdError) {
                        logger("--facebook_small_native--", "Native ad failed to load: " + adError.errorMessage)

                        isSmallNativeLoading = false
                        this@FacebookAds.smallNativeAd = null

                        val idIndex = getAdsPrefs().facebookNativeBannerIds!!.indexOf(id)
                        if (idIndex < (getAdsPrefs().facebookNativeBannerIds!!.size - 1)) {
                            loadSmallNativeAd(getAdsPrefs().facebookNativeBannerIds!![idIndex + 1])
                        }
                        loadSmallNativeAd()
                    }

                    override fun onAdLoaded(ad: Ad?) {
                        logger("--facebook_small_native--", "Native ad is loaded and ready to be displayed!")

                        isSmallNativeLoading = false
                        this@FacebookAds.smallNativeAd = nativeAd
                    }

                    override fun onAdClicked(ad: Ad?) {
                        logger("--facebook_small_native--", "Native ad clicked!")
                    }

                    override fun onLoggingImpression(ad: Ad?) {
                        logger("--facebook_small_native--", "Native ad impression logged!")

                        ManageAds.changeNativeAdPriority()
                        this@FacebookAds.smallNativeAd = null
                        loadSmallNativeAd()
                    }
                }

                nativeAd.loadAd(
                    nativeAd.buildLoadAdConfig()
                        .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                        .withAdListener(nativeAdListener)
                        .build()
                )
            }
        }
    }

    fun showSmallNativeAd(frameLayout: FrameLayout) {
        if (smallNativeAd == null) {
            loadSmallNativeAd()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayFacebookNativeAds) {
            frameLayout.gone()
            return
        }

        val nativeAdLayout = LayoutInflater.from(getKoinActivity()).inflate(R.layout.ad_facebook_native_small, null) as NativeAdLayout
        inflateSmallNativeAd(smallNativeAd!!, nativeAdLayout)
        frameLayout.removeAllViews()
        frameLayout.addView(nativeAdLayout)
    }

    private fun inflateSmallNativeAd(nativeAd: NativeBannerAd, nativeAdLayout: NativeAdLayout) {
        nativeAd.unregisterView()

        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        val adView = nativeAdLayout.findViewById<ConstraintLayout>(R.id.ad_unit)

        // Add the AdOptionsView
        val adChoicesContainer: LinearLayout = adView.findViewById(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(getKoinActivity(), nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.
        val nativeAdIcon: MediaView? = adView.findViewById(R.id.native_ad_icon)
        val nativeAdTitle: TextView = adView.findViewById(R.id.ad_headline)
        val nativeAdSocialContext: TextView = adView.findViewById(R.id.native_ad_social_context)
        val nativeAdBody: TextView = adView.findViewById(R.id.native_ad_body)
        val sponsoredLabel: TextView = adView.findViewById(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction: Button = adView.findViewById(R.id.ad_call_to_action)

        // Set the Text.
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility = if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation

        // Create a list of clickable views
        val clickableViews: MutableList<View?> = ArrayList<View?>()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
            adView, nativeAdIcon, clickableViews
        )
    }

    private fun loadInterstitialAd() {
        if ((interstitialAd != null) || isInterstitialLoading) {
            return
        }

        if (getAdsPrefs().facebookInterstitialIds.isNullOrEmpty() == true) {
            return
        }

        if (!getAdsPrefs().displayInterstitialAds) {
            return
        } else if (!getAdsPrefs().displayFacebookInterstitialAds) {
            return
        }

        val id = getAdsPrefs().facebookInterstitialIds!![0]
        loadInterstitialAd(id)
    }

    private fun loadInterstitialAd(id: String) {
        isInterstitialLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            synchronized(this) {
                val interstitialAd = InterstitialAd(getKoinContext(), id)

                val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
                    override fun onInterstitialDismissed(ad: Ad?) {
                        logger("--facebook_interstitial--", "loadInterstitialAd onInterstitialDismissed")

                        ManageAds.isFullScreenAdShown = false

                        ManageAds.changeInterstitialAdPriority()
                        this@FacebookAds.interstitialAd = null
                        loadInterstitialAd()

                        onFullScreenAdDismissed?.invoke()
                    }

                    override fun onInterstitialDisplayed(ad: Ad?) {
                        logger("--facebook_interstitial--", "loadInterstitialAd onInterstitialDisplayed")

                        ManageAds.isFullScreenAdShown = true
                    }

                    override fun onError(ad: Ad?, adError: AdError) {
                        logger("--facebook_interstitial--", "loadInterstitialAd onError: ${adError.errorMessage}")

                        isInterstitialLoading = false
                        this@FacebookAds.interstitialAd = null

                        val idIndex = getAdsPrefs().facebookInterstitialIds!!.indexOf(id)
                        if (idIndex < (getAdsPrefs().facebookInterstitialIds!!.size - 1)) {
                            loadInterstitialAd(getAdsPrefs().facebookInterstitialIds!![idIndex + 1])
                        }
                    }

                    override fun onAdLoaded(ad: Ad?) {
                        logger("--facebook_interstitial--", "loadInterstitialAd onAdLoaded")

                        isInterstitialLoading = false
                        this@FacebookAds.interstitialAd = interstitialAd
                    }

                    override fun onAdClicked(ad: Ad?) {
                        logger("--facebook_interstitial--", "loadInterstitialAd onAdClicked")
                    }

                    override fun onLoggingImpression(ad: Ad?) {
                        logger("--facebook_interstitial--", "loadInterstitialAd onLoggingImpression")
                    }
                }

                // Request an ad
                interstitialAd.loadAd(
                    interstitialAd.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build()
                )
            }
        }
    }

    fun showInterstitialAd(onDismiss: () -> Unit) {
        if (interstitialAd == null) {
            ManageAds.changeInterstitialAdPriority()
            loadInterstitialAd()
            onDismiss.invoke()
            return
        }

        if (!getAdsPrefs().displayInterstitialAds || !getAdsPrefs().displayFacebookInterstitialAds) {
            onDismiss.invoke()
            return
        }

        onFullScreenAdDismissed = onDismiss
        interstitialAd?.show()
    }
}
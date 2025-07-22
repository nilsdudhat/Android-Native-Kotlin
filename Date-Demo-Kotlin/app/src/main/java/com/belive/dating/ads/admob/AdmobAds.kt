package com.belive.dating.ads.admob

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.belive.dating.R
import com.belive.dating.ads.ManageAds
import com.belive.dating.extensions.getAdsPrefs
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.gone
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdmobAds {

    var isAdsInitialized = false

    var nativeAd: NativeAd? = null
    var isNativeLoading = false

    var bannerAd: AdView? = null
    var isBannerLoading = false

    var appOpenAd: AppOpenAd? = null
    var isAppOpenLoading = false
    var isAppOpenShown = false

    var interstitialAd: InterstitialAd? = null
    var isInterstitialLoading = false

    fun loadAds() {
        if (!getAdsPrefs().displayAdmobAds) {
            return
        }
        if (!isAdsInitialized) {
            MobileAds.initialize(getKoinContext().applicationContext) {}
            isAdsInitialized = true
        }
        loadBannerAd()
        loadNativeAd()
        loadAppOpenAd()
        loadInterstitialAd()
    }

    fun loadBannerAd() {
        if ((bannerAd != null) || isBannerLoading) {
            return
        }

        if (getAdsPrefs().admobBannerIds.isNullOrEmpty() == true) {
            return
        }

        if (!getAdsPrefs().displayBannerAds) {
            return
        } else if (!getAdsPrefs().displayAdmobBannerAds) {
            return
        }

        val id = getAdsPrefs().admobBannerIds!![0]

        loadBannerAd(id)
    }

    fun loadBannerAd(id: String) {
        logger("--admob_banner--", "loadBannerAd id:$id")

        isBannerLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            synchronized(this) {
                val adView = AdView(getKoinContext())
                adView.adUnitId = id

                val adWidth: Int = run {
                    val context = getKoinContext()
                    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

                    val widthPixels: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // API 30+
                        val metrics = windowManager.currentWindowMetrics
                        val insets = metrics.windowInsets
                            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                        val bounds = metrics.bounds
                        bounds.width() - insets.left - insets.right
                    } else {
                        // API 23 - 29
                        val displayMetrics = DisplayMetrics()
                        @Suppress("DEPRECATION")
                        windowManager.defaultDisplay.getMetrics(displayMetrics)
                        displayMetrics.widthPixels
                    }

                    val density = context.resources.displayMetrics.density
                    (widthPixels / density).toInt()
                }

                adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getKoinContext(), adWidth))

                adView.adListener = object : AdListener() {
                    override fun onAdClicked() {
                        super.onAdClicked()

                        logger("--admob_banner--", "loadBannerAd onAdClicked:")
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()

                        logger("--admob_banner--", "loadBannerAd onAdClosed:")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)

                        logger("--admob_banner--", "loadBannerAd onAdFailedToLoad: ${gsonString(loadAdError.cause)}")

                        bannerAd = null
                        isBannerLoading = false

                        val idIndex = getAdsPrefs().admobBannerIds!!.indexOf(id)
                        if (idIndex < (getAdsPrefs().admobBannerIds!!.size - 1)) {
                            loadBannerAd(getAdsPrefs().admobBannerIds!![idIndex + 1])
                        }
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()

                        logger("--admob_banner--", "loadBannerAd onAdImpression:")

                        ManageAds.changeBannerAdPriority()

                        bannerAd = null
                        loadBannerAd()
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()

                        logger("--admob_banner--", "loadBannerAd onAdLoaded:")

                        bannerAd = adView
                        isBannerLoading = false
                    }

                    override fun onAdOpened() {
                        super.onAdOpened()

                        logger("--admob_banner--", "loadBannerAd onAdClosed:")
                    }

                    override fun onAdSwipeGestureClicked() {
                        super.onAdSwipeGestureClicked()

                        logger("--admob_banner--", "loadBannerAd onAdClosed:")
                    }
                }

                val adRequest = AdRequest.Builder().build()

                launch(Dispatchers.Main) {
                    adView.loadAd(adRequest)
                }
            }
        }
    }

    fun showBannerAd(frameLayout: FrameLayout) {
        if (bannerAd == null) {
            frameLayout.gone()
            loadBannerAd()
        }

        frameLayout.removeAllViews()
        frameLayout.addView(bannerAd)
        bannerAd = null
    }

    fun loadNativeAd() {
        if ((nativeAd != null) || isNativeLoading) {
            return
        }

        if (getAdsPrefs().admobNativeIds.isNullOrEmpty() == true) {
            return
        }

        if (!getAdsPrefs().displayNativeAds) {
            return
        } else if (!getAdsPrefs().displayAdmobNativeAds) {
            return
        }

        val id = getAdsPrefs().admobNativeIds!![0]

        loadNativeAd(id)
    }

    fun loadNativeAd(id: String) {
        logger("--admob_native--", "loadNativeAd id:$id")

        isNativeLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            synchronized(this) {
                val videoOptions = VideoOptions.Builder().setStartMuted(false).build()

                val adOptions: NativeAdOptions = NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_RIGHT)
//                    .setMediaAspectRatio(MediaAspectRatio.PORTRAIT) // this can decrease revenue
                    .setVideoOptions(videoOptions).build()

                val builder = AdLoader.Builder(getKoinActivity(), id)
                builder.forNativeAd(NativeAd.OnNativeAdLoadedListener { unifiedNativeAd: NativeAd? ->
                    this@AdmobAds.nativeAd = unifiedNativeAd
                    this@AdmobAds.isNativeLoading = false
                })
                builder.withNativeAdOptions(
                    NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).setVideoOptions(VideoOptions.Builder().build()).build()
                )
                builder.withAdListener(object : AdListener() {
                    override fun onAdImpression() {
                        super.onAdImpression()

                        logger("--admob_native--", "loadNativeAd onAdImpression:")

                        ManageAds.changeNativeAdPriority()
                        this@AdmobAds.nativeAd = null
                        loadNativeAd()
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()

                        logger("--admob_native--", "loadNativeAd onAdClicked:")
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()

                        logger("--admob_native--", "loadNativeAd onAdClosed:")
                    }

                    override fun onAdOpened() {
                        super.onAdOpened()

                        logger("--admob_native--", "loadNativeAd onAdOpened:")
                    }

                    override fun onAdSwipeGestureClicked() {
                        super.onAdSwipeGestureClicked()

                        logger("--admob_native--", "loadNativeAd onAdSwipeGestureClicked:")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)

                        logger("--admob_native--", "loadNativeAd onAdFailedToLoad:" + loadAdError.cause)

                        isNativeLoading = false
                        this@AdmobAds.nativeAd = null

                        val idIndex = getAdsPrefs().admobNativeIds!!.indexOf(id)
                        if (idIndex < (getAdsPrefs().admobNativeIds!!.size - 1)) {
                            loadNativeAd(getAdsPrefs().admobNativeIds!![idIndex + 1])
                        }
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()

                        isNativeLoading = false

                        logger("--admob_native--", "loadNativeAd onAdLoaded:")
                    }
                }).withNativeAdOptions(adOptions).build().loadAd(AdRequest.Builder().build())
            }
        }
    }

    fun showNativeFullScreenAd(frameLayout: FrameLayout) {
        if (nativeAd == null) {
            frameLayout.gone()
            loadNativeAd()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayAdmobNativeAds) {
            frameLayout.gone()
            return
        }

        val unifiedNativeAdView = LayoutInflater.from(getKoinActivity()).inflate(R.layout.ad_admob_native_full_screen, null) as NativeAdView
        populateNativeAd(nativeAd!!, unifiedNativeAdView)
        frameLayout.removeAllViews()
        frameLayout.addView(unifiedNativeAdView)
    }

    fun showNativeSquareAd(frameLayout: FrameLayout) {
        if (nativeAd == null) {
            frameLayout.gone()
            loadNativeAd()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayAdmobNativeAds) {
            frameLayout.gone()
            return
        }

        val unifiedNativeAdView = LayoutInflater.from(getKoinActivity()).inflate(R.layout.ad_admob_native_square, null) as NativeAdView
        populateNativeAd(nativeAd!!, unifiedNativeAdView)
        frameLayout.removeAllViews()
        frameLayout.addView(unifiedNativeAdView)
    }

    fun showNativeInGridAd(frameLayout: FrameLayout) {
        if (nativeAd == null) {
            frameLayout.gone()
            loadNativeAd()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayAdmobNativeAds) {
            frameLayout.gone()
            return
        }

        val unifiedNativeAdView = LayoutInflater.from(getKoinActivity()).inflate(R.layout.ad_admob_native_grid, null) as NativeAdView
        populateNativeAd(nativeAd!!, unifiedNativeAdView)
        frameLayout.removeAllViews()
        frameLayout.addView(unifiedNativeAdView)
    }

    private fun populateNativeAd(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        // Set the media view.
        nativeAdView.mediaView = nativeAdView.findViewById(R.id.ad_media)

        // Set other ad assets.
        nativeAdView.headlineView = nativeAdView.findViewById<View?>(R.id.ad_headline)
        nativeAdView.bodyView = nativeAdView.findViewById<View?>(R.id.ad_body)
        nativeAdView.callToActionView = nativeAdView.findViewById<View?>(R.id.ad_call_to_action)
        nativeAdView.iconView = nativeAdView.findViewById<View?>(R.id.ad_app_icon).apply {
            setClipToOutline(true)
        }

        // The headline and mediaContent are guaranteed to be in every NativeAd.
        (nativeAdView.headlineView as TextView).text = nativeAd.headline
        nativeAdView.mediaView?.mediaContent = nativeAd.mediaContent

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            nativeAdView.bodyView?.visibility = View.INVISIBLE
        } else {
            nativeAdView.bodyView?.visibility = View.VISIBLE
            (nativeAdView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            nativeAdView.callToActionView?.visibility = View.INVISIBLE
        } else {
            nativeAdView.callToActionView?.visibility = View.VISIBLE
            (nativeAdView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            nativeAdView.iconView?.visibility = View.GONE
        } else {
            ((nativeAdView.iconView) as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            nativeAdView.iconView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)

        val videoController: VideoController? = nativeAd.mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (videoController?.hasVideoContent() == true) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController.
            // The VideoController will call methods on this object when events occur in the
            // video lifecycle.

            videoController.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    super.onVideoEnd()
                }
            }
        }
    }

    fun showSmallNativeAd(frameLayout: FrameLayout) {
        if (nativeAd == null) {
            frameLayout.gone()
            loadNativeAd()
            return
        }

        if (!getAdsPrefs().displayNativeAds || !getAdsPrefs().displayAdmobNativeAds) {
            frameLayout.gone()
            return
        }

        val unifiedNativeAdView = LayoutInflater.from(getKoinActivity()).inflate(R.layout.ad_admob_native_small, null) as NativeAdView
        populateSmallScreenNativeAdView(nativeAd!!, unifiedNativeAdView)
        frameLayout.removeAllViews()
        frameLayout.addView(unifiedNativeAdView)
    }

    private fun populateSmallScreenNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        // Set other ad assets.
        nativeAdView.headlineView = nativeAdView.findViewById<View?>(R.id.ad_headline)
        nativeAdView.bodyView = nativeAdView.findViewById<View?>(R.id.ad_body)
        nativeAdView.callToActionView = nativeAdView.findViewById<View?>(R.id.ad_call_to_action)
        nativeAdView.iconView = nativeAdView.findViewById<View?>(R.id.ad_app_icon).apply {
            setClipToOutline(true)
        }

        // The headline and mediaContent are guaranteed to be in every NativeAd.
        (nativeAdView.headlineView as TextView).text = nativeAd.headline

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            nativeAdView.bodyView?.visibility = View.INVISIBLE
        } else {
            nativeAdView.bodyView?.visibility = View.VISIBLE
            (nativeAdView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            nativeAdView.callToActionView?.visibility = View.INVISIBLE
        } else {
            nativeAdView.callToActionView?.visibility = View.VISIBLE
            (nativeAdView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            nativeAdView.iconView?.visibility = View.GONE
        } else {
            ((nativeAdView.iconView) as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            nativeAdView.iconView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)
    }

    /*private fun populateNativeAdWithMediaView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        val mediaView: MediaView = nativeAdView.findViewById(R.id.ad_media)

        // Set the media view.
        nativeAdView.mediaView = mediaView

        // Set other ad assets.
        nativeAdView.headlineView = nativeAdView.findViewById<View?>(R.id.ad_headline)
        nativeAdView.bodyView = nativeAdView.findViewById<View?>(R.id.ad_body)
        nativeAdView.callToActionView = nativeAdView.findViewById<View?>(R.id.ad_call_to_action)
        nativeAdView.iconView = nativeAdView.findViewById<View?>(R.id.ad_app_icon)
        nativeAdView.priceView = nativeAdView.findViewById<View?>(R.id.ad_price)
        nativeAdView.starRatingView = nativeAdView.findViewById<View?>(R.id.ad_stars)
        nativeAdView.storeView = nativeAdView.findViewById<View?>(R.id.ad_store)
        nativeAdView.advertiserView = nativeAdView.findViewById<View?>(R.id.ad_advertiser)

        // The headline and mediaContent are guaranteed to be in every NativeAd.
        (nativeAdView.headlineView as TextView).text = nativeAd.headline
        nativeAdView.mediaView?.mediaContent = nativeAd.mediaContent

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            nativeAdView.bodyView?.visibility = View.INVISIBLE
        } else {
            nativeAdView.bodyView?.visibility = View.VISIBLE
            (nativeAdView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            nativeAdView.callToActionView?.visibility = View.INVISIBLE
        } else {
            nativeAdView.callToActionView?.visibility = View.VISIBLE
            (nativeAdView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            nativeAdView.iconView?.visibility = View.GONE
        } else {
            ((nativeAdView.iconView) as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            nativeAdView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            nativeAdView.priceView?.visibility = View.INVISIBLE
        } else {
            nativeAdView.priceView?.visibility = View.VISIBLE
            (nativeAdView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            nativeAdView.storeView?.visibility = View.INVISIBLE
        } else {
            nativeAdView.storeView?.visibility = View.VISIBLE
            (nativeAdView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            nativeAdView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (nativeAdView.starRatingView as RatingBar).rating = nativeAd.starRating?.toFloat() ?: 5f
            nativeAdView.starRatingView?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            nativeAdView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (nativeAdView.advertiserView as TextView).text = nativeAd.advertiser
            nativeAdView.advertiserView?.visibility = View.VISIBLE
        }

        mediaView.setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View) {
                if (child is ImageView) { // Images
                    val imageView = child
                    imageView.setAdjustViewBounds(true)
                } else { // Videos
                    val scale = getKoinActivity().resources.displayMetrics.density

                    val maxHeightPixels = 175
                    val maxHeightDp = (maxHeightPixels * scale + 0.5f).toInt()
                    logger("--height--", "onChildViewAdded: $maxHeightDp")

                    val params: ViewGroup.LayoutParams = child.layoutParams
                    params.height = maxHeightDp
                    child.setLayoutParams(params)
                }
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {}
        })

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc?.hasVideoContent() == true) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.

            vc.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    logger("--admob_native--", "populateNativeAdWithMediaView setVideoLifecycleCallbacks:")
                    // Publishers should allow native ads to complete video playback before
                }
            }
        }
    }*/

    private fun loadAppOpenAd() {
        if ((appOpenAd != null) || isAppOpenLoading) {
            return
        }

        if (getAdsPrefs().admobAppOpenIds.isNullOrEmpty() == true) {
            return
        }

        if (!getAdsPrefs().displayAds || !getAdsPrefs().displayAdmobAds || !getAdsPrefs().displayAdmobAppOpenAds) {
            return
        }

        val id = getAdsPrefs().admobAppOpenIds!![0]

        loadAppOpenAd(id)
    }

    private fun loadAppOpenAd(id: String) {
        logger("--admob_app_open--", "loadAppOpenAd id: $id")

        isAppOpenLoading = true

        CoroutineScope(Dispatchers.Main).launch {
            synchronized(this) {
                val request = AdRequest.Builder().build()
                AppOpenAd.load(
                    getKoinActivity(),
                    id,
                    request,
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            logger("--admob_app_open--", "loadAppOpenAd onAdLoaded:")

                            isAppOpenLoading = false
                            appOpenAd = ad
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            logger("--admob_app_open--", "loadAppOpenAd onAdFailedToLoad: ${loadAdError.cause}")

                            isAppOpenLoading = false
                            appOpenAd = null

                            val idIndex = getAdsPrefs().admobAppOpenIds!!.indexOf(id)
                            if (idIndex < (getAdsPrefs().admobAppOpenIds!!.size - 1)) {
                                loadNativeAd(getAdsPrefs().admobAppOpenIds!![idIndex + 1])
                            }
                        }
                    },
                )
            }
        }
    }

    fun showAppOpenAdAfterSplash(onDismiss: () -> Unit) {
        if (!ManageAds.startLoadingAds) {
            onDismiss.invoke()
            return
        }

        if (getAdsPrefs().admobAppOpenIds.isNullOrEmpty() == true) {
            onDismiss.invoke()
            return
        }

        if (!getAdsPrefs().displayAds || !getAdsPrefs().displayAdmobAds) {
            onDismiss.invoke()
            return
        }

        if (!getAdsPrefs().displayAdmobAppOpenAds) {
            onDismiss.invoke()
            return
        }

        if (ManageAds.isFullScreenAdShown) {
            onDismiss.invoke()
            return
        }

        val id = getAdsPrefs().admobAppOpenIds!![0]

        logger("--admob_app_open--", "showAppOpenAdAfterSplash id: $id")

        CoroutineScope(Dispatchers.Main).launch {
            synchronized(this) {
                val request = AdRequest.Builder().build()
                AppOpenAd.load(
                    getKoinActivity(),
                    id,
                    request,
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            logger("--admob_app_open--", "showAppOpenAdAfterSplash onAdLoaded:")

                            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdClicked() {
                                    super.onAdClicked()

                                    logger("--admob_app_open--", "loadAppOpenAd onAdClicked:")
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()

                                    logger("--admob_app_open--", "loadAppOpenAd onAdDismissedFullScreenContent:")

                                    isAppOpenShown = false

                                    appOpenAd = null
                                    loadAppOpenAd()

                                    onDismiss.invoke()
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)

                                    logger("--admob_app_open--", "loadAppOpenAd onAdFailedToShowFullScreenContent:")

                                    appOpenAd = null
                                    loadAppOpenAd()

                                    onDismiss.invoke()
                                }

                                override fun onAdImpression() {
                                    super.onAdImpression()

                                    logger("--admob_app_open--", "loadAppOpenAd onAdImpression:")

                                    isAppOpenShown = true
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()

                                    logger("--admob_app_open--", "loadAppOpenAd onAdShowedFullScreenContent:")
                                }
                            }

                            ad.show(getKoinActivity())
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            logger("--admob_app_open--", "showAppOpenAdAfterSplash onAdFailedToLoad: ${loadAdError.cause}")

                            onDismiss.invoke()
                        }
                    },
                )
            }
        }
    }

    fun showAppOpenAd() {
        if (isAppOpenShown) {
            return
        }

        if (ManageAds.isFullScreenAdShown) {
            return
        }

        if (appOpenAd == null) {
            loadAppOpenAd()
        } else {
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()

                    logger("--admob_app_open--", "loadAppOpenAd onAdClicked:")
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()

                    logger("--admob_app_open--", "loadAppOpenAd onAdDismissedFullScreenContent:")

                    isAppOpenShown = false

                    appOpenAd = null
                    loadAppOpenAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)

                    logger("--admob_app_open--", "loadAppOpenAd onAdFailedToShowFullScreenContent:")

                    appOpenAd = null
                    loadAppOpenAd()
                }

                override fun onAdImpression() {
                    super.onAdImpression()

                    logger("--admob_app_open--", "loadAppOpenAd onAdImpression:")

                    isAppOpenShown = true
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()

                    logger("--admob_app_open--", "loadAppOpenAd onAdShowedFullScreenContent:")
                }
            }
            appOpenAd?.show(getKoinActivity())
        }
    }

    private fun loadInterstitialAd() {
        if ((interstitialAd != null) || isInterstitialLoading) {
            return
        }

        if (getAdsPrefs().admobInterstitialIds.isNullOrEmpty() == true) {
            return
        }

        if (!getAdsPrefs().displayInterstitialAds) {
            return
        } else if (!getAdsPrefs().displayInterstitialAds) {
            return
        }

        val id = getAdsPrefs().admobInterstitialIds!![0]

        loadInterstitialAd(id)
    }

    private fun loadInterstitialAd(id: String) {
        logger("--admob_interstitial--", "loadInterstitialAd id:$id")

        isInterstitialLoading = true

        CoroutineScope(Dispatchers.Main).launch {
            synchronized(this) {
                val adRequest = AdRequest.Builder().build()

                InterstitialAd.load(getKoinContext(), id, adRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        logger("--admob_interstitial--", "loadInterstitialAd onAdFailedToLoad: ${adError.cause}")

                        interstitialAd = null
                        isInterstitialLoading = false

                        val idIndex = getAdsPrefs().admobInterstitialIds!!.indexOf(id)
                        if (idIndex < (getAdsPrefs().admobInterstitialIds!!.size - 1)) {
                            loadInterstitialAd(getAdsPrefs().admobInterstitialIds!![idIndex + 1])
                        }
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        logger("--admob_interstitial--", "loadInterstitialAd onAdLoaded")

                        this@AdmobAds.interstitialAd = interstitialAd
                        isInterstitialLoading = false
                    }
                })
            }
        }
    }

    fun showInterstitialAd(onDismiss: () -> Unit) {
        if (interstitialAd == null) {
            loadInterstitialAd()
            onDismiss.invoke()
            return
        }

        if (!getAdsPrefs().displayInterstitialAds || !getAdsPrefs().displayAdmobInterstitialAds) {
            onDismiss.invoke()
            return
        }

        interstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                logger("--admob_interstitial--", "showInterstitialAd onAdClicked")
            }

            override fun onAdDismissedFullScreenContent() {
                logger("--admob_interstitial--", "showInterstitialAd onAdDismissedFullScreenContent")

                ManageAds.isFullScreenAdShown = false

                ManageAds.changeInterstitialAdPriority()
                interstitialAd = null
                loadInterstitialAd()

                onDismiss.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                logger("--admob_interstitial--", "showInterstitialAd onAdFailedToShowFullScreenContent")

                interstitialAd = null
                loadInterstitialAd()
            }

            override fun onAdImpression() {
                logger("--admob_interstitial--", "showInterstitialAd onAdImpression")
            }

            override fun onAdShowedFullScreenContent() {
                logger("--admob_interstitial--", "showInterstitialAd onAdShowedFullScreenContent")

                ManageAds.isFullScreenAdShown = true
            }
        }

        interstitialAd?.show(getKoinActivity())
    }
}
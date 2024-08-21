package com.adsmanager.app

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowMetrics
import android.widget.FrameLayout
import com.adsmanager.app.databinding.AdAdmobSmallNativeBinding
import com.adsmanager.app.databinding.AdmobBigNativeBinding
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

var bannerAdView: AdView? = null
var interstitialAd: InterstitialAd? = null
var bigNativeAd: NativeAd? = null
var smallNativeAd: NativeAd? = null
var rewardedInterstitialAd: RewardedInterstitialAd? = null
var rewardedVideoAd: RewardedAd? = null

fun loadAdmobAds(activity: Activity) {
    loadBannerAd(activity)
    loadInterstitialAd(activity)
    loadBigNativeAd(activity)
    loadSmallNativeAd(activity)
    loadRewardInterstitialAd(activity)
    loadRewardVideoAd(activity)
}

fun getAdmobAdSize(activity: Activity): AdSize {
    val displayMetrics = activity.resources.displayMetrics
    val adWidthPixels =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            displayMetrics.widthPixels
        }
    val density = displayMetrics.density
    return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
        activity,
        (adWidthPixels / density).toInt()
    )
}

fun loadBannerAd(activity: Activity) {
    if (bannerAdView != null) {
        return
    }

    val adSize = getAdmobAdSize(activity)

    val adView = AdView(activity)
    adView.adUnitId = activity.getString(R.string.admob_banner)
    adView.setAdSize(adSize)

    val extras = Bundle()
    extras.putString("collapsible", "bottom")

    val adRequest = AdRequest.Builder()
        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        .build()
    adView.loadAd(adRequest)
    adView.adListener = object : AdListener() {
        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            super.onAdFailedToLoad(loadAdError)

            bannerAdView = null

            Log.d("--admob_banner--", "showAdmobBannerAd onAdFailedToLoad:" + loadAdError.message)
        }

        override fun onAdImpression() {
            super.onAdImpression()

            Log.d("--admob_banner--", "showAdmobBannerAd onAdImpression:")

            bannerAdView = null
            loadBannerAd(activity)
        }

        override fun onAdLoaded() {
            super.onAdLoaded()

            bannerAdView = adView

            Log.d("--admob_banner--", "showAdmobBannerAd onAdLoaded:")
        }
    }
}

fun displayBannerAd(activity: Activity, frameLayout: FrameLayout) {
    if (bannerAdView == null) {
        Log.d("--admob_banner--", "displayBannerAd bannerAdView is null")
        loadBannerAd(activity)
        return
    }
    frameLayout.layoutParams.height = bannerAdView!!.height
    frameLayout.removeAllViews()
    frameLayout.addView(bannerAdView)
}

fun loadSmallNativeAd(activity: Activity) {

    val adLoader = AdLoader.Builder(activity, activity.getString(R.string.admob_native))
        .forNativeAd { ad: NativeAd ->
            Log.d("--admob_small_native--", "loadSmallNativeAd: forNativeAd")
            smallNativeAd = ad
        }
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Handle the failure.
                Log.d("--admob_small_native--", "onAdFailedToLoad: ${adError.message}")
                smallNativeAd = null
            }

            override fun onAdImpression() {
                super.onAdImpression()

                smallNativeAd = null
                loadSmallNativeAd(activity)
            }
        })
        .withNativeAdOptions(NativeAdOptions.Builder().build())
        .build()
//    For Admob Only
    adLoader.loadAd(AdRequest.Builder().build())
//    For Admob Mediation
//    adLoader.loadAds(AdRequest.Builder().build(), 3)
}

fun displaySmallNativeAd(activity: Activity, frameLayout: FrameLayout) {
    if (smallNativeAd == null) {
        Log.d("--admob_small_native--", "displaySmallNativeAd bigNativeAd is null")
        loadSmallNativeAd(activity)
        return
    }
    val binding = AdAdmobSmallNativeBinding.inflate(activity.layoutInflater)
    populateSmallNativeAdView(binding, smallNativeAd!!)
    frameLayout.removeAllViews()
    frameLayout.addView(binding.root)
}

private fun populateSmallNativeAdView(binding: AdAdmobSmallNativeBinding, nativeAd: NativeAd) {

    val nativeAdView = binding.root

    // Set other ad assets.
    nativeAdView.headlineView = binding.adHeadline
    nativeAdView.callToActionView = binding.adCallToAction
    nativeAdView.iconView = binding.adAppIcon
    nativeAdView.priceView = binding.adPrice
    nativeAdView.starRatingView = binding.adStars
    nativeAdView.storeView = binding.adStore
    nativeAdView.advertiserView = binding.adAdvertiser

    // The headline and media content are guaranteed to be in every UnifiedNativeAd.
    binding.adHeadline.text = nativeAd.headline

    if (nativeAd.callToAction == null) {
        binding.adCallToAction.visibility = View.INVISIBLE
    } else {
        binding.adCallToAction.visibility = View.VISIBLE
        binding.adCallToAction.text = nativeAd.callToAction
    }

    if (nativeAd.icon == null) {
        binding.adAppIcon.visibility = View.GONE
    } else {
        binding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
        binding.adAppIcon.visibility = View.VISIBLE
    }

    if (nativeAd.price == null) {
        binding.adPrice.visibility = View.INVISIBLE
    } else {
        binding.adPrice.visibility = View.VISIBLE
        binding.adPrice.text = nativeAd.price
    }

    if (nativeAd.store == null) {
        binding.adStore.visibility = View.INVISIBLE
    } else {
        binding.adStore.visibility = View.VISIBLE
        binding.adStore.text = nativeAd.store
    }

    if (nativeAd.starRating == null) {
        binding.adStars.visibility = View.INVISIBLE
    } else {
        binding.adStars.rating = nativeAd.starRating!!.toFloat()
        binding.adStars.visibility = View.VISIBLE
    }

    if (nativeAd.advertiser == null) {
        binding.adAdvertiser.visibility = View.INVISIBLE
    } else {
        binding.adAdvertiser.text = nativeAd.advertiser
        binding.adAdvertiser.visibility = View.VISIBLE
    }

    // This method tells the Google Mobile Ads SDK that you have finished populating your
    // native ad view with this native ad.
    nativeAdView.setNativeAd(nativeAd)
}

fun loadBigNativeAd(activity: Activity) {

    val adLoader = AdLoader.Builder(activity, activity.getString(R.string.admob_native))
        .forNativeAd { ad: NativeAd ->
            Log.d("--admob_big_native--", "loadBigNativeAd: forNativeAd")
            bigNativeAd = ad
        }
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Handle the failure.
                Log.d("--admob_big_native--", "onAdFailedToLoad: ${adError.message}")
                bigNativeAd = null
            }

            override fun onAdImpression() {
                super.onAdImpression()

                bigNativeAd = null
                loadBigNativeAd(activity)
            }
        })
        .withNativeAdOptions(
            NativeAdOptions.Builder()
                .setVideoOptions(
                    VideoOptions.Builder()
                        .setStartMuted(true)
                        .setClickToExpandRequested(true)
                        .build()
                )
                .build()
        )
        .build()
//    For Admob Only
    adLoader.loadAd(AdRequest.Builder().build())
//    For Admob Mediation
//    adLoader.loadAds(AdRequest.Builder().build(), 3)
}

fun displayBigNativeAd(activity: Activity, frameLayout: FrameLayout) {
    if (bigNativeAd == null) {
        Log.d("--admob_big_native--", "displayBigNativeAd bigNativeAd is null")
        loadBigNativeAd(activity)
        return
    }
    val binding = AdmobBigNativeBinding.inflate(activity.layoutInflater)
    populateBigNativeAdView(binding, bigNativeAd!!)
    frameLayout.removeAllViews()
    frameLayout.addView(binding.root)
}

private fun populateBigNativeAdView(binding: AdmobBigNativeBinding, nativeAd: NativeAd) {

    val nativeAdView = binding.root

    // Set the media view.
    nativeAdView.mediaView = binding.adMedia

    // Set other ad assets.
    nativeAdView.headlineView = binding.adHeadline
    nativeAdView.bodyView = binding.adBody
    nativeAdView.callToActionView = binding.adCallToAction
    nativeAdView.iconView = binding.adAppIcon
    nativeAdView.priceView = binding.adPrice
    nativeAdView.starRatingView = binding.adStars
    nativeAdView.storeView = binding.adStore
    nativeAdView.advertiserView = binding.adAdvertiser

    // The headline and media content are guaranteed to be in every UnifiedNativeAd.
    binding.adHeadline.text = nativeAd.headline
    nativeAd.mediaContent?.let { binding.adMedia.setMediaContent(it) }

    // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
    // check before trying to display them.
    if (nativeAd.body == null) {
        binding.adBody.visibility = View.INVISIBLE
    } else {
        binding.adBody.visibility = View.VISIBLE
        binding.adBody.text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
        binding.adCallToAction.visibility = View.INVISIBLE
    } else {
        binding.adCallToAction.visibility = View.VISIBLE
        binding.adCallToAction.text = nativeAd.callToAction
    }

    if (nativeAd.icon == null) {
        binding.adAppIcon.visibility = View.GONE
    } else {
        binding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
        binding.adAppIcon.visibility = View.VISIBLE
    }

    if (nativeAd.price == null) {
        binding.adPrice.visibility = View.INVISIBLE
    } else {
        binding.adPrice.visibility = View.VISIBLE
        binding.adPrice.text = nativeAd.price
    }

    if (nativeAd.store == null) {
        binding.adStore.visibility = View.INVISIBLE
    } else {
        binding.adStore.visibility = View.VISIBLE
        binding.adStore.text = nativeAd.store
    }

    if (nativeAd.starRating == null) {
        binding.adStars.visibility = View.INVISIBLE
    } else {
        binding.adStars.rating = nativeAd.starRating!!.toFloat()
        binding.adStars.visibility = View.VISIBLE
    }

    if (nativeAd.advertiser == null) {
        binding.adAdvertiser.visibility = View.INVISIBLE
    } else {
        binding.adAdvertiser.text = nativeAd.advertiser
        binding.adAdvertiser.visibility = View.VISIBLE
    }

    // This method tells the Google Mobile Ads SDK that you have finished populating your
    // native ad view with this native ad.
    nativeAdView.setNativeAd(nativeAd)

    // Get the video controller for the ad. One will always be provided, even if the ad doesn't
    // have a video asset.
    val mediaContent = nativeAd.mediaContent
    val vc = mediaContent?.videoController

    // Updates the UI to say whether or not this ad has a video asset.
    if (vc != null && mediaContent.hasVideoContent()) {
        // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
        // VideoController will call methods on this object when events occur in the video
        // lifecycle.
        vc.videoLifecycleCallbacks =
            object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    super.onVideoEnd()
                }
            }
    }
}

fun loadInterstitialAd(activity: Activity) {
    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
        activity,
        activity.getString(R.string.admob_interstitial),
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(
                    "--admob_interstitial--",
                    "loadInterstitialAd onAdFailedToLoad:" + adError.message
                )
                interstitialAd = null
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d("--admob_interstitial--", "loadInterstitialAd onAdLoaded:")
                interstitialAd = ad
            }
        })
}

interface OnInterstitialAdDismiss {
    fun onInterstitialAdDismiss()
}

fun displayInterstitialAd(activity: Activity, onInterstitialAdDismiss: OnInterstitialAdDismiss) {
    if (interstitialAd == null) {
        Log.d("--admob_interstitial--", "displayInterstitialAd interstitialAd is null")
        loadInterstitialAd(activity)

        onInterstitialAdDismiss.onInterstitialAdDismiss()
        return
    }

    interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdClicked() {
            // Called when a click is recorded for an ad.
            Log.d("--admob_interstitial--", "displayInterstitialAd onAdClicked:")
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()

            // Called when ad is dismissed.
            Log.d("--admob_interstitial--", "displayInterstitialAd onAdDismissedFullScreenContent:")
            interstitialAd = null

            loadInterstitialAd(activity)

            onInterstitialAdDismiss.onInterstitialAdDismiss()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            super.onAdFailedToShowFullScreenContent(adError)

            // Called when ad fails to show.
            Log.d(
                "--admob_interstitial--",
                "displayInterstitialAd onAdDismissedFullScreenContent: ${adError.message}"
            )
            interstitialAd = null

            loadInterstitialAd(activity)

            onInterstitialAdDismiss.onInterstitialAdDismiss()
        }

        override fun onAdImpression() {
            super.onAdImpression()

            // Called when an impression is recorded for an ad.
            Log.d("--admob_interstitial--", "displayInterstitialAd onAdImpression:")
        }

        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()

            // Called when ad is shown.
            Log.d("--admob_interstitial--", "displayInterstitialAd onAdShowedFullScreenContent:")
        }
    }
    interstitialAd?.show(activity)
}

fun loadRewardVideoAd(activity: Activity) {
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(activity,activity.getString(R.string.admob_reward_video), adRequest, object : RewardedAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
            Log.d("--admob_reward_video--", "loadRewardVideoAd onAdFailedToLoad: ${adError.message}")
            rewardedVideoAd = null
        }

        override fun onAdLoaded(ad: RewardedAd) {
            Log.d("--admob_reward_video--", "loadRewardVideoAd onAdLoaded:")
            rewardedVideoAd = ad
        }
    })
}

fun displayRewardVideoAd(activity: Activity, onRewardAdListener: OnRewardAdListener) {
    if (rewardedVideoAd == null) {
        Log.d("--admob_reward_video--", "displayRewardVideoAd rewardedVideoAd is null")
        loadRewardVideoAd(activity)

        onRewardAdListener.onAdNotAvailable()
        return
    }

    rewardedVideoAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
        override fun onAdClicked() {
            super.onAdClicked()

            // Called when a click is recorded for an ad.
            Log.d("--admob_reward_video--", "onAdClicked:")
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()

            // Called when ad is dismissed.
            // Set the ad reference to null so you don't show the ad a second time.
            Log.d("--admob_reward_video--", "onAdDismissedFullScreenContent:")
            rewardedVideoAd = null

            loadRewardVideoAd(activity)

            onRewardAdListener.onAdDismissedFullScreenContent()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            super.onAdFailedToShowFullScreenContent(adError)

            // Called when ad fails to show.
            Log.d("--admob_reward_video--", "onAdFailedToShowFullScreenContent: ${adError.message}")
            rewardedVideoAd = null

            loadRewardVideoAd(activity)

            onRewardAdListener.onAdFailedToShowFullScreenContent(adError)
        }

        override fun onAdImpression() {
            super.onAdImpression()

            // Called when an impression is recorded for an ad.
            Log.d("--admob_reward_video--", "onAdImpression:")
        }

        override fun onAdShowedFullScreenContent() {
            // Called when ad is shown.
            Log.d("--admob_reward_video--", "onAdShowedFullScreenContent:")
        }
    }

    rewardedVideoAd?.let { ad ->
        ad.show(activity) { rewardItem ->
            // Handle the reward.
            val rewardAmount = rewardItem.amount
            val rewardType = rewardItem.type
            Log.d("--admob_reward_video--", "displayRewardVideoAd rewardedVideoAd is null")

            onRewardAdListener.onUserEarnedReward()
        }
    } ?: run {
        Log.d("--admob_reward_video--", "The rewarded ad wasn't ready yet.")

        onRewardAdListener.onAdNotAvailable()
    }
}

fun loadRewardInterstitialAd(activity: Activity) {
    RewardedInterstitialAd.load(activity, activity.getString(R.string.admob_reward_interstitial),
        AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                Log.d("--admob_reward_interstitial--", "displayInterstitialAd onAdLoaded:")
                rewardedInterstitialAd = ad
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(
                    "--admob_reward_interstitial--",
                    "displayInterstitialAd onAdFailedToLoad: ${adError.message}"
                )
                rewardedInterstitialAd = null
            }
        })
}

interface OnRewardAdListener {
    fun onAdDismissedFullScreenContent()
    fun onAdFailedToShowFullScreenContent(adError: AdError)
    fun onUserEarnedReward()
    fun onAdNotAvailable()
}

fun displayRewardInterstitialAd(activity: Activity, onRewardAdListener: OnRewardAdListener) {
    if (rewardedInterstitialAd == null) {
        Log.d(
            "--admob_reward_interstitial--",
            "displayRewardInterstitialAd rewardedInterstitialAd is null"
        )
        loadRewardInterstitialAd(activity)

        onRewardAdListener.onAdNotAvailable()
        return
    }

    rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdClicked() {
            // Called when a click is recorded for an ad.
            Log.d("--admob_reward_interstitial--", "Ad was clicked.")
        }

        override fun onAdDismissedFullScreenContent() {
            // Called when ad is dismissed.
            // Set the ad reference to null so you don't show the ad a second time.
            Log.d("--admob_reward_interstitial--", "Ad dismissed fullscreen content.")
            rewardedInterstitialAd = null
            loadRewardInterstitialAd(activity)

            onRewardAdListener.onAdDismissedFullScreenContent()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            // Called when ad fails to show.
            Log.e("--admob_reward_interstitial--", "Ad failed to show fullscreen content.")
            rewardedInterstitialAd = null

            onRewardAdListener.onAdFailedToShowFullScreenContent(adError)
        }

        override fun onAdImpression() {
            // Called when an impression is recorded for an ad.
            Log.d("--admob_reward_interstitial--", "Ad recorded an impression.")
        }

        override fun onAdShowedFullScreenContent() {
            // Called when ad is shown.
            Log.d("--admob_reward_interstitial--", "Ad showed fullscreen content.")
        }
    }

    rewardedInterstitialAd.let {
        it?.show(activity) { rewardItem ->
            // Handle the reward.
            val rewardAmount = rewardItem.amount
            val rewardType = rewardItem.type
            Log.d("--admob_reward_interstitial--", "User earned the reward.")
            onRewardAdListener.onUserEarnedReward()
        }
    } ?: run {
        Log.d("--admob_reward_interstitial--", "The rewarded ad wasn't ready yet.")

        onRewardAdListener.onAdNotAvailable()
    }
}
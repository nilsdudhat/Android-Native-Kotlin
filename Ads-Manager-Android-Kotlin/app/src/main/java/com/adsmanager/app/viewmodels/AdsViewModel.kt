package com.adsmanager.app.viewmodels

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.adsmanager.app.OnInterstitialAdDismiss
import com.adsmanager.app.OnRewardAdListener
import com.google.android.gms.ads.AdError

data class AdsFactory(val activity: Activity)

class AdsViewModel(private val adsFactory: AdsFactory) : ViewModel() {

    fun displayBannerAd() {

    }

    fun displayBigNativeAd() {

    }

    fun displaySmallNativeAd() {

    }

    fun displayInterstitialAd() {
        com.adsmanager.app.displayInterstitialAd(
            adsFactory.activity,
            object : OnInterstitialAdDismiss {
                override fun onInterstitialAdDismiss() {

                }
            })
    }

    fun displayRewardVideoAd() {
        com.adsmanager.app.displayRewardVideoAd(adsFactory.activity, object : OnRewardAdListener {
            override fun onAdDismissedFullScreenContent() {
                Log.d(
                    "--admob_reward_video--",
                    "onAdDismissedFullScreenContent: Success Interface"
                )
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(
                    "--admob_reward_video--",
                    "onAdFailedToShowFullScreenContent Interface: ${adError.message}"
                )
            }

            override fun onUserEarnedReward() {
                Log.d("--admob_reward_video--", "onUserEarnedReward: Success Interface")
            }

            override fun onAdNotAvailable() {
                Log.d("--admob_reward_video--", "onAdNotAvailable: Success Interface")
            }
        })
    }

    fun displayRewardInterstitialAd() {
        com.adsmanager.app.displayRewardInterstitialAd(adsFactory.activity, object : OnRewardAdListener {
            override fun onAdDismissedFullScreenContent() {
                Log.d(
                    "--admob_reward_interstitial--",
                    "onAdDismissedFullScreenContent: Success Interface"
                )
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(
                    "--admob_reward_interstitial--",
                    "onAdFailedToShowFullScreenContent Interface: ${adError.message}"
                )
            }

            override fun onUserEarnedReward() {
                Log.d(
                    "--admob_reward_interstitial--",
                    "onUserEarnedReward: Success Interface"
                )
            }

            override fun onAdNotAvailable() {
                Log.d(
                    "--admob_reward_interstitial--",
                    "onAdNotAvailable: Success Interface"
                )
            }
        })
    }
}
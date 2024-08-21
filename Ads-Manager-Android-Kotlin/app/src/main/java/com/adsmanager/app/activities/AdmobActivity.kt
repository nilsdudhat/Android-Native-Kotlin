package com.adsmanager.app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.adsmanager.app.OnInterstitialAdDismiss
import com.adsmanager.app.OnRewardAdListener
import com.adsmanager.app.R
import com.adsmanager.app.activities.admob.BannerActivity
import com.adsmanager.app.activities.admob.BigNativeActivity
import com.adsmanager.app.activities.admob.SmallNativeActivity
import com.adsmanager.app.databinding.ActivityAdmobBinding
import com.adsmanager.app.displayInterstitialAd
import com.adsmanager.app.displayRewardInterstitialAd
import com.adsmanager.app.displayRewardVideoAd
import com.adsmanager.app.loadAdmobAds
import com.adsmanager.app.utils.createFactory
import com.adsmanager.app.viewmodels.AdmobActivityFactory
import com.adsmanager.app.viewmodels.AdmobActivityViewModel
import com.adsmanager.app.viewmodels.AdsFactory
import com.adsmanager.app.viewmodels.AdsViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdmobActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdmobBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdmobBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.adsViewModel = ViewModelProvider(
            this,
            AdsViewModel(AdsFactory(this)).createFactory()
        )[AdsViewModel::class.java]

        binding.viewModel = ViewModelProvider(
            this,
            AdmobActivityViewModel(AdmobActivityFactory(this)).createFactory()
        )[AdmobActivityViewModel::class.java]

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@AdmobActivity) {
                loadAdmobAds(this@AdmobActivity)
            }
        }
    }
}
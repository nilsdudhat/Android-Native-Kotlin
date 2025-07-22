package com.belive.dating.ads.custom

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.AdCustomInterstitialBinding
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.noAnimation
import com.belive.dating.extensions.openBrowser

class CustomInterstitialAdActivity : NetworkReceiverActivity() {

    val binding by lazy {
        AdCustomInterstitialBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val customNative = CustomAds.customAd!!

        binding.posterUrl = getGistPrefs().customImagesURL + customNative.posterUrl
        binding.logoUrl = getGistPrefs().customImagesURL + customNative.logoUrl
        binding.title = customNative.title
        binding.body = customNative.body
        binding.buttonText = customNative.btnText

        binding.btnClose.setOnClickListener {
            ManageAds.changeInterstitialAdPriority()

            CustomAds.customAd = null
            CustomAds.loadAds()

            finish()
            noAnimation()
            CustomAds.onFullScreenAdDismissed?.invoke()
            CustomAds.onFullScreenAdDismissed = null
        }
        binding.btnOpen.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }
        binding.btnInfo.setOnClickListener {
            getKoinActivity().openBrowser(customNative.infoUrl)
        }
        binding.root.setOnClickListener {
            getKoinActivity().openBrowser(customNative.redirectUrl)
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
    }
}
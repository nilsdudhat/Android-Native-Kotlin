package com.adsmanager.app.viewmodels

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.adsmanager.app.activities.admob.BannerActivity
import com.adsmanager.app.activities.admob.BigNativeActivity

data class AdmobActivityFactory(val activity: Activity)

class AdmobActivityViewModel(private val factory: AdmobActivityFactory) : ViewModel() {

    fun btnBannerClicked() {
        val intent = Intent(factory.activity, BannerActivity::class.java)
        factory.activity.startActivity(intent)
    }

    fun btnSmallNativeClicked() {
        val intent = Intent(factory.activity, BigNativeActivity::class.java)
        factory.activity.startActivity(intent)
    }

    fun btnBigNativeClicked() {
        val intent = Intent(factory.activity, BigNativeActivity::class.java)
        factory.activity.startActivity(intent)
    }
}
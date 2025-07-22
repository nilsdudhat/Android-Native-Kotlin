package com.belive.dating

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.di.activityModule
import com.belive.dating.di.adsSettingsModule
import com.belive.dating.di.authenticationHelperModule
import com.belive.dating.di.deepLinkViewModels
import com.belive.dating.di.gistModule
import com.belive.dating.di.glideModule
import com.belive.dating.di.googleVisionModule
import com.belive.dating.di.mainViewModel
import com.belive.dating.di.nsfwModule
import com.belive.dating.di.paywallViewModels
import com.belive.dating.di.preferenceModule
import com.belive.dating.di.splashDataModule
import com.belive.dating.di.userModule
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class MainApplication : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    override fun onCreate() {
        super<Application>.onCreate()

        registerActivityLifecycleCallbacks(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Set the app to always use dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // Initializing Koin Dependency Modules
        startKoin {
            printLogger(Level.DEBUG)
            androidContext(this@MainApplication)
            modules(
                listOf(
                    activityModule,
                    preferenceModule,
                    splashDataModule,
                    adsSettingsModule,
                    nsfwModule,
                    glideModule,
                    gistModule,
                    authenticationHelperModule,
                    mainViewModel,
                    userModule,
                    googleVisionModule,
                    deepLinkViewModels,
                    paywallViewModels,
                )
            )
        }

        // Initializing Firebase
        FirebaseApp.initializeApp(this)

        // Disabling Firebase for Debug App
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        MultiDex.install(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        AdmobAds.showAppOpenAd()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}
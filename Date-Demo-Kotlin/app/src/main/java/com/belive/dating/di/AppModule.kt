package com.belive.dating.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.belive.dating.helpers.helper_functions.splash_data.SplashData
import com.bumptech.glide.Glide
import org.koin.dsl.module

val activityModule = module {
    single<AppCompatActivity> { throw IllegalStateException("AppCompatActivity not initialized yet") }
}

val glideModule = module {
    single {
        Glide.with(get<Context>())
    }
}

val splashDataModule = module {
    single {
        SplashData
    }
}
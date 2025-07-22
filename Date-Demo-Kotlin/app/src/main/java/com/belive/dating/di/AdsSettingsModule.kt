package com.belive.dating.di

import com.belive.dating.api.ads_settings.AdsSettingClient
import com.belive.dating.api.ads_settings.AdsSettingsRepository
import com.belive.dating.api.ads_settings.AdsSettingsService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val adsSettingsModule = module {
    single(named("ads-settings")) {
        AdsSettingClient.getAdsSettingInstance()
    }
    single {
        get<Retrofit>(named("ads-settings")).create(AdsSettingsService::class.java)
    }
    single {
        AdsSettingsRepository()
    }
}
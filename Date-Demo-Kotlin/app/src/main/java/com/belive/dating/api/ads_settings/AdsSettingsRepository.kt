package com.belive.dating.api.ads_settings

import com.belive.dating.extensions.getKoinObject
import com.google.gson.JsonObject
import org.koin.core.component.KoinComponent
import retrofit2.Response

class AdsSettingsRepository : KoinComponent {
    private val adsSettingsService = getKoinObject().get<AdsSettingsService>()

    suspend fun getAdsSettings(): Response<JsonObject> = adsSettingsService.getAdsSettings()
}
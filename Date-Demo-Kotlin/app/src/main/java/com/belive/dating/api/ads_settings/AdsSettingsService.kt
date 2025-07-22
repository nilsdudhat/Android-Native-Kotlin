package com.belive.dating.api.ads_settings

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET

interface AdsSettingsService {

    @GET("ads-setting")
    suspend fun getAdsSettings(): Response<JsonObject>
}
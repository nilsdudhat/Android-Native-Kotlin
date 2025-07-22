package com.belive.dating.api.country

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface CountryService {

    @GET
    suspend fun getCountryData(@Url url: String): Response<JsonObject>
}
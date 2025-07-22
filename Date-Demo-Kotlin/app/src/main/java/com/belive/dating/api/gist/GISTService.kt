package com.belive.dating.api.gist

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET

interface GISTService {

    @GET("com.belive.dating")
    suspend fun getRaw(): Response<JsonObject>
}
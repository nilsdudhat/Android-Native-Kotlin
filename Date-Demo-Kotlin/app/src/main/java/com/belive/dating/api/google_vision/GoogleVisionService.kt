package com.belive.dating.api.google_vision

import com.belive.dating.api.google_vision.models.CombinedRequest
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleVisionService {

    @Headers("Content-Type: application/json")
    @POST("v1/images:annotate")
    suspend fun annotateImage(
        @Body requestBody: CombinedRequest,
        @Query("key") apiKey: String,
    ): Response<JsonObject>
}
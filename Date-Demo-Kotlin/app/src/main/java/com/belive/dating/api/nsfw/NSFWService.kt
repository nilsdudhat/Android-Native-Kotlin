package com.belive.dating.api.nsfw

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NSFWService {

    @Multipart
    @POST("api/verify")
    suspend fun verifyNSFW(
        @Part image: MultipartBody.Part): Response<JsonObject>
}
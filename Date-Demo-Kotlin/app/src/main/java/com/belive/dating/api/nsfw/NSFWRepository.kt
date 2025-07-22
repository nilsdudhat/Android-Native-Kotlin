package com.belive.dating.api.nsfw

import com.belive.dating.extensions.getKoinObject
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import org.koin.core.component.KoinComponent
import retrofit2.Response

class NSFWRepository : KoinComponent {
    private val nsfwService = getKoinObject().get<NSFWService>()

    suspend fun verifyNSFW(image: MultipartBody.Part): Response<JsonObject> = nsfwService.verifyNSFW(image)
}
package com.belive.dating.api.google_vision

import com.belive.dating.api.google_vision.models.CombinedRequest
import com.belive.dating.extensions.getKoinObject
import com.google.gson.JsonObject
import org.koin.core.component.KoinComponent
import retrofit2.Response

class GoogleVisionRepository : KoinComponent {
    private val googleVisionService = getKoinObject().get<GoogleVisionService>()

    suspend fun annotateImage(requestBody: CombinedRequest, apiKey: String): Response<JsonObject> =
        googleVisionService.annotateImage(requestBody, apiKey)
}
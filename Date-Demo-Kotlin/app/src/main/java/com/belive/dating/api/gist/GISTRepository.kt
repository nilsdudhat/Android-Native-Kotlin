package com.belive.dating.api.gist

import com.belive.dating.extensions.getKoinObject
import com.google.gson.JsonObject
import org.koin.core.component.KoinComponent
import retrofit2.Response

class GISTRepository : KoinComponent {
    private val gistService = getKoinObject().get<GISTService>()

    suspend fun getRaw(): Response<JsonObject> = gistService.getRaw()
}
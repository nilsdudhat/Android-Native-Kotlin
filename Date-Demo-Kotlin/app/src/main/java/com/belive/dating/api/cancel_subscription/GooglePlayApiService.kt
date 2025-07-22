package com.belive.dating.api.cancel_subscription

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

interface GooglePlayApiService {
    @POST("applications/{packageName}/purchases/subscriptions/{subscriptionId}/tokens/{token}:cancel")
    suspend fun cancelSubscription(
        @Path("packageName") packageName: String,
        @Path("subscriptionId") subscriptionId: String?,
        @Path("token") token: String?,
    ): Response<Void>
}
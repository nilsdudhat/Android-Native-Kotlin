package com.belive.dating.api.introduction

import com.belive.dating.api.introduction.models.InterestsResponse
import com.belive.dating.api.introduction.models.LanguagesResponse
import com.belive.dating.api.introduction.models.RelationshipGoalsResponse
import com.belive.dating.api.introduction.models.SexualOrientationResponse
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.safeApiCallResponse
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.component.KoinComponent
import retrofit2.Response

class IntroductionRepository() : KoinComponent {

    private val introductionService = getKoinObject().get<IntroductionService>()

    suspend fun signIn(email: String?, deviceId: String?, fcmToken: String?): Response<JsonObject> = safeApiCallResponse {
        introductionService.signIn(email = email, deviceId = deviceId, fcmToken = fcmToken)
    }

    suspend fun getAllSexualOrientations(): Response<SexualOrientationResponse> = safeApiCallResponse {
        introductionService.getAllSexualOrientations()
    }

    suspend fun getAllLanguages(): Response<LanguagesResponse> = safeApiCallResponse {
        introductionService.getAllLanguages()
    }

    suspend fun getAllInterests(): Response<InterestsResponse> = safeApiCallResponse {
        introductionService.getAllInterests()
    }

    suspend fun getAllRelationshipGoals(): Response<RelationshipGoalsResponse> = safeApiCallResponse {
        introductionService.getAllRelationshipGoals()
    }

    suspend fun signUp(
        fullName: RequestBody?,
        email: RequestBody?,
        gender: RequestBody?,
        birthDate: RequestBody?,
        seeingInterest: RequestBody?,
        orientationId: RequestBody?,
        myInterests: RequestBody?,
        relationshipGoal: RequestBody?,
        loginType: RequestBody?,
        deviceToken: RequestBody?,
        fcmToken: RequestBody?,
        versionName: RequestBody?,
        images: List<MultipartBody.Part>,
        selfie: List<MultipartBody.Part>,
    ): Response<JsonObject> = safeApiCallResponse {
        introductionService.signUp(
            type = "android".toRequestBody("text/plain".toMediaTypeOrNull()),
            fullName = fullName,
            email = email,
            gender = gender,
            birthDate = birthDate,
            seeingInterest = seeingInterest,
            orientationId = orientationId,
            myInterests = myInterests,
            relationshipGoal = relationshipGoal,
            loginType = loginType,
            deviceToken = deviceToken,
            fcmToken = fcmToken,
            versionName = versionName,
            images = images,
            selfie = selfie,
        )
    }

    suspend fun validateText(text: String?): Response<JsonObject> = safeApiCallResponse {
        introductionService.validateText(text)
    }
}
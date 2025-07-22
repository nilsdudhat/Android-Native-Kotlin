package com.belive.dating.api.introduction

import com.belive.dating.api.introduction.models.EducationResponse
import com.belive.dating.api.introduction.models.FamilyPlanResponse
import com.belive.dating.api.introduction.models.InterestsResponse
import com.belive.dating.api.introduction.models.LanguagesResponse
import com.belive.dating.api.introduction.models.MaritalStatusResponse
import com.belive.dating.api.introduction.models.RelationshipGoalsResponse
import com.belive.dating.api.introduction.models.ReligionsResponse
import com.belive.dating.api.introduction.models.SexualOrientationResponse
import com.belive.dating.api.introduction.models.ZodiacResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface IntroductionService {

    @FormUrlEncoded
    @POST("splash-user")
    suspend fun splashUser(
        @FieldMap map: MutableMap<String, String?>,
    ): Response<JsonObject>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("sign-in")
    suspend fun signIn(
        @Field("email") email: String?,
        @Field("device_token") deviceId: String?,
        @Field("fcm_token") fcmToken: String?,
    ): Response<JsonObject>

    @GET("all-orientation")
    suspend fun getAllSexualOrientations(): Response<SexualOrientationResponse>

    @GET("knownlanguage")
    suspend fun getAllLanguages(): Response<LanguagesResponse>

    @GET("zodiac")
    suspend fun getAllZodiac(): Response<ZodiacResponse>

    @GET("religion")
    suspend fun getAllReligions(): Response<ReligionsResponse>

    @GET("maritalstatus")
    suspend fun getAllMaritalStatus(): Response<MaritalStatusResponse>

    @GET("familyplan")
    suspend fun getAllFamilyPlan(): Response<FamilyPlanResponse>

    @GET("education")
    suspend fun getAllEducation(): Response<EducationResponse>

    @GET("all-interest")
    suspend fun getAllInterests(): Response<InterestsResponse>

    @GET("all-looking-for")
    suspend fun getAllRelationshipGoals(): Response<RelationshipGoalsResponse>

    @Multipart
    @POST("sign-up")
    suspend fun signUp(
        @Part("type") type: RequestBody?,
        @Part("fullname") fullName: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("birth_date") birthDate: RequestBody?,
        @Part("seeing_interest") seeingInterest: RequestBody?,
        @Part("orientation_id") orientationId: RequestBody?,
        @Part("my_interests") myInterests: RequestBody?,
        @Part("looking_for") relationshipGoal: RequestBody?,
        @Part("login_type") loginType: RequestBody?,
        @Part("device_token") deviceToken: RequestBody?,
        @Part("fcm_token") fcmToken: RequestBody?,
        @Part("create_app_version") versionName: RequestBody?,
        @Part images: List<MultipartBody.Part>,
        @Part selfie: List<MultipartBody.Part>,
    ): Response<JsonObject>

    @GET("verify-text")
    suspend fun validateText(@Query("text") text: String?): Response<JsonObject>
}
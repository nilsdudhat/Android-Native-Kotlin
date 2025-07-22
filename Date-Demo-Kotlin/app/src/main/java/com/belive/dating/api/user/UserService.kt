package com.belive.dating.api.user

import com.belive.dating.api.user.models.add_location.AddLocationModel
import com.belive.dating.api.user.models.ai.AIProfilesResponse
import com.belive.dating.api.user.models.basic.BasicResponse
import com.belive.dating.api.user.models.diamond_history.DiamondHistoryResponse
import com.belive.dating.api.user.models.diamond_question.DiamondQuestionResponse
import com.belive.dating.api.user.models.edit_images.EditImagesResponse
import com.belive.dating.api.user.models.filters.FiltersResponse
import com.belive.dating.api.user.models.home.RewindDeductResponse
import com.belive.dating.api.user.models.home_profiles.HomeUsersResponse
import com.belive.dating.api.user.models.images.ImagesResponse
import com.belive.dating.api.user.models.like_profile.LikeProfileResponse
import com.belive.dating.api.user.models.liked_me_profiles.LikedMeProfilesResponse
import com.belive.dating.api.user.models.my_locations.MyLocationsResponse
import com.belive.dating.api.user.models.notification.NotificationResponse
import com.belive.dating.api.user.models.notification_settings.NotificationSettingsResponse
import com.belive.dating.api.user.models.payment.PaymentResponse
import com.belive.dating.api.user.models.profile_balances.ProfileBalanceResponse
import com.belive.dating.api.user.models.rewind_like_conversion.RewindLikeConversionResponse
import com.belive.dating.api.user.models.search_user.SearchUserResponse
import com.belive.dating.api.user.models.user.UserResponse
import com.belive.dating.api.user.models.user_details.UserDetailsResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @PATCH("update-user")
    suspend fun updateUserDetails(
        @Body locationJson: JsonObject,
    ): Response<UserResponse>

    @POST("logout")
    suspend fun logout(): Response<JsonObject>

    @POST("delete-user")
    suspend fun deleteUser(): Response<JsonObject>

    @GET("home-users-profile")
    suspend fun getHomeProfiles(
        @Query("country_code") countryCode: String,
        @Query("ignore") ignoreIds: String?,
    ): Response<HomeUsersResponse>

    @GET("ai-matcher-like-superlike")
    suspend fun getAIProfiles(
        @Query("page") page: Int,
        @Query("country_code") countryCode: String,
        @Query("flag") flag: String,
    ): Response<AIProfilesResponse>

    @GET("ai-matcher-like-superlike")
    suspend fun getLikeProfiles(
        @Query("page") page: Int,
        @Query("country_code") countryCode: String,
        @Query("flag") flag: String,
    ): Response<LikedMeProfilesResponse>

    @POST("rewinds-deduct")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    @FormUrlEncoded
    suspend fun deductRewind(
        @Field("profile_id") profileID: Int,
        @Field("deductFrom") deductFrom: String?,
    ): Response<RewindDeductResponse>

    @POST("like")
    suspend fun likeProfile(
        @Body queryJson: JsonObject,
    ): Response<LikeProfileResponse>

    @GET("skip-profile/{profile_id}")
    suspend fun skipProfile(
        @Path("profile_id") profileId: Int,
    ): Response<JsonObject>

    @GET("profile/{user_id}")
    suspend fun readProfile(
        @Path("user_id") userId: Int,
    ): Response<JsonObject>

    @PATCH("notification/{notification_id}")
    suspend fun readNotification(
        @Path("notification_id") notificationId: Int,
    ): Response<JsonObject>

    @GET("balances")
    suspend fun getProfileBalance(): Response<ProfileBalanceResponse>

    @GET("settings")
    suspend fun getFiltersData(): Response<FiltersResponse>

    @GET("my-locations")
    suspend fun getMyLocations(): Response<MyLocationsResponse>

    @DELETE("delete-location")
    suspend fun deleteLocations(@Query("ids") delete: String): Response<MyLocationsResponse>

    @FormUrlEncoded
    @POST("add-location")
    suspend fun addNewLocation(
        @FieldMap locationMap: MutableMap<String, String>,
    ): Response<AddLocationModel>

    @FormUrlEncoded
    @POST("update-setting")
    suspend fun updateFilters(
        @FieldMap map: MutableMap<String, Any?>,
    ): Response<JsonObject>

    @GET("user-profile/{profile_id}")
    suspend fun getUserDetails(
        @Path("profile_id") profileId: Int,
    ): Response<UserDetailsResponse>

    @GET("basic")
    suspend fun getAllBasics(
        @Query("category") category: String,
    ): Response<BasicResponse>

    @FormUrlEncoded
    @POST("purchase-subscribe-topup") // is_topup: = 0 - subscription, 1 - topup
    suspend fun makePurchase(
        @FieldMap map: MutableMap<String, Any?>,
    ): Response<PaymentResponse>

    @POST("cancel-purchase-subscribe")
    suspend fun cancelPurchase(): Response<JsonObject>

    @GET("rewind-like-diamond-details") // flag = like, rewind
    suspend fun getRewindLikeConversionDetails(
        @Query("flag") flag: String,
    ): Response<RewindLikeConversionResponse>

    @FormUrlEncoded
    @POST("diamond-purchase-like-rewind")
    suspend fun redeemRewindLike(
        @FieldMap map: MutableMap<String, Any?>,
    ): Response<JsonObject>

    @GET("diamond-questions")
    suspend fun getDiamondQuestions(): Response<DiamondQuestionResponse>

    @FormUrlEncoded
    @POST("claim-diamond")
    suspend fun claimDiamond(
        @FieldMap map: MutableMap<String, Any?>,
    ): Response<JsonObject>

    @GET("history/diamond")
    suspend fun getDiamondHistory(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
    ): Response<DiamondHistoryResponse>

    @POST("block-profile")
    suspend fun blockUser(
        @Body map: MutableMap<String, Any?>,
    ): Response<JsonObject>

    @POST("report-user")
    suspend fun reportUser(
        @Body body: JsonObject,
    ): Response<JsonObject>

    @GET("history/notification")
    suspend fun getNotificationHistory(): Response<NotificationResponse>

    @POST("boosts-deduct")
    suspend fun deductBoost(): Response<JsonObject>

    @GET("search-home-profile")
    suspend fun searchUsers(
        @Query("search") search: String,
    ): Response<SearchUserResponse>

    @Multipart
    @POST("update-profile")
    suspend fun updateImages(
        @Part("eImg_1") img1: RequestBody?,
        @Part("eImg_2") img2: RequestBody?,
        @Part("eImg_3") img3: RequestBody?,
        @Part("eImg_4") img4: RequestBody?,
        @Part("eImg_5") img5: RequestBody?,
        @Part("eImg_6") img6: RequestBody?,
        @Part images: List<MultipartBody.Part>?,
        @Part selfie: MultipartBody.Part? = null,
    ): Response<EditImagesResponse>

    @GET("alert-settings")
    suspend fun getNotificationSettings(): Response<NotificationSettingsResponse>

    @POST("alert-settings")
    suspend fun updateNotificationSettings(
        @Body json: JsonObject
    ): Response<JsonObject>

    @GET("images")
    suspend fun getImages(): Response<ImagesResponse>
}
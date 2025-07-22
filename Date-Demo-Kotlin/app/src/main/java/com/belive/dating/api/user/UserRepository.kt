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
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.safeApiCallResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.core.component.KoinComponent
import retrofit2.Response

class UserRepository : KoinComponent {

    private val userService = getKoinObject().get<UserService>()

    suspend fun updateUserDetails(userDetailsJson: JsonObject): Response<UserResponse> = safeApiCallResponse {
        userService.updateUserDetails(userDetailsJson)
    }

    suspend fun logout(): Response<JsonObject> = safeApiCallResponse {
        userService.logout()
    }

    suspend fun deleteUser(): Response<JsonObject> = safeApiCallResponse {
        userService.deleteUser()
    }

    suspend fun getHomeProfiles(countryCode: String, ignoreIds: String?): Response<HomeUsersResponse> = safeApiCallResponse {
        userService.getHomeProfiles(countryCode, ignoreIds)
    }

    suspend fun getAIProfiles(page: Int, countryCode: String, flag: String): Response<AIProfilesResponse> = safeApiCallResponse {
        userService.getAIProfiles(page, countryCode, flag)
    }

    suspend fun getLikeProfiles(page: Int, countryCode: String, flag: String): Response<LikedMeProfilesResponse> = safeApiCallResponse {
        userService.getLikeProfiles(page, countryCode, flag)
    }

    suspend fun deductRewind(profileID: Int, deductFrom: String?): Response<RewindDeductResponse> = safeApiCallResponse {
        userService.deductRewind(profileID, deductFrom)
    }

    suspend fun skipProfile(profileID: Int): Response<JsonObject> = safeApiCallResponse {
        userService.skipProfile(profileID)
    }

    suspend fun likeProfile(queryJson: JsonObject): Response<LikeProfileResponse> = safeApiCallResponse {
        userService.likeProfile(queryJson)
    }

    suspend fun readProfile(userId: Int) = safeApiCallResponse {
        userService.readProfile(userId)
    }

    suspend fun readNotification(id: Int) = safeApiCallResponse {
        userService.readNotification(id)
    }

    suspend fun getProfileBalance(): Response<ProfileBalanceResponse> = safeApiCallResponse {
        userService.getProfileBalance()
    }

    suspend fun getFiltersData(): Response<FiltersResponse> = safeApiCallResponse {
        userService.getFiltersData()
    }

    suspend fun getMyLocations(): Response<MyLocationsResponse> = safeApiCallResponse {
        userService.getMyLocations()
    }

    suspend fun deleteLocations(delete: String): Response<MyLocationsResponse> = safeApiCallResponse {
        userService.deleteLocations(delete)
    }

    suspend fun addNewLocation(locationMap: MutableMap<String, String>): Response<AddLocationModel> = safeApiCallResponse {
        userService.addNewLocation(locationMap)
    }

    suspend fun updateFilters(locationMap: MutableMap<String, Any?>): Response<JsonObject> = safeApiCallResponse {
        userService.updateFilters(locationMap)
    }

    suspend fun getUserDetails(userID: Int): Response<UserDetailsResponse> = safeApiCallResponse {
        userService.getUserDetails(userID)
    }

    suspend fun getAllBasics(category: String): Response<BasicResponse> = safeApiCallResponse {
        userService.getAllBasics(category)
    }

    suspend fun makePurchase(map: MutableMap<String, Any?>): Response<PaymentResponse> = safeApiCallResponse {
        userService.makePurchase(map)
    }

    suspend fun cancelPurchase(): Response<JsonObject> = safeApiCallResponse {
        userService.cancelPurchase()
    }

    suspend fun getRewindLikeConversionDetails(flag: String): Response<RewindLikeConversionResponse> = safeApiCallResponse {
        userService.getRewindLikeConversionDetails(flag)
    }

    suspend fun redeemRewindLike(map: MutableMap<String, Any?>): Response<JsonObject> = safeApiCallResponse {
        userService.redeemRewindLike(map)
    }

    suspend fun getDiamondQuestions(): Response<DiamondQuestionResponse> = safeApiCallResponse {
        userService.getDiamondQuestions()
    }

    suspend fun claimDiamond(map: MutableMap<String, Any?>): Response<JsonObject> = safeApiCallResponse {
        userService.claimDiamond(map)
    }

    suspend fun getDiamondHistory(page: Int?, limit: Int?): Response<DiamondHistoryResponse> = safeApiCallResponse {
        userService.getDiamondHistory(page, limit)
    }

    suspend fun blockUser(map: MutableMap<String, Any?>): Response<JsonObject> = safeApiCallResponse {
        userService.blockUser(map)
    }

    suspend fun reportUser(rawJson: JsonObject): Response<JsonObject> = safeApiCallResponse {
        userService.reportUser(rawJson)
    }

    suspend fun getNotificationHistory(): Response<NotificationResponse> = safeApiCallResponse {
        userService.getNotificationHistory()
    }

    suspend fun deductBoost(): Response<JsonObject> = safeApiCallResponse {
        userService.deductBoost()
    }

    suspend fun searchUsers(search: String): Response<SearchUserResponse> = safeApiCallResponse {
        userService.searchUsers(search)
    }

    suspend fun updateImages(
        img1: RequestBody?,
        img2: RequestBody?,
        img3: RequestBody?,
        img4: RequestBody?,
        img5: RequestBody?,
        img6: RequestBody?,
        images: List<MultipartBody.Part>?,
        selfie: MultipartBody.Part? = null,
    ): Response<EditImagesResponse> = safeApiCallResponse {
        userService.updateImages(img1, img2, img3, img4, img5, img6, images, selfie)
    }

    suspend fun getNotificationSettings(): Response<NotificationSettingsResponse> = safeApiCallResponse {
        userService.getNotificationSettings()
    }

    suspend fun updateNotificationSettings(json: JsonObject): Response<JsonObject> = safeApiCallResponse {
        userService.updateNotificationSettings(json)
    }

    suspend fun getImages(): Response<ImagesResponse> = safeApiCallResponse {
        userService.getImages()
    }
}
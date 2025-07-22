package com.belive.dating.preferences.pref_helpers

import android.content.SharedPreferences
import com.belive.dating.api.user.models.chat_file.FileDownloadModel
import com.belive.dating.api.user.models.chat_file.FileUploadModel
import com.belive.dating.api.user.models.user.Basic
import com.belive.dating.api.user.models.user.Height
import com.belive.dating.api.user.models.user.Orientation
import com.belive.dating.api.user.models.user.RelationshipGoal
import com.belive.dating.api.user.models.user.User
import com.belive.dating.api.user.models.user.UserImage
import com.belive.dating.constants.IntroductionConstants
import com.belive.dating.constants.PrefConst
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.preferences.pref_utils.PrefUtils

object UserPrefs {

    private val prefUtils = getKoinObject().get<PrefUtils>()

    fun getPreference(): SharedPreferences {
        return prefUtils.getPrefs()
    }

    var userToken: String? = null
        get() {
            return prefUtils.getString(PrefConst.USER_TOKEN)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.USER_TOKEN, value)
        }

    var lastVerificationDate: String? = null
        get() {
            return prefUtils.getString(PrefConst.LAST_VERIFICATION_DATE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.LAST_VERIFICATION_DATE, value)
        }

    var remainingVerificationAttempts: Int = IntroductionConstants.SELFIE_LIMIT
        get() {
            return prefUtils.getInt(PrefConst.REMAINING_VERIFICATION_ATTEMPTS, IntroductionConstants.SELFIE_LIMIT)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.REMAINING_VERIFICATION_ATTEMPTS, value)
        }

    var userId = -1
        get() {
            return prefUtils.getInt(PrefConst.USER_ID, -1)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.USER_ID, value)
        }

    var fcmToken: String? = null
        get() {
            return prefUtils.getString(PrefConst.FCM_TOKEN)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.FCM_TOKEN, value)
        }

    var fullName: String? = null
        get() {
            return prefUtils.getString(PrefConst.FULL_NAME)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.FULL_NAME, value)
        }

    var emailId: String? = null
        get() {
            return prefUtils.getString(PrefConst.EMAIL_ID)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.EMAIL_ID, value)
        }

    var gender: String? = null
        get() {
            return prefUtils.getString(PrefConst.GENDER)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.GENDER, value)
        }

    var birthDate: String? = null
        get() {
            return prefUtils.getString(PrefConst.BIRTH_DATE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.BIRTH_DATE, value)
        }

    var height: Height?
        get() {
            val height: Height? = if (prefUtils.getString(PrefConst.HEIGHT) == null) {
                null
            } else {
                prefUtils.getString(PrefConst.HEIGHT)!!.fromJson()
            }
            return height
        }
        set(value) {
            prefUtils.setString(PrefConst.HEIGHT, gsonString(value))
        }

    var personalityTypes: List<String>? = null
        get() {
            return prefUtils.getStringList(PrefConst.PERSONALITY_TYPES)
        }
        set(value) {
            field = value
            prefUtils.setStringList(PrefConst.PERSONALITY_TYPES, value)
        }

    var communicationTypes: List<String>? = null
        get() {
            return prefUtils.getStringList(PrefConst.COMMUNICATION_TYPES)
        }
        set(value) {
            field = value
            prefUtils.setStringList(PrefConst.COMMUNICATION_TYPES, value)
        }

    var loveTypes: List<String>? = null
        get() {
            return prefUtils.getStringList(PrefConst.LOVE_TYPES)
        }
        set(value) {
            field = value
            prefUtils.setStringList(PrefConst.LOVE_TYPES, value)
        }

    var pet: String? = null
        get() {
            return prefUtils.getString(PrefConst.PET)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.PET, value)
        }

    var preferredDiet: String? = null
        get() {
            return prefUtils.getString(PrefConst.PREFERRED_DIET)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.PREFERRED_DIET, value)
        }

    var drinking: String? = null
        get() {
            return prefUtils.getString(PrefConst.DRINKING)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.DRINKING, value)
        }

    var smoking: String? = null
        get() {
            return prefUtils.getString(PrefConst.SMOKING)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.SMOKING, value)
        }

    var workout: String? = null
        get() {
            return prefUtils.getString(PrefConst.WORKOUT)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.WORKOUT, value)
        }

    var socialStatus: String? = null
        get() {
            return prefUtils.getString(PrefConst.SOCIAL_STATUS)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.SOCIAL_STATUS, value)
        }

    var sleepingHabit: String? = null
        get() {
            return prefUtils.getString(PrefConst.SLEEPING_HABIT)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.SLEEPING_HABIT, value)
        }

    var knownLanguages: List<String>? = null
        get() {
            return prefUtils.getStringList(PrefConst.KNOWN_LANGUAGES)
        }
        set(value) {
            field = value
            prefUtils.setStringList(PrefConst.KNOWN_LANGUAGES, value)
        }

    var myInterests: List<String>? = null
        get() {
            return prefUtils.getStringList(PrefConst.MY_INTERESTS)
        }
        set(value) {
            field = value
            prefUtils.setStringList(PrefConst.MY_INTERESTS, value)
        }

    var age = -1
        get() {
            return prefUtils.getInt(PrefConst.AGE, -1)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.AGE, value)
        }

    var oppositeGender: String? = null
        get() {
            return prefUtils.getString(PrefConst.OPPOSITE_GENDER)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.OPPOSITE_GENDER, value)
        }

    var completeProfilePercentage = 0
        get() {
            return prefUtils.getInt(PrefConst.COMPLETE_PROFILE_PERCENTAGE, 0)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.COMPLETE_PROFILE_PERCENTAGE, value)
        }

    var userImages: List<UserImage>?
        get() {
            try {
                val userImageArrayList: List<UserImage>? = if (prefUtils.getString(PrefConst.USER_IMAGES) == null) {
                    null
                } else {
                    prefUtils.getString(PrefConst.USER_IMAGES)!!.fromJson()
                }
                return userImageArrayList
            } catch (e: Exception) {
                return null
            }
        }
        set(value) {
            prefUtils.setString(PrefConst.USER_IMAGES, gsonString(value))
        }

    var runningFileUploadList: ArrayList<FileUploadModel>?
        get() {
            try {
                val runningFileUploadList: ArrayList<FileUploadModel>? = if (prefUtils.getString(PrefConst.RUNNING_FILE_UPLOAD_LIST) == null) {
                    null
                } else {
                    prefUtils.getString(PrefConst.RUNNING_FILE_UPLOAD_LIST)!!.fromJson()
                }
                return runningFileUploadList
            } catch (e: Exception) {
                return null
            }
        }
        set(value) {
            prefUtils.setString(PrefConst.RUNNING_FILE_UPLOAD_LIST, gsonString(value))
        }

    var runningFileDownloadList: ArrayList<FileDownloadModel>?
        get() {
            try {
                val runningFileDownloadList: ArrayList<FileDownloadModel>? = if (prefUtils.getString(PrefConst.RUNNING_FILE_DOWNLOAD_LIST) == null) {
                    null
                } else {
                    prefUtils.getString(PrefConst.RUNNING_FILE_DOWNLOAD_LIST)!!.fromJson()
                }
                return runningFileDownloadList
            } catch (e: Exception) {
                return null
            }
        }
        set(value) {
            prefUtils.setString(PrefConst.RUNNING_FILE_DOWNLOAD_LIST, gsonString(value))
        }

    var sexualOrientation: Orientation?
        get() {
            val orientation: Orientation? = if (prefUtils.getString(PrefConst.SEXUAL_ORIENTATION) == null) {
                null
            } else {
                prefUtils.getString(PrefConst.SEXUAL_ORIENTATION)!!.fromJson()
            }
            return orientation
        }
        set(value) {
            prefUtils.setString(PrefConst.SEXUAL_ORIENTATION, gsonString(value))
        }

    var relationshipGoal: RelationshipGoal?
        get() {
            val relationshipGoal: RelationshipGoal? = if (prefUtils.getString(PrefConst.RELATIONSHIP_GOAL) == null) {
                null
            } else {
                prefUtils.getString(PrefConst.RELATIONSHIP_GOAL)!!.fromJson()
            }
            return relationshipGoal
        }
        set(value) {
            prefUtils.setString(PrefConst.RELATIONSHIP_GOAL, gsonString(value))
        }

    var aboutMe: String? = null
        get() {
            return prefUtils.getString(PrefConst.ABOUT_ME)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.ABOUT_ME, value)
        }

    var hideAge: Boolean = false
        get() {
            return prefUtils.getBoolean(PrefConst.HIDE_AGE, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.HIDE_AGE, value)
        }

    var hideDistance: Boolean = false
        get() {
            return prefUtils.getBoolean(PrefConst.HIDE_DISTANCE, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.HIDE_DISTANCE, value)
        }

    var school: String? = null
        get() {
            return prefUtils.getString(PrefConst.SCHOOL)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.SCHOOL, value)
        }

    var currentDateForDailyCheck: String? = null
        get() {
            return prefUtils.getString(PrefConst.CURRENT_DATE_FOR_DAILY_CHECK)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CURRENT_DATE_FOR_DAILY_CHECK, value)
        }

    var currentDateFromAPI: Long = 0L
        get() {
            return prefUtils.getLong(PrefConst.CURRENT_DATE_FROM_API, 0L)
        }
        set(value) {
            field = value
            prefUtils.setLong(PrefConst.CURRENT_DATE_FROM_API, value)
        }

    var countryCode: String? = null
        get() {
            return prefUtils.getString(PrefConst.COUNTRY_CODE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.COUNTRY_CODE, value)
        }

    var currentLatitude: String? = null
        get() {
            return prefUtils.getString(PrefConst.CURRENT_LATITUDE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CURRENT_LATITUDE, value)
        }

    var currentLongitude: String? = null
        get() {
            return prefUtils.getString(PrefConst.CURRENT_LONGITUDE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CURRENT_LONGITUDE, value)
        }

    var currentCity: String? = null
        get() {
            return prefUtils.getString(PrefConst.CURRENT_CITY)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CURRENT_CITY, value)
        }

    var currentState: String? = null
        get() {
            return prefUtils.getString(PrefConst.CURRENT_STATE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CURRENT_STATE, value)
        }

    var currentCountry: String? = null
        get() {
            return prefUtils.getString(PrefConst.CURRENT_COUNTRY)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CURRENT_COUNTRY, value)
        }

    var customLatitude: String? = null
        get() {
            return prefUtils.getString(PrefConst.CUSTOM_LATITUDE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CUSTOM_LATITUDE, value)
        }

    var customLongitude: String? = null
        get() {
            return prefUtils.getString(PrefConst.CUSTOM_LONGITUDE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CUSTOM_LONGITUDE, value)
        }

    var customCountry: String? = null
        get() {
            return prefUtils.getString(PrefConst.CUSTOM_COUNTRY)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CUSTOM_COUNTRY, value)
        }

    var customLocationName: String? = null
        get() {
            return prefUtils.getString(PrefConst.CUSTOM_LOCATION_NAME)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.CUSTOM_LOCATION_NAME, value)
        }

    var checkInDay = 1
        get() {
            return prefUtils.getInt(PrefConst.CHECK_IND_DAY, 1)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.CHECK_IND_DAY, value)
        }

    var isDailyCheckInAvailable = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_DAILY_CHECK_IN_AVAILABLE, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_DAILY_CHECK_IN_AVAILABLE, value)
        }

    var dailyOnlineTime = 0
        get() {
            return prefUtils.getInt(PrefConst.DAILY_ONLINE_TIME, 0)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.DAILY_ONLINE_TIME, value)
        }

    var likeCount = -1
        get() {
            return prefUtils.getInt(PrefConst.LIKE_COUNT, -1)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.LIKE_COUNT, value)
        }

    var superLikeCount = -1
        get() {
            return prefUtils.getInt(PrefConst.SUPER_LIKE_COUNT, -1)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.SUPER_LIKE_COUNT, value)
        }

    var aiRemainProfileCount = -1
        get() {
            return prefUtils.getInt(PrefConst.AI_REMAIN_PROFILE_COUNT, -1)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.AI_REMAIN_PROFILE_COUNT, value)
        }

    var unreadNotificationCount = 0
        get() {
            return prefUtils.getInt(PrefConst.UNREAD_NOTIFICATION_COUNT, 0)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.UNREAD_NOTIFICATION_COUNT, value)
        }

    var boostTime = 60000f
        get() {
            return prefUtils.getFloat(PrefConst.BOOST_TIME, 60000f)
        }
        set(value) {
            field = value
            prefUtils.setFloat(PrefConst.BOOST_TIME, value)
        }

    var activePackage: String? = null
        get() {
            return prefUtils.getString(PrefConst.ACTIVE_PACKAGE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.ACTIVE_PACKAGE, value)
        }

    var boostStartTime: String? = null
        get() {
            return prefUtils.getString(PrefConst.BOOST_START_TIME)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.BOOST_START_TIME, value)
        }

    var boostEndTime: String? = null
        get() {
            return prefUtils.getString(PrefConst.BOOST_END_TIME)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.BOOST_END_TIME, value)
        }

    var purchaseStartDate: String? = null
        get() {
            return prefUtils.getString(PrefConst.PURCHASE_START_DATE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.PURCHASE_START_DATE, value)
        }

    var purchaseEndDate: String? = null
        get() {
            return prefUtils.getString(PrefConst.PURCHASE_END_DATE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.PURCHASE_END_DATE, value)
        }

    var isUnlimitedRewinds = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_UNLIMITED_REWINDS, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_UNLIMITED_REWINDS, value)
        }

    var isBoostRunning = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_BOOST_RUNNING, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_BOOST_RUNNING, value)
        }

    var isUnlimitedLikes = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_UNLIMITED_LIKES, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_UNLIMITED_LIKES, value)
        }

    var isLocationFilter = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_LOCATION_FILTER, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_LOCATION_FILTER, value)
        }

    var isAiMatchMaker = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_AI_MATCH_MAKER, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_AI_MATCH_MAKER, value)
        }

    var isLifetimeBadge = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_LIFETIME_BADGE, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_LIFETIME_BADGE, value)
        }

    var isPlatinumBadge = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_PLATINUM_BADGE, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_PLATINUM_BADGE, value)
        }

    var isGoldBadge = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_GOLD_BADGE, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_GOLD_BADGE, value)
        }

    var isNoAds = false
        get() {
            return prefUtils.getBoolean(PrefConst.IS_NO_ADS, false)
        }
        set(value) {
            field = value
            prefUtils.setBoolean(PrefConst.IS_NO_ADS, value)
        }

    var remainingLikes = 0
        get() {
            return prefUtils.getInt(PrefConst.REMAINING_LIKES, 0)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.REMAINING_LIKES, value)
        }

    var remainingSuperLikes = 0
        get() {
            return prefUtils.getInt(PrefConst.REMAINING_SUPER_LIKES, 0)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.REMAINING_SUPER_LIKES, value)
        }

    var remainingRewinds = 0
        get() {
            return prefUtils.getInt(PrefConst.REMAINING_REWINDS, 0)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.REMAINING_REWINDS, value)
        }

    var remainingBoosts = 0
        get() {
            return prefUtils.getInt(PrefConst.REMAINING_BOOSTS, 0)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.REMAINING_BOOSTS, value)
        }

    var remainingDiamonds = 0
        get() {
            return prefUtils.getInt(PrefConst.REMAINING_DIAMONDS, 0)
        }
        set(value) {
            field = value
            prefUtils.setInt(PrefConst.REMAINING_DIAMONDS, value)
        }

    var selfie: String? = null
        get() {
            return prefUtils.getString(PrefConst.SELFIE)
        }
        set(value) {
            field = value
            prefUtils.setString(PrefConst.SELFIE, value)
        }

    var zodiac: Basic?
        get() {
            val basic: Basic? = if (prefUtils.getString(PrefConst.ZODIAC) == null) {
                null
            } else {
                prefUtils.getString(PrefConst.ZODIAC)!!.fromJson()
            }
            return basic
        }
        set(value) {
            prefUtils.setString(PrefConst.ZODIAC, gsonString(value))
        }

    var education: Basic?
        get() {
            val basic: Basic? = if (prefUtils.getString(PrefConst.EDUCATION) == null) {
                null
            } else {
                prefUtils.getString(PrefConst.EDUCATION)!!.fromJson()
            }
            return basic
        }
        set(value) {
            prefUtils.setString(PrefConst.EDUCATION, gsonString(value))
        }

    var religion: Basic?
        get() {
            val basic: Basic? = if (prefUtils.getString(PrefConst.RELIGION) == null) {
                null
            } else {
                prefUtils.getString(PrefConst.RELIGION)!!.fromJson()
            }
            return basic
        }
        set(value) {
            prefUtils.setString(PrefConst.RELIGION, gsonString(value))
        }

    var maritalStatus: Basic?
        get() {
            val basic: Basic? = if (prefUtils.getString(PrefConst.MARITAL_STATUS) == null) {
                null
            } else {
                prefUtils.getString(PrefConst.MARITAL_STATUS)!!.fromJson()
            }
            return basic
        }
        set(value) {
            prefUtils.setString(PrefConst.MARITAL_STATUS, gsonString(value))
        }

    var familyPlan: Basic?
        get() {
            val basic: Basic? = if (prefUtils.getString(PrefConst.FAMILY_PLAN) == null) {
                null
            } else {
                prefUtils.getString(PrefConst.FAMILY_PLAN)!!.fromJson()
            }
            return basic
        }
        set(value) {
            prefUtils.setString(PrefConst.FAMILY_PLAN, gsonString(value))
        }

    fun updateUserPrefs(user: User) {
        userId = user.id
        fullName = user.fullName
        emailId = user.email
        gender = user.gender
        birthDate = user.birthDate
        age = user.age
        height = user.height
        completeProfilePercentage = user.completeProfilePer
        userImages = user.userImages
        sexualOrientation = user.orientation
        oppositeGender = user.oppositeGender
        relationshipGoal = user.relationshipGoal
        aboutMe = user.aboutMe
        currentLatitude = user.latitude
        currentLongitude = user.longitude
        customLatitude = user.customLatitude
        customLongitude = user.customLongitude
        likeCount = user.likeCount
        superLikeCount = user.superLikeCount
        aiRemainProfileCount = user.aiRemainProfileCount
        unreadNotificationCount = user.unreadNotification
        currentCity = user.city
        currentState = user.state
        currentCountry = user.country
        countryCode = user.countryCode
        selfie = user.selfie?.image
        zodiac = user.zodiac
        education = user.education
        religion = user.religion
        maritalStatus = user.maritalStatus
        currentDateFromAPI = user.currentDate
        hideAge = user.hideAge
        hideDistance = user.hideDistance
        pet = user.pet
        drinking = user.drinking
        smoking = user.smoking
        preferredDiet = user.preferredDiet
        workout = user.workout
        socialStatus = user.socialStatus
        sleepingHabit = user.sleepHabit

        knownLanguages = if (user.knownLanguage.isNullOrEmpty()) {
            null
        } else if (user.knownLanguage.contains(",")) {
            user.knownLanguage.split(",").toList()
        } else {
            listOf(user.knownLanguage)
        }
        personalityTypes = if (user.personalityType.isNullOrEmpty()) {
            null
        } else if (user.personalityType.contains(",")) {
            user.personalityType.split(",").toList()
        } else {
            listOf(user.personalityType)
        }
        communicationTypes = if (user.communicationType.isNullOrEmpty()) {
            null
        } else if (user.communicationType.contains(",")) {
            user.communicationType.split(",").toList()
        } else {
            listOf(user.communicationType)
        }
        loveTypes = if (user.loveType.isNullOrEmpty()) {
            null
        } else if (user.loveType.contains(",")) {
            user.loveType.split(",").toList()
        } else {
            listOf(user.loveType)
        }
        myInterests = if (user.myInterests.isNullOrEmpty()) {
            null
        } else if (user.myInterests.contains(",")) {
            user.myInterests.split(",").toList()
        } else {
            listOf(user.myInterests)
        }

        activePackage = user.purchase?.activePlan
        purchaseStartDate = user.purchase?.startDate
        purchaseEndDate = user.purchase?.endDate
        isUnlimitedLikes = user.purchase?.unlimitedLikes == true
        isUnlimitedRewinds = user.purchase?.unlimitedRewinds == true
        isLifetimeBadge = user.purchase?.lifetimeBadge == true
        isPlatinumBadge = user.purchase?.platinumBadge == true
        isGoldBadge = user.purchase?.goldBadge == true
        isLocationFilter = user.purchase?.locationFilter == true
        isAiMatchMaker = user.purchase?.aiMatchmaker == true
        isNoAds = user.purchase?.noAds == true
    }
}
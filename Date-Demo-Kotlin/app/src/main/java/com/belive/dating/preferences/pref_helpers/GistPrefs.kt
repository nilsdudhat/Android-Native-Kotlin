package com.belive.dating.preferences.pref_helpers

import com.belive.dating.constants.EncryptedPrefConst
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.preferences.pref_utils.GistPrefUtils
import com.belive.dating.helpers.helper_functions.aes.AESUtils
import com.google.gson.JsonObject

object GistPrefs {

    private val gistPrefUtils = getKoinObject().get<GistPrefUtils>()
    private val encryptedPrefsEditor = gistPrefUtils.getEncryptedPrefEditor()

    var appRedirectOtherAppStatus = gistPrefUtils.getBoolean(EncryptedPrefConst.APP_REDIRECT_OTHER_APP_STATUS, false)
        set(value) {
            field = value
            encryptedPrefsEditor.putBoolean(
                EncryptedPrefConst.APP_REDIRECT_OTHER_APP_STATUS, value
            ).apply()
        }

    var appNewPackageName = gistPrefUtils.getString(EncryptedPrefConst.APP_NEW_PACKAGE_NAME)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.APP_NEW_PACKAGE_NAME, value).apply()
        }

    var appUpdateAppDialogStatus = gistPrefUtils.getBoolean(EncryptedPrefConst.APP_UPDATE_APP_DIALOG_STATUS, false)
        set(value) {
            field = value
            encryptedPrefsEditor.putBoolean(
                EncryptedPrefConst.APP_UPDATE_APP_DIALOG_STATUS, value
            ).apply()
        }

    var appVersionCode = gistPrefUtils.getString(EncryptedPrefConst.APP_VERSION_CODE)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.APP_VERSION_CODE, value).apply()
        }

    var appUpdatePackageName = gistPrefUtils.getString(EncryptedPrefConst.APP_UPDATE_PACKAGE_NAME)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.APP_UPDATE_PACKAGE_NAME, value).apply()
        }

    var appOneSingleID = gistPrefUtils.getString(EncryptedPrefConst.APP_ONE_SINGLE_ID)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.APP_ONE_SINGLE_ID, value).apply()
        }

    var gName = gistPrefUtils.getString(EncryptedPrefConst.G_NAME)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.G_NAME, value).apply()
        }

    var fName = gistPrefUtils.getString(EncryptedPrefConst.F_NAME)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.F_NAME, value).apply()
        }

    var gcpKey = gistPrefUtils.getString(EncryptedPrefConst.GCP_KEY)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.GCP_KEY, value).apply()
        }

    var nsfwKey = gistPrefUtils.getString(EncryptedPrefConst.NSFW_KEY)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.NSFW_KEY, value).apply()
        }

    var revenueCatKey = gistPrefUtils.getString(EncryptedPrefConst.REVENUE_CAT_KEY)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.REVENUE_CAT_KEY, value).apply()
        }

    var mixpanelKey = gistPrefUtils.getString(EncryptedPrefConst.MIXPANEL_KEY)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.MIXPANEL_KEY, value).apply()
        }

    var termCondition = gistPrefUtils.getString(EncryptedPrefConst.TERMS_CONDITION)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.TERMS_CONDITION, value).apply()
        }

    var privacyPolicy = gistPrefUtils.getString(EncryptedPrefConst.PRIVACY_POLICY)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.PRIVACY_POLICY, value).apply()
        }

    var googleURL = gistPrefUtils.getString(EncryptedPrefConst.GOOGLE_URL)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.GOOGLE_URL, value).apply()
        }

    var countryCodeURL = gistPrefUtils.getString(EncryptedPrefConst.COUNTRY_CODE_URL)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.COUNTRY_CODE_URL, value).apply()
        }

    var baseURL = gistPrefUtils.getString(EncryptedPrefConst.BASE_URL)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.BASE_URL, value).apply()
        }

    var nsfwURL = gistPrefUtils.getString(EncryptedPrefConst.NSFW_URL)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.NSFW_URL, value).apply()
        }

    var imagesURL = gistPrefUtils.getString(EncryptedPrefConst.IMAGES_URL)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.IMAGES_URL, value).apply()
        }

    var customImagesURL = gistPrefUtils.getString(EncryptedPrefConst.CUSTOM_IMAGES_URL)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.CUSTOM_IMAGES_URL, value).apply()
        }

    var iconURL = gistPrefUtils.getString(EncryptedPrefConst.ICON_URL)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.ICON_URL, value).apply()
        }

    var socURL = gistPrefUtils.getString(EncryptedPrefConst.SOC_URL)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.SOC_URL, value).apply()
        }

    var chatMediaURL = gistPrefUtils.getString(EncryptedPrefConst.CHAT_MEDIA_URL)
        set(value) {
            field = value
            encryptedPrefsEditor.putString(EncryptedPrefConst.CHAT_MEDIA_URL, value).apply()
        }

    fun setGistData(jsonObject: JsonObject) {
        if (jsonObject.has(EncryptedPrefConst.APP_REDIRECT_OTHER_APP_STATUS)) {
            appRedirectOtherAppStatus = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.APP_REDIRECT_OTHER_APP_STATUS).asBoolean
        }
        if (jsonObject.has(EncryptedPrefConst.APP_NEW_PACKAGE_NAME)) {
            appNewPackageName = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.APP_NEW_PACKAGE_NAME).asString
        }
        if (jsonObject.has(EncryptedPrefConst.APP_UPDATE_APP_DIALOG_STATUS)) {
            appUpdateAppDialogStatus = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.APP_UPDATE_APP_DIALOG_STATUS).asBoolean
        }
        if (jsonObject.has(EncryptedPrefConst.APP_VERSION_CODE)) {
            appVersionCode = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.APP_VERSION_CODE).asString
        }
        if (jsonObject.has(EncryptedPrefConst.APP_UPDATE_PACKAGE_NAME)) {
            appUpdatePackageName = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.APP_UPDATE_PACKAGE_NAME).asString
        }
        if (jsonObject.has(EncryptedPrefConst.APP_ONE_SINGLE_ID)) {
            appOneSingleID = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.APP_ONE_SINGLE_ID).asString
        }
        if (jsonObject.has(EncryptedPrefConst.G_NAME)) {
            gName = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.G_NAME).asString
        }
        if (jsonObject.has(EncryptedPrefConst.F_NAME)) {
            fName = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.F_NAME).asString
        }
        if (jsonObject.has(EncryptedPrefConst.GCP_KEY)) {
            gcpKey = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.GCP_KEY).asString
        }
        if (jsonObject.has(EncryptedPrefConst.NSFW_KEY)) {
            nsfwKey = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.NSFW_KEY).asString
        }
        if (jsonObject.has(EncryptedPrefConst.REVENUE_CAT_KEY)) {
            revenueCatKey = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.REVENUE_CAT_KEY).asString
        }
        if (jsonObject.has(EncryptedPrefConst.MIXPANEL_KEY)) {
            mixpanelKey = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.MIXPANEL_KEY).asString
        }
        if (jsonObject.has(EncryptedPrefConst.PRIVACY_POLICY)) {
            privacyPolicy = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.PRIVACY_POLICY).asString
        }
        if (jsonObject.has(EncryptedPrefConst.TERMS_CONDITION)) {
            termCondition = jsonObject.getAsJsonPrimitive(EncryptedPrefConst.TERMS_CONDITION).asString
        }
        if (jsonObject.has(EncryptedPrefConst.GOOGLE_URL)) {
            googleURL = AESUtils.Logd(jsonObject.getAsJsonPrimitive(EncryptedPrefConst.GOOGLE_URL).asString)
        }
        if (jsonObject.has(EncryptedPrefConst.COUNTRY_CODE_URL)) {
            countryCodeURL = AESUtils.Logd(jsonObject.getAsJsonPrimitive(EncryptedPrefConst.COUNTRY_CODE_URL).asString)
        }
        if (jsonObject.has(EncryptedPrefConst.BASE_URL)) {
            baseURL = AESUtils.Logd(jsonObject.getAsJsonPrimitive(EncryptedPrefConst.BASE_URL).asString)
        }
        if (jsonObject.has(EncryptedPrefConst.NSFW_URL)) {
            nsfwURL = AESUtils.Logd(jsonObject.getAsJsonPrimitive(EncryptedPrefConst.NSFW_URL).asString)
        }
        if (jsonObject.has(EncryptedPrefConst.IMAGES_URL)) {
            imagesURL = AESUtils.Logd(jsonObject.getAsJsonPrimitive(EncryptedPrefConst.IMAGES_URL).asString)
        }
        if (jsonObject.has(EncryptedPrefConst.CUSTOM_IMAGES_URL)) {
            customImagesURL = AESUtils.Logd(jsonObject.getAsJsonPrimitive(EncryptedPrefConst.CUSTOM_IMAGES_URL).asString)
        }
        if (jsonObject.has(EncryptedPrefConst.ICON_URL)) {
            iconURL = AESUtils.Logd(jsonObject.getAsJsonPrimitive(EncryptedPrefConst.ICON_URL).asString)
        }
        if (jsonObject.has(EncryptedPrefConst.SOC_URL)) {
            socURL = AESUtils.Logd(jsonObject.getAsJsonPrimitive(EncryptedPrefConst.SOC_URL).asString)
        }
        if (jsonObject.has(EncryptedPrefConst.CHAT_MEDIA_URL)) {
            chatMediaURL = AESUtils.Logd(jsonObject.getAsJsonPrimitive(EncryptedPrefConst.CHAT_MEDIA_URL).asString)
        }
    }
}
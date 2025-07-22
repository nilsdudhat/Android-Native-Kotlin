package com.belive.dating.preferences.pref_helpers

import com.belive.dating.constants.IntroductionConstants
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.preferences.pref_utils.IntroductionPrefUtils

object IntroductionPrefs {

    private val introductionPrefUtils = getKoinObject().get<IntroductionPrefUtils>()
    private val introductionPrefsEditor = introductionPrefUtils.getPrefEditor()

    var email: String? = null
        get() {
            return introductionPrefUtils.getString(IntroductionConstants.EMAIL)
        }
        set(value) {
            field = value
            introductionPrefUtils.setString(IntroductionConstants.EMAIL, value)
        }

    var loginType: Int = -1
        get() {
            return introductionPrefUtils.getInt(IntroductionConstants.LOGIN_TYPE, -1)
        }
        set(value) {
            field = value
            introductionPrefUtils.setInt(IntroductionConstants.LOGIN_TYPE, value)
        }

    var name: String? = null
        get() {
            return introductionPrefUtils.getString(IntroductionConstants.NAME)
        }
        set(value) {
            field = value
            introductionPrefUtils.setString(IntroductionConstants.NAME, value)
        }

    var gender: Int = 0
        get() {
            return introductionPrefUtils.getInt(IntroductionConstants.GENDER, 0)
        }
        set(value) {
            field = value
            introductionPrefUtils.setInt(IntroductionConstants.GENDER, value)
        }

    var birthDate: String? = null
        get() {
            return introductionPrefUtils.getString(IntroductionConstants.BIRTH_DATE)
        }
        set(value) {
            field = value
            introductionPrefUtils.setString(IntroductionConstants.BIRTH_DATE, value)
        }

    var oppositeGender: Int = 0
        get() {
            return introductionPrefUtils.getInt(IntroductionConstants.OPPOSITE_GENDER, 0)
        }
        set(value) {
            field = value
            introductionPrefUtils.setInt(IntroductionConstants.OPPOSITE_GENDER, value)
        }

    var sexualOrientation: Int = -1
        get() {
            return introductionPrefUtils.getInt(IntroductionConstants.SEXUAL_ORIENTATION, -1)
        }
        set(value) {
            field = value
            introductionPrefUtils.setInt(IntroductionConstants.SEXUAL_ORIENTATION, value)
        }

    var relationshipGoal: Int = -1
        get() {
            return introductionPrefUtils.getInt(IntroductionConstants.RELATIONSHIP_GOAL, -1)
        }
        set(value) {
            field = value
            introductionPrefUtils.setInt(IntroductionConstants.RELATIONSHIP_GOAL, value)
        }

    var interestList: List<String>? = null
        get() {
            return introductionPrefUtils.getStringList(IntroductionConstants.INTEREST_LIST)
        }
        set(value) {
            field = value
            introductionPrefUtils.setStringList(IntroductionConstants.INTEREST_LIST, value)
        }

    var photoList: List<String>? = null
        get() {
            return introductionPrefUtils.getStringList(IntroductionConstants.PHOTO_LIST)
        }
        set(value) {
            field = value
            introductionPrefUtils.setStringList(IntroductionConstants.PHOTO_LIST, value)
        }

    var selfie: String? = null
        get() {
            return introductionPrefUtils.getString(IntroductionConstants.SELFIE)
        }
        set(value) {
            field = value
            introductionPrefUtils.setString(IntroductionConstants.SELFIE, value)
        }
}
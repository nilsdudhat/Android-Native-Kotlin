package com.belive.dating.constants

enum class GENDER_OPTIONS(val genderIndex: Int) {
    MALE(1),
    FEMALE(2),
    OTHER(3),
}

enum class OPPOSITE_GENDER_OPTIONS(val genderIndex: Int) {
    MEN(1),
    WOMEN(2),
    BOTH(3),
}

enum class PROFILE_VISIBILITY(val profileVisibilityIndex: Int) {
    SLANDERED(1),
    INCOGNITO(2),
    HIDDEN(3),
}

object UserConstants {

    const val ONLINE_TIME_MAX: Int = 7
}
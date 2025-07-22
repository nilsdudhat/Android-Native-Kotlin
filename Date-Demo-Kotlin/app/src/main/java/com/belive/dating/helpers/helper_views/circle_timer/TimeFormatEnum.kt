package com.belive.dating.helpers.helper_views.circle_timer

enum class TimeFormatEnum {
    MILLIS,
    SECONDS,
    MINUTES,
    HOUR,
    DAY;

    fun canonicalForm(): String {
        return this.name
    }

    companion object {
        fun fromCanonicalForm(canonical: String): TimeFormatEnum {
            return valueOf(canonical)
        }
    }
}

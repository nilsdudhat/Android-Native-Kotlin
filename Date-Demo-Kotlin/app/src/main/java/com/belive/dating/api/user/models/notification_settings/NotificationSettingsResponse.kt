package com.belive.dating.api.user.models.notification_settings

import com.google.gson.annotations.SerializedName

data class NotificationSettingsResponse(
    @SerializedName("data")
    val notificationSettings: NotificationSettings,
    val message: String,
)

data class NotificationSettings(
    @SerializedName("new_like_alert")
    val newLikeAlert: Boolean,
    @SerializedName("new_match_alert")
    val newMatchAlert: Boolean,
    @SerializedName("new_message_alert")
    val newMessageAlert: Boolean,
    @SerializedName("new_super_like_alert")
    val newSuperLikeAlert: Boolean,
)
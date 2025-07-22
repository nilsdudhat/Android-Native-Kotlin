package com.belive.dating.api.user.models.friend

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class FriendsList : ArrayList<FriendDetails>(), Parcelable

@Parcelize
data class FriendDetails(
    val friendData: FriendData,
    @SerializedName("friend_id")
    val friendId: Int,
    @SerializedName("is_blocked_by_friend")
    val isBlockedByFriend: Int,
    @SerializedName("is_blocked_by_user")
    val isBlockedByUser: Int,
    @SerializedName("is_deleted_by_friend")
    val isDeletedByFriend: Int,
    @SerializedName("is_deleted_by_user")
    val isDeletedByUser: Int,
    val lastMessage: LastMessage? = null,
    var unReadCount: Int,
    var lastMessageTime: String,
    @SerializedName("userData")
    val userData: UserData,
    @SerializedName("user_id")
    val userId: Int,
    var isTyping: Boolean = false,
    var isOnline: Boolean,
) : Parcelable

@Parcelize
data class FriendData(
    @SerializedName("fullname")
    val fullName: String,
    val id: Int,
    @SerializedName("userimages")
    val userImage: String,
) : Parcelable

@Parcelize
data class LastMessage(
    val createdAt: String,
    @SerializedName("emoji_by_receiver")
    val emojiByReceiver: String?,
    @SerializedName("emoji_by_sender")
    val emojiBySender: String?,
    val id: Int,
    @SerializedName("is_deleted_by_receiver")
    val isDeletedByReceiver: Boolean,
    @SerializedName("is_deleted_by_sender")
    val isDeletedBySender: Boolean,
    @SerializedName("is_edit")
    val isEdit: Boolean,
    @SerializedName("is_media")
    val isMedia: Boolean,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("is_send")
    val isSend: Boolean,
    @SerializedName("media_url")
    val mediaUrl: String,
    val message: String? = null,
    @SerializedName("message_replay_id")
    val messageReplayId: Int,
    @SerializedName("receiver_id")
    val receiverId: Int,
    @SerializedName("send_by")
    val sendBy: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    val updatedAt: String,
) : Parcelable

@Parcelize
data class UserData(
    @SerializedName("fullname")
    val fullName: String,
    val id: Int,
    @SerializedName("userimages")
    val userImage: String,
) : Parcelable
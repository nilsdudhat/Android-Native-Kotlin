package com.belive.dating.api.user.models.friend_chat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TypingResponseModel(
    @SerializedName("is_typing")
    val isTyping: Boolean,
    @SerializedName("user_id")
    val userId: Int,
) : Parcelable

@Parcelize
data class DeleteMessageModel(
    @SerializedName("friend_id")
    val friendId: Int,
    @SerializedName("messageIds")
    val messageIds: List<Int>,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("totalPage")
    val totalPage: Int
) : Parcelable

@Parcelize
data class ChatResponse(
    val chatData: ChatData,
    @SerializedName("friend_id")
    val friendId: Int,
    @SerializedName("user_id")
    val userId: Int,
) : Parcelable

@Parcelize
data class PaginateChatData(
    var currentPage: Int,
    var messages: ArrayList<Message>,
    var totalPages: Int,
    @SerializedName("is_msg_deleted")
    var isMsgDeleted: Boolean,
) : Parcelable

@Parcelize
data class FriendChatResponse(
    val chatData: ChatData?,
    val friendData: FriendData,
    val isOnline: Boolean,
    val isBlocked: Boolean,
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
data class ChatData(
    var currentPage: Int,
    var messages: ArrayList<Message>,
    var totalPages: Int,
) : Parcelable

@Parcelize
data class Message(
    val createdAt: String,
    @SerializedName("emoji_by_receiver")
    val emojiByReceiver: String? = null,
    @SerializedName("emoji_by_sender")
    val emojiBySender: String? = null,
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
    val mediaUrl: String? = null,
    @SerializedName("sender_media_path")
    val senderMediaPath: String? = null,
    @SerializedName("receiver_media_path")
    var receiverMediaPath: String? = null,
    val message: String? = null,
    @SerializedName("media_name")
    val mediaName: String? = null,
    @SerializedName("message_replay_id")
    val messageReplayId: Int,
    @SerializedName("media_type")
    var mediaType: Int = 1,
    @SerializedName("receiver_id")
    val receiverId: Int,
    val replayData: ReplyData? = null,
    @SerializedName("send_by")
    val sendBy: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    val updatedAt: String,
    @SerializedName("is_date")
    var isDate: Boolean = false,
    @SerializedName("is_selected")
    var isSelected: Boolean = false,
    var msgTime: String? = null,
    @SerializedName("is_profile")
    var isProfile: Boolean = false,
    var progress: Int = -1
) : Parcelable

@Parcelize
data class ReplyData(
    val id: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    @SerializedName("receiver_id")
    val receiverId: Int,
    val message: String,
    @SerializedName("message_replay_id")
    val messageReplayId: Int,
    @SerializedName("send_by")
    val sendBy: Int,
    @SerializedName("is_edit")
    val isEdit: Boolean,
    @SerializedName("is_media")
    val isMedia: Boolean,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("is_send")
    val isSend: Boolean,
    @SerializedName("media_url")
    val mediaUrl: String? = null,
    @SerializedName("media_path")
    val mediaPath: String? = null,
    @SerializedName("is_deleted_by_receiver")
    val isDeletedByReceiver: Boolean,
    @SerializedName("is_deleted_by_sender")
    val isDeletedBySender: Boolean,
    @SerializedName("emoji_by_receiver")
    val emojiByReceiver: String?,
    @SerializedName("emoji_by_sender")
    val emojiBySender: String?,
    val createdAt: String,
    val updatedAt: String,
    val senderData: SenderData,
) : Parcelable

@Parcelize
data class SenderData(
    @SerializedName("fullname")
    val fullName: String,
    val id: Int,
) : Parcelable
package com.belive.dating.constants

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ChatType(val value: Int) : Parcelable {
    TYPE_DATE(0),
    TYPE_TEXT(1),
    TYPE_IMAGE(2),
    TYPE_VIDEO(3),
}

@Parcelize
enum class MessageType(val value: Int) : Parcelable {
    TYPE_TEXT_SEND(21),
    TYPE_TEXT_RECEIVE(22),
    TYPE_IMAGE_SEND(23),
    TYPE_IMAGE_RECEIVE(24),
    TYPE_VIDEO_SEND(25),
    TYPE_VIDEO_RECEIVE(26),
}

object SocketConstants {

    // send
    const val MESSAGE = "message"
    const val CREATE_USER = "createUser"
    const val DELETE_MESSAGE = "deleteMessage"
    const val GET_FRIEND_LIST = "getFriendList"
    const val GET_FRIEND_CHAT = "getFriendChat"
    const val GET_PAGINATION_MESSAGE = "getPaginateMessage"
    const val GET_UPTO_REPLY_MESSAGE = "getUptoReplayMessage"
    const val ON_CHAT_STATUS_UPDATE_REQUEST = "onChatStatusUpdateRequest"
    const val ON_MEDIA_PATH_UPDATE = "onMediaPathUpdate"
    const val ON_MEDIA_SEND = "onMediaSend"
    const val SET_ONLINE_STATUS = "setOnlineStatus"
    const val TYPING = "onTyping"

    // receive
    const val GET_FRIEND_LIST_RESPONSE = "getFriendListResponse"
    const val GET_FRIEND_CHAT_RESPONSE = "getFriendChatResponse"
    const val ON_CHAT_RESPONSE = "onChatResponse"
    const val ON_PAGINATE_RESPONSE = "onPaginateResponse"
    const val ON_DELETE_MESSAGE_RESPONSE = "onDeleteMessageResponse"
    const val TYPING_RESPONSE = "onTypingResponse"
}
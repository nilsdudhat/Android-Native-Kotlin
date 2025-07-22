package com.belive.dating.helpers.helper_functions.socket

import com.belive.dating.api.user.models.friend.FriendsList
import com.belive.dating.api.user.models.friend_chat.ChatResponse
import com.belive.dating.api.user.models.friend_chat.DeleteMessageModel
import com.belive.dating.api.user.models.friend_chat.FriendChatResponse
import com.belive.dating.api.user.models.friend_chat.PaginateChatData
import com.belive.dating.api.user.models.friend_chat.TypingResponseModel
import com.belive.dating.constants.SocketConstants
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import io.socket.client.AckWithTimeout
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

enum class SocketStatus { DISCONNECTED, CONNECTING, CONNECTED }

object SocketManager {
    private var socket: Socket? = null

    var status = SocketStatus.DISCONNECTED

    val connected: Boolean
        get() {
            return socket?.connected() == true
        }

    fun connect() {
        if (socket?.connected() == true) return
        if ((status == SocketStatus.CONNECTED) || (status == SocketStatus.CONNECTING)) return

        status = SocketStatus.CONNECTING

        try {
            val options = IO.Options()
            options.path = "/socket.io"
            options.timeout = 30000
            options.upgrade = true
            options.reconnectionDelay = 1000
            options.reconnectionDelayMax = 5000
            options.transports = arrayOf("websocket", "polling")

            socket = IO.socket(getGistPrefs().socURL, options)

            socket?.connect()

            listenOnEvents()

            socket?.on(Socket.EVENT_CONNECT) {
                emit(SocketConstants.CREATE_USER, getUserPrefs().userId.toString())
            }?.on(Socket.EVENT_DISCONNECT) {
                connect()
            }?.on(Socket.EVENT_CONNECT_ERROR) {
                connect()
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun listenOnEvents() {
        socket?.on(SocketConstants.GET_FRIEND_LIST_RESPONSE) { response ->
            logger("--event--", "on: SocketConstants.GET_FRIEND_LIST_RESPONSE")

            EventManager.postEvent(Event(SocketConstants.GET_FRIEND_LIST_RESPONSE, response[0].toString().fromJson<FriendsList>()))
        }
        socket?.on(SocketConstants.GET_FRIEND_CHAT_RESPONSE) { response ->
            logger("--event--", "on: SocketConstants.GET_FRIEND_CHAT_RESPONSE")

            EventManager.postEvent(Event(SocketConstants.GET_FRIEND_CHAT_RESPONSE, response[0].toString().fromJson<FriendChatResponse>()))
        }
        socket?.on(SocketConstants.ON_CHAT_RESPONSE) { response ->
            logger("--event--", "on: SocketConstants.ON_CHAT_RESPONSE")

            EventManager.postEvent(Event(SocketConstants.ON_CHAT_RESPONSE, response[0].toString().fromJson<ChatResponse>()))
        }
        socket?.on(SocketConstants.ON_PAGINATE_RESPONSE) { response ->
            logger("--event--", "on: SocketConstants.ON_PAGINATE_RESPONSE")

            EventManager.postEvent(Event(SocketConstants.ON_PAGINATE_RESPONSE, response[0].toString().fromJson<PaginateChatData>()))
        }
        socket?.on(SocketConstants.ON_DELETE_MESSAGE_RESPONSE) { response ->
            logger("--event--", "on: SocketConstants.ON_DELETE_MESSAGE_RESPONSE")

            EventManager.postEvent(Event(SocketConstants.ON_DELETE_MESSAGE_RESPONSE, response[0].toString().fromJson<DeleteMessageModel>()))
        }
        socket?.on(SocketConstants.TYPING_RESPONSE) { response ->
            logger("--event--", "on: SocketConstants.TYPING_RESPONSE")

            EventManager.postEvent(Event(SocketConstants.TYPING_RESPONSE, response[0].toString().fromJson<TypingResponseModel>()))
        }
    }

    fun getSocket(): Socket? {
        return socket
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.close()
        socket?.off()
        status = SocketStatus.DISCONNECTED
    }

    fun emit(event: String, vararg dataString: Any, onMessageSuccess: (() -> Unit)? = null) {
        logger("--event--", "emit: $event")

        if (status == SocketStatus.DISCONNECTED) {
            logger("--event--", "SocketStatus.DISCONNECTED")

            connect()
        }

        socket?.emit(event, dataString, object : AckWithTimeout(5 * 6000) {
            override fun onTimeout() {
                logger("--event--", "emit: onTimeout")
            }

            override fun onSuccess(vararg args: Any) {
                logger("--event--", "emit: onSuccess")

                onMessageSuccess?.invoke()
            }
        })
    }
}
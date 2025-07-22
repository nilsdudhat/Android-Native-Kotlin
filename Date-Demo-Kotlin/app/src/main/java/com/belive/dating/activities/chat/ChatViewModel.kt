package com.belive.dating.activities.chat

import android.os.Handler
import android.os.Looper
import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.chat_file.FileDownloadModel
import com.belive.dating.api.user.models.chat_file.FileUploadModel
import com.belive.dating.api.user.models.friend_chat.Message
import com.belive.dating.api.user.models.friend_chat.PaginateChatData
import com.belive.dating.constants.SocketConstants
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.convertDateToTime
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.isSameDay
import com.belive.dating.extensions.iso8601ToMillis
import com.belive.dating.extensions.logger
import com.belive.dating.helpers.helper_functions.socket.SocketManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class MediaSendType(val value: Int) {
    IMAGE(2),
    VIDEO(3),
    NOT_SELECTED(1),
}

class ChatViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "CHAT_VIEW_MODEL"

    val isLoading = ObservableField<Boolean>()
    val friendId = ObservableField<Int>()
    val profileImage = ObservableField<String>()
    val profileName = ObservableField<String>()
    val isOnline = ObservableField(false)
    val isFriendTyping = ObservableField(false)
    val toReplyName = ObservableField<String>()
    val toReplyMessage = ObservableField<String>()
    val toReplyImage = ObservableField<String>()
    val isReply = ObservableField(false)
    val replyId = ObservableField<Int>()
    val replyType = ObservableField(1)
    val messageList = ObservableField<ArrayList<Message>>(arrayListOf())
    val sendingList = ObservableField<ArrayList<FileUploadModel>>(arrayListOf())
    val downloadingList = ObservableField<ArrayList<FileDownloadModel>>(arrayListOf())
    val isPaginate = ObservableField(false)
    val blinkPosition = ObservableField(RecyclerView.NO_POSITION)
    val isScrollEnabled = ObservableField(true)
    val scrollState = ObservableField(false)
    val pid = ObservableField<Int>()
    val lastPosition = ObservableField<Int>()
    val messageSend = ObservableField<String>()
    val currentPage = ObservableField(1)
    val totalPages = ObservableField(1)
    val mediaSendType = ObservableField<MediaSendType>()
    val isLoadMore = ObservableField(false)
    val isDeleteView = ObservableField(false)
    val deleteCount = ObservableField(0)

    fun updateState() {
        savedStateHandle["${TAG}_isLoading"] = isLoading.get()
        savedStateHandle["${TAG}_friendId"] = friendId.get()
        savedStateHandle["${TAG}_profileImage"] = profileImage.get()
        savedStateHandle["${TAG}_profileName"] = profileName.get()
        savedStateHandle["${TAG}_isOnline"] = isOnline.get()
        savedStateHandle["${TAG}_isFriendTyping"] = isFriendTyping.get()
        savedStateHandle["${TAG}_toReplyName"] = toReplyName.get()
        savedStateHandle["${TAG}_toReplyMessage"] = toReplyMessage.get()
        savedStateHandle["${TAG}_toReplyImage"] = toReplyImage.get()
        savedStateHandle["${TAG}_isReply"] = isReply.get()
        savedStateHandle["${TAG}_replyId"] = replyId.get()
        savedStateHandle["${TAG}_replyType"] = replyType.get()
        savedStateHandle["${TAG}_messageList"] = messageList.get()
        savedStateHandle["${TAG}_sendingList"] = sendingList.get()
        savedStateHandle["${TAG}_isPaginate"] = isPaginate.get()
        savedStateHandle["${TAG}_blinkPosition"] = blinkPosition.get()
        savedStateHandle["${TAG}_isScrollEnabled"] = isScrollEnabled.get()
        savedStateHandle["${TAG}_scrollState"] = scrollState.get()
        savedStateHandle["${TAG}_pid"] = pid.get()
        savedStateHandle["${TAG}_lastPosition"] = lastPosition.get()
        savedStateHandle["${TAG}_messageSend"] = messageSend.get()
        savedStateHandle["${TAG}_currentPage"] = currentPage.get()
        savedStateHandle["${TAG}_totalPages"] = totalPages.get()
        savedStateHandle["${TAG}_mediaSendType"] = mediaSendType.get()
        savedStateHandle["${TAG}_isLoadMore"] = isLoadMore.get()
        savedStateHandle["${TAG}_isDeleteView"] = isDeleteView.get()
        savedStateHandle["${TAG}_deleteCount"] = deleteCount.get()
    }

    fun getState() {
        isLoading.set(savedStateHandle["${TAG}_isLoading"])
        friendId.set(savedStateHandle["${TAG}_friendId"])
        profileImage.set(savedStateHandle["${TAG}_profileImage"])
        profileName.set(savedStateHandle["${TAG}_profileName"])
        isOnline.set(savedStateHandle["${TAG}_isOnline"])
        isFriendTyping.set(savedStateHandle["${TAG}_isFriendTyping"])
        toReplyName.set(savedStateHandle["${TAG}_toReplyName"])
        toReplyMessage.set(savedStateHandle["${TAG}_toReplyMessage"])
        toReplyImage.set(savedStateHandle["${TAG}_toReplyImage"])
        isReply.set(savedStateHandle["${TAG}_isReply"])
        replyId.set(savedStateHandle["${TAG}_replyId"])
        replyType.set(savedStateHandle["${TAG}_replyType"])
        messageList.set(savedStateHandle["${TAG}_messageList"])
        sendingList.set(savedStateHandle["${TAG}_sendingList"])
        isPaginate.set(savedStateHandle["${TAG}_isPaginate"])
        blinkPosition.set(savedStateHandle["${TAG}_blinkPosition"])
        isScrollEnabled.set(savedStateHandle["${TAG}_isScrollEnabled"])
        scrollState.set(savedStateHandle["${TAG}_scrollState"])
        pid.set(savedStateHandle["${TAG}_pid"])
        lastPosition.set(savedStateHandle["${TAG}_lastPosition"])
        messageSend.set(savedStateHandle["${TAG}_messageSend"])
        currentPage.set(savedStateHandle["${TAG}_currentPage"])
        totalPages.set(savedStateHandle["${TAG}_totalPages"])
        mediaSendType.set(savedStateHandle["${TAG}_mediaSendType"])
        isLoadMore.set(savedStateHandle["${TAG}_isLoadMore"])
        isDeleteView.set(savedStateHandle["${TAG}_isDeleteView"])
        deleteCount.set(savedStateHandle["${TAG}_deleteCount"])
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun addDateTitles(messages: ArrayList<Message>, selectedMessages: List<Message>, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newList = ArrayList<Message>()
                for (i in 0 until messages.size) {
                    if ((messages.size == 1) && newList.none { it.id == messages[i].id }) {
                        val msg = Message(
                            createdAt = messages[i].createdAt,
                            emojiByReceiver = messages[i].emojiByReceiver,
                            emojiBySender = messages[i].emojiBySender,
                            isDeletedByReceiver = messages[i].isDeletedByReceiver,
                            isDeletedBySender = messages[i].isDeletedBySender,
                            isEdit = messages[i].isEdit,
                            isMedia = messages[i].isMedia,
                            isRead = messages[i].isRead,
                            isSend = messages[i].isSend,
                            mediaUrl = messages[i].mediaUrl,
                            senderMediaPath = messages[i].senderMediaPath,
                            receiverMediaPath = messages[i].receiverMediaPath,
                            message = messages[i].message,
                            mediaName = messages[i].mediaName,
                            messageReplayId = messages[i].messageReplayId,
                            receiverId = messages[i].receiverId,
                            replayData = messages[i].replayData,
                            sendBy = messages[i].sendBy,
                            senderId = messages[i].senderId,
                            updatedAt = messages[i].updatedAt,
                            id = -1,
                            mediaType = 0, // date = 0, message = 1, image = 2, video = 3
                            isDate = true,
                        )
                        newList.add(msg)
                    } else {
                        if ((i <= messages.size - 1 && messages[i].isDate) && (newList.none { it.id == messages[i].id })) {
                            val msg = Message(
                                createdAt = messages[i].createdAt,
                                emojiByReceiver = messages[i].emojiByReceiver,
                                emojiBySender = messages[i].emojiBySender,
                                isDeletedByReceiver = messages[i].isDeletedByReceiver,
                                isDeletedBySender = messages[i].isDeletedBySender,
                                isEdit = messages[i].isEdit,
                                isMedia = messages[i].isMedia,
                                isRead = messages[i].isRead,
                                isSend = messages[i].isSend,
                                mediaUrl = messages[i].mediaUrl,
                                senderMediaPath = messages[i].senderMediaPath,
                                receiverMediaPath = messages[i].receiverMediaPath,
                                message = messages[i].message,
                                mediaName = messages[i].mediaName,
                                messageReplayId = messages[i].messageReplayId,
                                receiverId = messages[i].receiverId,
                                replayData = messages[i].replayData,
                                sendBy = messages[i].sendBy,
                                senderId = messages[i].senderId,
                                updatedAt = messages[i].updatedAt,
                                id = -1,
                                mediaType = 0, // date = 0, message = 1, image = 2, video = 3
                                isDate = true,
                            )
                            newList.add(msg)
                        }
                    }
                    newList.add(messages[i].apply {
                        isSelected = selectedMessages.any { it.id == messages[i].id }
                    })
                }
                messageList.set(newList)
            } catch (e: Exception) {
                catchLog("updateMessageList: ${e.printStackTrace()}")
            } finally {
                launch(Dispatchers.Main) {
                    onFinish.invoke()
                }
            }
        }
    }

    fun groupChatMessagesByDate(chatMessages: List<Message>): ArrayList<Message> {
        val filterList = ArrayList<Message>()
        val groupedMessages = mutableMapOf<String, MutableList<Message>>()
        val today = Calendar.getInstance().apply {
            timeInMillis = getUserPrefs().currentDateFromAPI
        }
        val tomorrow = Calendar.getInstance().apply {
            timeInMillis = getUserPrefs().currentDateFromAPI
        }
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        for (message in chatMessages) {
            val messageDate = Calendar.getInstance().apply {
                timeInMillis = iso8601ToMillis(message.createdAt)
            }

            val key = when {
                isSameDay(messageDate, today) -> "Today"
                isSameDay(messageDate, tomorrow) -> "Yesterday"
                else -> dateFormat.format(iso8601ToMillis(message.createdAt))
            }

            if (!groupedMessages.containsKey(key)) {
                message.isDate = true
                groupedMessages[key] = mutableListOf()
            }

            message.msgTime = convertDateToTime(message.createdAt)
            filterList.add(message)
            groupedMessages[key]?.add(message)
        }

        filterList.removeAll { ((it.sendBy == getUserPrefs().userId) && it.isDeletedBySender) || ((it.sendBy == friendId.get()) && it.isDeletedByReceiver) }

        for (i in 0 until filterList.size) {
            if (filterList[i].sendBy != getUserPrefs().userId) {
                val inx = filterList.indexOfFirst { (it.id != getUserPrefs().userId) && (it.msgTime == filterList[i].msgTime) }
                if (i == inx) {
                    filterList[i].isProfile = true
                }
            }
        }

        return filterList
    }

    fun updatePaginationData(newData: PaginateChatData) {
        if (newData.isMsgDeleted) {
            isPaginate.set(false)
        } else {
            isPaginate.set(true)
            totalPages.set(newData.totalPages)
            currentPage.set(newData.currentPage)

            val list = messageList.get()
            newData.messages.forEach { message ->
                message.isDate = false
            }
            list!!.addAll(newData.messages)
            list.apply {
                sortBy { it.id }
            }

            val groupList = groupChatMessagesByDate(messageList.get()!!)
            messageList.set(groupList)
        }
    }

    fun disableDeleteView() {
        val list = messageList.get()!!
        list.forEach { message ->
            if (message.isSelected) {
                message.isSelected = false
            }
        }
        messageList.set(list)
        deleteCount.set(0)
        isDeleteView.set(false)
    }

    private var isTyping = false
    private val typingHandler = Handler(Looper.getMainLooper())

    private val typingThread = Runnable {
        val obj = JSONObject()
        obj.put("user_id", getUserPrefs().userId)
        obj.put("friend_id", friendId.get())
        obj.put("is_typing", false)
        SocketManager.emit(SocketConstants.TYPING, obj)
        isTyping = false
    }

    fun manageTyping(msg: String) {
        if (msg.isEmpty()) {

            if (isTyping) {
                typingHandler.removeCallbacks(typingThread)

                val obj = JSONObject()
                obj.put("user_id", getUserPrefs().userId)
                obj.put("friend_id", friendId.get())
                obj.put("is_typing", false)
                SocketManager.emit(SocketConstants.TYPING, obj)
            }
            isTyping = false
        } else if (!isTyping) {
            val obj = JSONObject()
            obj.put("user_id", getUserPrefs().userId)
            obj.put("friend_id", friendId.get())
            obj.put("is_typing", true)
            SocketManager.emit(SocketConstants.TYPING, obj)

            isTyping = true

            typingHandler.postDelayed(typingThread, 1000)
        }
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    fun readNotification(notificationId: Int): MutableStateFlow<Resource<JsonObject>> {
        val data = MutableStateFlow<Resource<JsonObject>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.readNotification(notificationId)
                val errorBody = response.errorBody()?.string()

                logger("--notification--", "url: ${response.raw().request.url}")
                logger("--notification--", "isSuccessful: " + response.isSuccessful)
                logger("--notification--", "message: " + response.message())
                logger("--notification--", "body: " + gsonString(response.body()))
                logger("--notification--", "errorBody: $errorBody")
                logger("--notification--", "code: " + response.code())

                if (response.isSuccessful) {
                    data.emit(Resource.success(response.body()))
                } else {
                    if (response.code() == 401) {
                        data.emit(Resource.signOut(getErrorMessage(errorBody), null))
                    } else if (response.code() == 403) {
                        data.emit(Resource.adminBlocked(getErrorMessage(errorBody), null))
                    } else {
                        if (!errorBody.isNullOrEmpty()) {
                            data.emit(Resource.error(getErrorMessage(errorBody), null))
                        } else {
                            data.emit(Resource.error("Something went wrong...!", null))
                        }
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }
}
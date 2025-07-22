package com.belive.dating.activities.chat

import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.belive.dating.BuildConfig
import com.belive.dating.api.user.models.friend_chat.Message
import com.belive.dating.constants.ChatType
import com.belive.dating.constants.MessageType
import com.belive.dating.databinding.ItemDateBinding
import com.belive.dating.databinding.ItemReceiveImageBinding
import com.belive.dating.databinding.ItemReceiveMessageBinding
import com.belive.dating.databinding.ItemReceiveVideoBinding
import com.belive.dating.databinding.ItemSendImageBinding
import com.belive.dating.databinding.ItemSendMessageBinding
import com.belive.dating.databinding.ItemSendVideoBinding
import com.belive.dating.extensions.convertDateToTime
import com.belive.dating.extensions.formatDateForChatGroupTitle
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.iso8601ToMillis
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.openImageInPlayer
import com.belive.dating.extensions.openVideoInPlayer
import com.belive.dating.helpers.helper_functions.double_click.DoubleClickListener
import java.io.File

class ChatAdapter(
    val viewModel: ChatViewModel,
    var callBack: QuoteClickListener,
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    interface QuoteClickListener {
        fun onQuoteClick(position: Int)
        fun onSelectClick(id: Int)
        fun onViewClick(imgUrl: String)
        fun onDownloadClick(chatMessage: Message)
    }

    var index = -1

    private val diffUtil = object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.isDate == newItem.isDate) && (oldItem.isSelected == newItem.isSelected) && (oldItem.isMedia == newItem.isMedia) && (oldItem.mediaType == newItem.mediaType) && (oldItem.progress == newItem.progress) && (oldItem.updatedAt == newItem.updatedAt)
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {

            MessageType.TYPE_TEXT_SEND.value -> {
                val binding = ItemSendMessageBinding.inflate(layoutInflater, parent, false)
                TextSentViewHolder(binding)
            }

            MessageType.TYPE_TEXT_RECEIVE.value -> {
                val binding = ItemReceiveMessageBinding.inflate(layoutInflater, parent, false)
                TextReceiveViewHolder(binding)
            }

            MessageType.TYPE_IMAGE_SEND.value -> {
                val binding = ItemSendImageBinding.inflate(layoutInflater, parent, false)
                ImageSentViewHolder(binding)
            }

            MessageType.TYPE_IMAGE_RECEIVE.value -> {
                val binding = ItemReceiveImageBinding.inflate(layoutInflater, parent, false)
                ImageReceiveViewHolder(binding)
            }

            MessageType.TYPE_VIDEO_SEND.value -> {
                val binding = ItemSendVideoBinding.inflate(layoutInflater, parent, false)
                VideoSentViewHolder(binding)
            }

            MessageType.TYPE_VIDEO_RECEIVE.value -> {
                val binding = ItemReceiveVideoBinding.inflate(layoutInflater, parent, false)
                VideoReceiveViewHolder(binding)
            }

            ChatType.TYPE_DATE.value -> {
                val binding = ItemDateBinding.inflate(layoutInflater, parent, false)
                DateViewHolder(binding)
            }

            else -> {
                val binding = ItemSendMessageBinding.inflate(layoutInflater, parent, false)
                TextSentViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = asyncListDiffer.currentList[position]
        logger("--list--", "position: $position ================================= ${message.mediaName}")

        val zoomIn: Animation = ScaleAnimation(
            1.0f, 1.2f,
            1.0f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f,
        ).apply {
            duration = 200
            fillAfter = true
        }

        val zoomOut: Animation = ScaleAnimation(
            1.2f, 1.0f,
            1.2f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f,
        ).apply {
            duration = 200
            fillAfter = true
        }

        if (viewModel.blinkPosition.get() == position) {
            holder.itemView.startAnimation(zoomIn)
            Handler(Looper.getMainLooper()).postDelayed({
                holder.itemView.startAnimation(zoomOut)
                viewModel.blinkPosition.set(RecyclerView.NO_POSITION)
            }, 200)
        }

        holder.bind(message, position, callBack)
    }

    fun updateProgress(msgId: Int, progress: Int, file: String) {
        logger("--upload--", "display progress: $progress")

        val inx = viewModel.messageList.get()?.indexOfFirst { it.id == msgId && it.mediaType != 0 }
        if ((inx != null) && (inx != -1)) {
            val message = viewModel.messageList.get()?.get(inx)
            message?.progress = progress

            if (progress == 100) {
                message?.receiverMediaPath = file
                MediaScannerConnection.scanFile(getKoinContext(), arrayOf(file), null) { path, uri ->
                    logger("--media_file--", "Scanned $path:")
                    logger("--media_file--", "-> uri=$uri")
                }
            }

            viewModel.messageList.set(
                viewModel.messageList.get()?.apply {
                    message?.let { set(inx, it) }
                }
            )

            asyncListDiffer.submitList(viewModel.messageList.get())

            notifyItemChanged(inx)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = asyncListDiffer.currentList[position]

        return when (message.mediaType) {

            ChatType.TYPE_TEXT.value -> {
                if (message.senderId == getUserPrefs().userId) {
                    MessageType.TYPE_TEXT_SEND.value
                } else {
                    MessageType.TYPE_TEXT_RECEIVE.value
                }
            }

            ChatType.TYPE_IMAGE.value -> {
                if (message.senderId == getUserPrefs().userId) {
                    MessageType.TYPE_IMAGE_SEND.value
                } else {
                    MessageType.TYPE_IMAGE_RECEIVE.value
                }
            }

            ChatType.TYPE_VIDEO.value -> {
                if (message.senderId == getUserPrefs().userId) {
                    MessageType.TYPE_VIDEO_SEND.value
                } else {
                    MessageType.TYPE_VIDEO_RECEIVE.value
                }
            }

            else -> {
                ChatType.TYPE_DATE.value
            }
        }
    }

    inner class TextSentViewHolder(private var binding: ItemSendMessageBinding) : ViewHolder(binding) {
        override fun bind(message: Message, seenStatus: Int, callBack: QuoteClickListener) {

            binding.apply {

                if (message.replayData != null) {
                    isReply = true
                    replyName = if (message.replayData.sendBy == getUserPrefs().userId) {
                        "Reply to Yourself"
                    } else {
                        StringBuilder("Reply to ").append(message.replayData.senderData.fullName).toString()
                    }

                    if (message.replayData.mediaPath.isNullOrEmpty() && message.replayData.mediaUrl.isNullOrEmpty()) {
                        replyMessage = message.replayData.message
                    } else {
                        replyImage = if (!message.senderMediaPath.isNullOrEmpty()) {
                            message.replayData.mediaPath
                        } else {
                            getGistPrefs().chatMediaURL + message.replayData.mediaUrl
                        }
                    }
                } else {
                    isReply = false
                }

                isSelected = message.isSelected

                this.message = message.message

                val formattedTime = convertDateToTime(message.createdAt)
                time = formattedTime
                isTimeVisible = false

                executePendingBindings()

                itemView.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                itemView.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    }
                }

                layoutMessage.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                layoutMessage.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        isTimeVisible = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            isTimeVisible = false
                        }, 1000)
                    }
                }

                layoutReply.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        callBack.onQuoteClick(bindingAdapterPosition)
                    }
                }
            }
        }
    }

    inner class TextReceiveViewHolder(private var binding: ItemReceiveMessageBinding) : ViewHolder(binding) {
        override fun bind(message: Message, seenStatus: Int, callBack: QuoteClickListener) {

            binding.apply {
                if (message.replayData != null) {
                    isReply = true
                    replyName = if (message.replayData.sendBy == getUserPrefs().userId) {
                        "Reply to Yourself"
                    } else {
                        StringBuilder("Reply to ").append(message.replayData.senderData.fullName).toString()
                    }

                    if (message.replayData.mediaPath.isNullOrEmpty() && message.replayData.mediaUrl.isNullOrEmpty()) {
                        replyMessage = message.replayData.message
                    } else {
                        replyImage = if (!message.senderMediaPath.isNullOrEmpty()) {
                            message.replayData.mediaPath
                        } else {
                            getGistPrefs().chatMediaURL + message.replayData.mediaUrl
                        }
                    }
                } else {
                    isReply = false
                }

                seenProfileImage = if (message.isProfile) {
                    viewModel.profileImage.get()
                } else {
                    null
                }

                isSelected = message.isSelected

                this.message = message.message

                val formattedTime = convertDateToTime(message.createdAt)
                time = formattedTime
                isTimeVisible = false

                executePendingBindings()

                itemView.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                itemView.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    }
                }

                layoutMessage.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                layoutMessage.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        isTimeVisible = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            isTimeVisible = false
                        }, 1000)
                    }
                }

                layoutReply.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        callBack.onQuoteClick(bindingAdapterPosition)
                    }
                }
            }
        }
    }

    inner class ImageSentViewHolder(private var binding: ItemSendImageBinding) : ViewHolder(binding) {
        override fun bind(message: Message, seenStatus: Int, callBack: QuoteClickListener) {
            binding.apply {
                if (message.replayData != null) {
                    isReply = true
                    replyName = if (message.replayData.sendBy == getUserPrefs().userId) {
                        "Reply to Yourself"
                    } else {
                        StringBuilder("Reply to ").append(message.replayData.senderData.fullName).toString()
                    }

                    if (message.replayData.mediaPath.isNullOrEmpty() && message.replayData.mediaUrl.isNullOrEmpty()) {
                        replyMessage = message.replayData.message
                    } else {
                        replyImage = if (!message.senderMediaPath.isNullOrEmpty()) {
                            message.replayData.mediaPath
                        } else {
                            getGistPrefs().chatMediaURL + message.replayData.mediaUrl
                        }
                    }
                } else {
                    isReply = false
                }

                logger("--progress--", message.progress)

                isSelected = message.isSelected

                if (message.senderMediaPath != null) {
                    if ((message.progress != -1) && (message.progress != 0) && (message.progress != 100)) {
                        progress = message.progress
                        isUploadVisible = false

                        image = getGistPrefs().chatMediaURL + message.mediaUrl
                    } else {
                        progress = null

                        if (File(message.senderMediaPath).exists()) {
                            isUploadVisible = false
                            image = message.senderMediaPath
                        } else {
                            isUploadVisible = true
                            image = getGistPrefs().chatMediaURL + message.mediaUrl
                        }
                    }
                } else {
                    if ((message.progress != -1) && (message.progress != 0) && (message.progress != 100)) {
                        progress = message.progress
                        isUploadVisible = false
                    } else {
                        progress = null
                        isUploadVisible = true
                    }
                    image = getGistPrefs().chatMediaURL + message.mediaUrl
                }

                val formattedTime = convertDateToTime(message.createdAt)
                time = formattedTime
                isTimeVisible = false

                itemView.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                itemView.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    }
                }

                layoutMessage.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                layoutMessage.setOnClickListener(object : DoubleClickListener() {
                    override fun onDoubleClick() {
                        if (message.senderMediaPath != null) {
                            val imageUri: Uri? = File(message.senderMediaPath).let {
                                FileProvider.getUriForFile(
                                    getKoinContext(), "${BuildConfig.APPLICATION_ID}.provider", it
                                )
                            }
                            imageUri?.let { getKoinActivity().openImageInPlayer(it) }
                        }
                    }

                    override fun onSingleClick() {
                        if (viewModel.isDeleteView.get() == true) {
                            callBack.onSelectClick(message.id)
                        } else {
                            isTimeVisible = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                isTimeVisible = false
                            }, 1000)
                        }
                    }
                })

                btnUpload.setOnClickListener {
                    isUploadVisible = false
                    progress = 0

                    callBack.onDownloadClick(message)
                }

                layoutReply.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        callBack.onQuoteClick(bindingAdapterPosition)
                    }
                }
            }
        }
    }

    inner class ImageReceiveViewHolder(private var binding: ItemReceiveImageBinding) : ViewHolder(binding) {
        override fun bind(message: Message, seenStatus: Int, callBack: QuoteClickListener) {
            binding.apply {
                if (message.replayData != null) {
                    isReply = true
                    replyName = if (message.replayData.sendBy == getUserPrefs().userId) {
                        "Reply to Yourself"
                    } else {
                        StringBuilder("Reply to ").append(message.replayData.senderData.fullName).toString()
                    }

                    if (message.replayData.mediaPath.isNullOrEmpty() && message.replayData.mediaUrl.isNullOrEmpty()) {
                        replyMessage = message.replayData.message
                    } else {
                        replyImage = if (!message.senderMediaPath.isNullOrEmpty()) {
                            message.replayData.mediaPath
                        } else {
                            getGistPrefs().chatMediaURL + message.replayData.mediaUrl
                        }
                    }
                } else {
                    isReply = false
                }

                isSelected = message.isSelected

                if (viewModel.downloadingList.get()?.any { it.msgId == message.id } == true) {
                    isDownloadVisible = false
                    progress = if ((message.progress != -1) && (message.progress != 0) && (message.progress != 100)) message.progress else 0
                } else {
                    isDownloadVisible = true
                    progress = null
                }

                image = if (message.receiverMediaPath != null) {
                    if (File(message.receiverMediaPath!!).exists()) {
                        isDownloadVisible = false
                        message.receiverMediaPath
                    } else {
                        getGistPrefs().chatMediaURL + message.mediaUrl
                    }
                } else {
                    getGistPrefs().chatMediaURL + message.mediaUrl
                }

                val formattedTime = convertDateToTime(message.createdAt)
                time = formattedTime
                isTimeVisible = false

                seenProfileImage = if (seenStatus == index && index != -1) {
                    viewModel.profileImage.get()
                } else {
                    null
                }
                executePendingBindings()

                itemView.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                itemView.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    }
                }

                layoutMessage.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                layoutMessage.setOnClickListener(object : DoubleClickListener() {
                    override fun onDoubleClick() {
                        if (message.receiverMediaPath != null) {
                            val imageUri: Uri? = message.receiverMediaPath?.let { File(it) }?.let {
                                FileProvider.getUriForFile(
                                    getKoinContext(), "${BuildConfig.APPLICATION_ID}.provider", it
                                )
                            }
                            imageUri?.let { getKoinActivity().openImageInPlayer(it) }
                        }
                    }

                    override fun onSingleClick() {
                        if (viewModel.isDeleteView.get() == true) {
                            callBack.onSelectClick(message.id)
                        } else {
                            isTimeVisible = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                isTimeVisible = false
                            }, 1000)
                        }
                    }
                })

                btnDownload.setOnClickListener {
                    isDownloadVisible = false
                    progress = 0

                    callBack.onDownloadClick(message)
                }

                layoutReply.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        callBack.onQuoteClick(bindingAdapterPosition)
                    }
                }
            }
        }
    }

    inner class VideoSentViewHolder(private var binding: ItemSendVideoBinding) : ViewHolder(binding) {
        override fun bind(message: Message, seenStatus: Int, callBack: QuoteClickListener) {
            binding.apply {
                if (message.replayData != null) {
                    isReply = true
                    replyName = if (message.replayData.sendBy == getUserPrefs().userId) {
                        "Reply to Yourself"
                    } else {
                        StringBuilder("Reply to ").append(message.replayData.senderData.fullName).toString()
                    }

                    if (message.replayData.mediaPath.isNullOrEmpty() && message.replayData.mediaUrl.isNullOrEmpty()) {
                        replyMessage = message.replayData.message
                    } else {
                        replyImage = if (!message.senderMediaPath.isNullOrEmpty()) {
                            message.replayData.mediaPath
                        } else {
                            getGistPrefs().chatMediaURL + message.replayData.mediaUrl
                        }
                    }
                } else {
                    isReply = false
                }

                isSelected = message.isSelected

                if (viewModel.sendingList.get()?.any { it.msgId == message.id } == true) {
                    isUploadVisible = false
                    isPlayVisible = false
                    progress = if ((message.progress != -1) && (message.progress != 0) && (message.progress != 100)) message.progress else 0
                } else {
                    isUploadVisible = false
                    isPlayVisible = true
                    progress = null
                }

                image = if (message.senderMediaPath != null) {
                    if (File(message.senderMediaPath).exists()) {
                        message.senderMediaPath
                    } else {
                        getGistPrefs().chatMediaURL + message.mediaUrl
                    }
                } else {
                    getGistPrefs().chatMediaURL + message.mediaUrl
                }

                /*if (message.senderMediaPath != null) {
                    if ((message.progress != -1) && (message.progress != 0) && (message.progress != 100)) {
                        progress = message.progress
                        isUploadVisible = false
                        isPlayVisible = false

                        image = message.senderMediaPath
                    } else {
                        progress = null

                        if (File(message.senderMediaPath).exists()) {
                            isUploadVisible = false
                            isPlayVisible = true

                            image = message.senderMediaPath
                        } else {
                            isUploadVisible = true
                            isPlayVisible = false

                            image = getGistPrefs().chatMediaURL + message.mediaUrl
                        }
                    }
                } else {
                    isPlayVisible = false
                    if ((message.progress != -1) && (message.progress != 0) && (message.progress != 100)) {
                        progress = message.progress
                        isUploadVisible = false
                    } else {
                        progress = null
                        isUploadVisible = true
                    }

                    image = getGistPrefs().chatMediaURL + message.mediaUrl
                }*/

                val formattedTime = convertDateToTime(message.createdAt)
                time = formattedTime
                isTimeVisible = false

                btnUpload.setOnClickListener {
                    isUploadVisible = false
                    progress = 0
                    isPlayVisible = false

                    callBack.onDownloadClick(message)
                }

                itemView.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                itemView.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    }
                }

                layoutMessage.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                layoutMessage.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        isTimeVisible = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            isTimeVisible = false
                        }, 1000)
                    }
                }

                btnPlay.setOnClickListener {
                    if (File(message.senderMediaPath!!).exists()) {
                        val imageUri: Uri = FileProvider.getUriForFile(
                            getKoinContext(), "${BuildConfig.APPLICATION_ID}.provider", // The authority of the FileProvider
                            File(message.senderMediaPath)
                        )
                        getKoinActivity().openVideoInPlayer(imageUri)
                    } else {
                        Toast.makeText(getKoinContext(), "Video not found", Toast.LENGTH_SHORT).show()
                    }
                }

                layoutReply.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        callBack.onQuoteClick(bindingAdapterPosition)
                    }
                }
            }
        }
    }

    inner class VideoReceiveViewHolder(private var binding: ItemReceiveVideoBinding) : ViewHolder(binding) {
        override fun bind(message: Message, seenStatus: Int, callBack: QuoteClickListener) {
            binding.apply {
                if (message.replayData != null) {
                    isReply = true
                    replyName = if (message.replayData.sendBy == getUserPrefs().userId) {
                        "Reply to Yourself"
                    } else {
                        StringBuilder("Reply to ").append(message.replayData.senderData.fullName).toString()
                    }

                    if (message.replayData.mediaPath.isNullOrEmpty() && message.replayData.mediaUrl.isNullOrEmpty()) {
                        replyMessage = message.replayData.message
                    } else {
                        replyImage = if (!message.senderMediaPath.isNullOrEmpty()) {
                            message.replayData.mediaPath
                        } else {
                            getGistPrefs().chatMediaURL + message.replayData.mediaUrl
                        }
                    }
                } else {
                    isReply = false
                }

                isSelected = message.isSelected

                seenProfileImage = if (seenStatus == index && index != -1) {
                    viewModel.profileImage.get()
                } else {
                    null
                }

                isPlayVisible = false

                if (viewModel.downloadingList.get()?.any { it.msgId == message.id } == true) {
                    isDownloadVisible = false
                    progress = if ((message.progress != -1) && (message.progress != 0) && (message.progress != 100)) message.progress else 0
                } else {
                    isDownloadVisible = true
                    progress = null
                }

                image = if (message.receiverMediaPath != null) {
                    if (File(message.receiverMediaPath!!).exists()) {
                        isDownloadVisible = false
                        isPlayVisible = true
                        message.receiverMediaPath
                    } else {
                        getGistPrefs().chatMediaURL + message.mediaUrl
                    }
                } else {
                    getGistPrefs().chatMediaURL + message.mediaUrl
                }

                btnDownload.setOnClickListener {
                    callBack.onDownloadClick(message)
                    isDownloadVisible = false
                }

                val formattedTime = convertDateToTime(message.createdAt)
                time = formattedTime
                isTimeVisible = false

                itemView.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                itemView.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    }
                }

                layoutMessage.setOnLongClickListener {
                    callBack.onSelectClick(message.id)
                    true
                }

                layoutMessage.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        isTimeVisible = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            isTimeVisible = false
                        }, 1000)
                    }
                }

                btnDownload.setOnClickListener {
                    isDownloadVisible = false
                    progress = 0
                    isPlayVisible = false

                    callBack.onDownloadClick(message)
                }

                btnPlay.setOnClickListener {
                    if (File(message.receiverMediaPath!!).exists()) {
                        val imageUri: Uri = FileProvider.getUriForFile(
                            getKoinContext(), "${BuildConfig.APPLICATION_ID}.provider", // The authority of the FileProvider
                            File(message.receiverMediaPath!!)
                        )
                        getKoinActivity().openVideoInPlayer(imageUri)
                    } else {
                        Toast.makeText(getKoinContext(), "Video not found", Toast.LENGTH_SHORT).show()
                    }
                }

                layoutReply.setOnClickListener {
                    if (viewModel.isDeleteView.get() == true) {
                        callBack.onSelectClick(message.id)
                    } else {
                        callBack.onQuoteClick(bindingAdapterPosition)
                    }
                }
            }
        }
    }

    inner class DateViewHolder(private var binding: ItemDateBinding) : ViewHolder(binding) {
        override fun bind(message: Message, seenStatus: Int, callBack: QuoteClickListener) {
            val dt = iso8601ToMillis(message.createdAt)
            binding.date = formatDateForChatGroupTitle(dt)
            binding.executePendingBindings()
        }
    }

    open class ViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        open fun bind(message: Message, seenStatus: Int, callBack: QuoteClickListener) {

        }
    }
}
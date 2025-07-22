package com.belive.dating.activities.chat

import android.app.ActionBar
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.activities.paywalls.subscriptions.subscription.SubscriptionActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.api.user.models.chat_file.FileDownloadModel
import com.belive.dating.api.user.models.chat_file.FileUploadModel
import com.belive.dating.api.user.models.friend_chat.ChatResponse
import com.belive.dating.api.user.models.friend_chat.DeleteMessageModel
import com.belive.dating.api.user.models.friend_chat.FriendChatResponse
import com.belive.dating.api.user.models.friend_chat.Message
import com.belive.dating.api.user.models.friend_chat.PaginateChatData
import com.belive.dating.api.user.models.friend_chat.TypingResponseModel
import com.belive.dating.constants.ChatType
import com.belive.dating.constants.EventConstants
import com.belive.dating.constants.SocketConstants
import com.belive.dating.databinding.ActivityChatBinding
import com.belive.dating.di.deepLinkViewModels
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.askPermissions
import com.belive.dating.extensions.focusField
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getRealPathFromUri
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.getVideoDuration
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.hasPermission
import com.belive.dating.extensions.hideKeyboard
import com.belive.dating.extensions.isOnlyActivity
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeUp
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.linear_layout_manager.WrapLinearLayoutManager
import com.belive.dating.helpers.helper_functions.socket.SocketManager
import com.belive.dating.helpers.helper_functions.swipe_replay.SwipeControllerActions
import com.belive.dating.helpers.helper_functions.swipe_replay.SwipeToReplayCallback
import com.belive.dating.services.enqueueDownload
import com.belive.dating.services.enqueueUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ChatActivity : NetworkReceiverActivity(), ChatAdapter.QuoteClickListener {

    val binding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: ChatViewModel

    val adapter by lazy {
        ChatAdapter(viewModel, this)
    }

    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (binding.rvChat.adapter != null) {
                val msgId = intent?.getIntExtra("index", -1) ?: return
                val isSuccess = intent.getBooleanExtra("isSuccess", false)
                if (isSuccess) {
                    viewModel.sendingList.set(viewModel.sendingList.get()?.apply {
                        val indexModel = find { it.msgId == msgId }
                        remove(indexModel)
                    })
                }
                val progress = intent.getIntExtra("progress", 0)
                val file = intent.getStringExtra("file")
                adapter.updateProgress(msgId, progress, file!!)
            }
        }
    }

    private val progressDownloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (binding.rvChat.adapter != null) {
                val msgId = intent?.getIntExtra("index", -1) ?: return
                val progress = intent.getIntExtra("progress", 0)
                val file = intent.getStringExtra("file")
                val isSuccess = intent.getBooleanExtra("isSuccess", false)
                if (isSuccess) {
                    viewModel.downloadingList.set(viewModel.downloadingList.get()?.apply {
                        val indexModel = find { it.msgId == msgId }
                        remove(indexModel)
                    })
                }
                adapter.updateProgress(msgId, progress, file!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(ChatActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(ChatActivity::class.java.simpleName)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.updateState()
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        viewModel.getState()
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

            // Set top padding for status bar
            view.setPadding(
                view.paddingLeft,
                statusBarHeight,
                view.paddingRight,
                if (imeHeight > 0) imeHeight else navBarHeight,
            )

            insets
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        viewModel = tryKoinViewModel(listOf(deepLinkViewModels))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initViews()

        clickListeners()

        observers()

        binding.root.post {
            listenEvents()
            observeNetwork()
        }
    }

    private fun observers() {
        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvChat.layoutManager as WrapLinearLayoutManager

                if (dy < 0 && layoutManager.findFirstCompletelyVisibleItemPosition() <= 2 && !viewModel.isLoadMore.get()!! && viewModel.isLoading.get() == false) {
                    if (viewModel.currentPage.get()!! < viewModel.totalPages.get()!!) {
                        viewModel.lastPosition.set(layoutManager.findLastVisibleItemPosition())
                        viewModel.isLoading.set(true)
                        viewModel.pid.set(viewModel.messageList.get()!![0].id)
                        viewModel.scrollState.set(true)
                        viewModel.isLoadMore.set(true)
                        viewModel.currentPage.set(viewModel.currentPage.get()!! + 1)

                        val obj = JSONObject()
                        obj.put("user_id", getUserPrefs().userId)
                        obj.put("friend_id", viewModel.friendId.get())
                        obj.put("page", viewModel.currentPage.get()!!)
                        SocketManager.emit(SocketConstants.GET_PAGINATION_MESSAGE, obj)
                    }
                }
            }
        })

        binding.edtMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.manageTyping(s.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun updateRecyclerView() {
        val index: Int
        val filteredIds =
            viewModel.messageList.get()!!.filter { !it.isRead && (it.sendBy == getUserPrefs().userId) && (it.mediaType != 0) }.map { it.id }
        val filteredIdsAll = viewModel.messageList.get()!!.filter { (it.sendBy == getUserPrefs().userId) && (it.mediaType != 0) }.map { it.id }

        if (filteredIdsAll.isNotEmpty()) {
            if (filteredIds.isNotEmpty()) {
                val index1 = filteredIdsAll.indexOfLast { it == filteredIds[0] }
                index = if (index1 > 0) {
                    val id = filteredIdsAll.find { it == filteredIdsAll[index1 - 1] }
                    viewModel.messageList.get()!!.indexOfLast { it.id == id && it.mediaType != 0 }
                } else {
                    -1
                }
            } else {
                val id: Int = if (filteredIdsAll.size - 1 > 0) {
                    val inx = filteredIdsAll.size - 1
                    filteredIdsAll[inx]
                } else {
                    filteredIdsAll[0]
                }
                index = viewModel.messageList.get()!!.indexOfLast { it.id == id && it.mediaType != 0 }
            }
        } else {
            index = -1
        }

        adapter.index = index

        if (viewModel.isPaginate.get()!! && viewModel.replyId.get() != null) {
            val inx = viewModel.messageList.get()!!.indexOfLast { it.id == viewModel.replyId.get() && it.mediaType != 0 }
            if (inx != -1) {
                scrollReplyMsg()
            }
        } else {
            viewModel.isPaginate.set(false)
            viewModel.replyId.set(null)
            if (viewModel.scrollState.get() == true) {
                val scrollIndex = viewModel.messageList.get()!!.indexOfLast { it.id == viewModel.pid.get() }
                binding.rvChat.smoothScrollToPosition(scrollIndex + (viewModel.lastPosition.get()!! - 1))
                viewModel.scrollState.set(false)
                viewModel.isLoading.set(false)
            } else {
                scrollRecycleView()
            }
        }
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isDeleteView.get() == true) {
                    viewModel.disableDeleteView()
                    adapter.asyncListDiffer.submitList(viewModel.messageList.get()?.let { ArrayList(it) })
                    adapter.notifyItemRangeChanged(0, viewModel.messageList.get()?.size ?: 0)
                } else if (viewModel.mediaSendType.get() != null) {
                    viewModel.mediaSendType.set(null)
                } else if (viewModel.isReply.get() == true) {
                    binding.btnCancelReply.performClick()
                } else {
                    if (isOnlyActivity()) {
                        startActivity(Intent(this@ChatActivity, MainActivity::class.java).apply { putExtra("display_splash", false) })
                    } else {
                        finish()
                    }
                    swipeLeft()
                }
            }
        })

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnClose.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnDelete.setOnClickListener {
            if (viewModel.deleteCount.get()!! > 0) {
                deleteMessagesDialog.show()
            } else {
                Toast.makeText(this@ChatActivity, "Please select at least one message to delete", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancelReply.setOnClickListener {
            viewModel.isReply.set(false)
            viewModel.toReplyMessage.set(null)
            viewModel.toReplyImage.set(null)
            viewModel.toReplyName.set(null)
        }

        binding.btnAttachment.setOnClickListener {
            if (viewModel.mediaSendType.get() == null) {
                hideKeyboard()
                viewModel.mediaSendType.set(MediaSendType.NOT_SELECTED)
            } else {
                viewModel.mediaSendType.set(null)
            }
        }

        binding.layoutVideo.setOnClickListener {
            viewModel.mediaSendType.set(MediaSendType.VIDEO)
            openGallery(true)
        }

        binding.btnVideo.setOnClickListener {
            binding.layoutVideo.performClick()
        }

        binding.layoutImage.setOnClickListener {
            viewModel.mediaSendType.set(MediaSendType.IMAGE)
            openGallery(false)
        }

        binding.btnImage.setOnClickListener {
            binding.layoutImage.performClick()
        }

        binding.btnSend.setOnClickListener {
            if (binding.btnSend.tag == "send") {
                if ((viewModel.messageSend.get()?.isNotEmpty() == true) && (viewModel.messageSend.get()?.trim()?.isNotEmpty() == true)) {
                    /*if (adapter.itemCount > 0) {
                        binding.rvChat.post {
                            binding.rvChat.scrollToPosition(0)
                        }
                    }*/

                    val message = viewModel.messageSend.get()
                    viewModel.messageSend.set("")

                    val obj = JSONObject()
                    obj.put("sender_id", getUserPrefs().userId)
                    obj.put("receiver_id", viewModel.friendId.get())
                    obj.put("message", message)
                    obj.put("is_media", false)
                    obj.put("sender_media_path", "")
                    obj.put("message_replay_id", viewModel.replyId.get())
                    obj.put("media_type", viewModel.replyType.get())

                    if (viewModel.isReply.get() == true) {
                        binding.btnCancelReply.performClick()
                    }

                    SocketManager.emit(SocketConstants.MESSAGE, obj)
                }
            } else {
                startActivity(Intent(getKoinActivity(), SubscriptionActivity::class.java))
                swipeUp()
            }
        }
    }

    private fun initViews() {
        viewModel.friendId.set(intent.getIntExtra("friend_id", -1))

        updateSendMessageLayout()

        LocalBroadcastManager.getInstance(this).registerReceiver(progressReceiver, IntentFilter("FILE_UPLOAD_PROGRESS"))
        LocalBroadcastManager.getInstance(this).registerReceiver(progressDownloadReceiver, IntentFilter("FILE_DOWNLOAD_PROGRESS"))

        binding.rvChat.layoutManager = WrapLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = adapter

        val messageSwipeController = SwipeToReplayCallback(this@ChatActivity, object : SwipeControllerActions {
            override fun showReplyUI(position: Int) {
                if (!viewModel.messageList.get()!![position].isMedia && (viewModel.messageList.get()!![position].mediaType == 2 || viewModel.messageList.get()!![position].mediaType == 3) && viewModel.messageList.get()!![position].mediaUrl.isNullOrBlank()) {

                } else {
                    showReplayUI(viewModel.messageList.get()!![position])
                }
            }
        }, viewModel.messageList.get()!!)

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(binding.rvChat)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val outRect = Rect()
            binding.layoutMedia.getGlobalVisibleRect(outRect)

            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                // Touch is outside the layout
                viewModel.mediaSendType.set(null)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun updateSendMessageLayout() {
        val pkgName = getUserPrefs().activePackage

        if (pkgName == null) {
            val messageDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(this, R.drawable.ic_lock)!!)
            messageDrawable.setTint(ContextCompat.getColor(this, R.color.gold_plan))
            binding.btnSend.setImageDrawable(messageDrawable)
            binding.btnSend.tag = "lock"
            binding.btnAttachment.visibility = View.GONE
            (binding.edtMessage.layoutParams as ConstraintLayout.LayoutParams).marginStart = getDimensionPixelOffset(com.intuit.sdp.R.dimen._10sdp)
        } else {
            binding.btnSend.setImageResource(R.drawable.ic_send)
            binding.btnSend.tag = "send"
            binding.btnAttachment.visibility = View.VISIBLE
            (binding.edtMessage.layoutParams as ConstraintLayout.LayoutParams).marginStart = 0
        }
    }

    private val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
    )
    else arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    private val videoPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
        android.Manifest.permission.READ_MEDIA_VIDEO,
    )
    else arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    private fun openGallery(isVideo: Boolean) {
        if (isVideo) {
            if (!hasPermission(*videoPermission)) {
                askPermissions(videoPermission, PERMISSION_CODE)
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                videoResult.launch(intent)
            }
        } else {
            if (!hasPermission(*imagePermission)) {
                askPermissions(imagePermission, PERMISSION_CODE)
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                imageResult.launch(intent)
            }
        }
    }

    private fun scrollRecycleView() {
        logger("--scroll--", "scrollRecycleView")

        if (adapter.itemCount > 0) {
            binding.rvChat.post {
                binding.rvChat.smoothScrollToPosition(viewModel.messageList.get()!!.size - 1)
            }
        }
    }

    private fun scrollReplyMsg() {
        logger("--scroll--", "scrollReplyMsg")

        val inx = viewModel.messageList.get()!!.indexOfLast { it.id == viewModel.replyId.get() && it.mediaType != 0 }
        binding.rvChat.smoothScrollToPosition(inx)

        viewModel.blinkPosition.set(inx)
        adapter.notifyItemChanged(inx)

        viewModel.isPaginate.set(false)

        viewModel.replyId.set(null)
    }

    fun showReplayUI(message: Message) {
        viewModel.isReply.set(true)

        binding.root.postDelayed({
            viewModel.replyId.set(message.id)

            if (message.sendBy == getUserPrefs().userId) {
                viewModel.toReplyName.set("You")
            } else {
                viewModel.toReplyName.set(viewModel.profileName.get())
            }
            when (message.mediaType) {
                ChatType.TYPE_TEXT.value -> {
                    viewModel.replyType.set(ChatType.TYPE_TEXT.value)
                    viewModel.toReplyMessage.set(message.message)
                    viewModel.toReplyImage.set(null)
                    binding.edtMessage.focusField()
                }

                ChatType.TYPE_IMAGE.value -> {
                    binding.edtMessage.focusField()
                    viewModel.toReplyMessage.set(null)

                    if (message.senderMediaPath != null) {
                        if (File(message.senderMediaPath).exists()) {
                            viewModel.toReplyImage.set(message.senderMediaPath)
                        } else {
                            viewModel.toReplyImage.set(getGistPrefs().chatMediaURL + message.mediaUrl)
                        }
                    } else {
                        viewModel.toReplyImage.set(getGistPrefs().chatMediaURL + message.mediaUrl)
                    }
                }

                ChatType.TYPE_VIDEO.value -> {
                    binding.edtMessage.requestFocus()
                    viewModel.toReplyMessage.set(null)

                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(
                        binding.edtMessage, InputMethodManager.SHOW_IMPLICIT
                    )

                    if (message.sendBy == getUserPrefs().userId) {
                        if (File(message.senderMediaPath!!).exists()) {
                            viewModel.toReplyImage.set(message.senderMediaPath)
                        } else {
                            viewModel.toReplyImage.set(getGistPrefs().chatMediaURL + message.mediaUrl)
                        }
                    } else {
                        if (File(message.receiverMediaPath!!).exists()) {
                            viewModel.toReplyImage.set(message.receiverMediaPath)
                        } else {
                            viewModel.toReplyImage.set(getGistPrefs().chatMediaURL + message.mediaUrl)
                        }
                    }
                }
            }
        }, 50)
    }

    private val permissionDialog: Dialog by lazy {
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.dialog_permission_required)
            setCanceledOnTouchOutside(true)
            window?.setDimAmount(0.75f)
            window?.apply {
                setLayout(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(android.R.color.transparent)
            }
            val messageView = findViewById<TextView>(R.id.txt_message_permission)
            messageView.text =
                StringBuilder().append("To select ${if (viewModel.mediaSendType.get() == MediaSendType.IMAGE) "videos" else "images"} from your device, we need access to your media storage. Please click below and allow it from settings.")
        }
    }

    private val deleteMessagesDialog: Dialog by lazy {
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.dialog_delete_messages)
            setCanceledOnTouchOutside(true)
            window?.setDimAmount(0.75f)
            window?.apply {
                setLayout(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(android.R.color.transparent)
            }
            val txtTitle = findViewById<TextView>(R.id.txt_title)
            val txtDeleteForMe = findViewById<TextView>(R.id.txt_delete_for_me)
            val txtDeleteForEveryone = findViewById<TextView>(R.id.txt_delete_for_everyone)
            val txtCancel = findViewById<TextView>(R.id.txt_cancel)

            var jsonIds = JSONArray()
            var listSize: Int
            var lastId: Int
            var bool = false

            setOnShowListener {
                txtTitle.text = StringBuilder().append("Delete ").append(viewModel.deleteCount.get()).append(" messages?")

                val allIds = viewModel.messageList.get()!!.filter { it.isSelected }.map { it.id }
                val isDeleteForEveryOneVisible = viewModel.messageList.get()!!.none { it.isSelected && it.sendBy == viewModel.friendId.get() }
                txtDeleteForEveryone.visibility = if (isDeleteForEveryOneVisible) View.VISIBLE else View.GONE

                jsonIds = JSONArray()
                allIds.forEach {
                    jsonIds.put(it)
                }
                listSize = viewModel.messageList.get()!!.size
                lastId = viewModel.messageList.get()!![listSize - 1].id
                bool = allIds.any { it == lastId }
            }

            txtDeleteForMe.setOnClickListener {
                val obj = JSONObject()
                obj.put("delete_for_everyone", false)
                obj.put("is_last_msg", bool)
                obj.put("friend_id", viewModel.friendId.get())
                obj.put("user_id", getUserPrefs().userId)
                obj.put("messageIds", jsonIds)
                logger("--delete--", obj)
                SocketManager.emit(SocketConstants.DELETE_MESSAGE, obj)

                viewModel.disableDeleteView()

                adapter.asyncListDiffer.submitList(viewModel.messageList.get()?.let { ArrayList(it) })

                deleteMessagesDialog.dismiss()
            }

            txtDeleteForEveryone.setOnClickListener {
                val obj = JSONObject()
                obj.put("delete_for_everyone", true)
                obj.put("is_last_msg", bool)
                obj.put("friend_id", viewModel.friendId.get())
                obj.put("user_id", getUserPrefs().userId)
                obj.put("messageIds", jsonIds)
                logger("--delete--", obj)
                SocketManager.emit(SocketConstants.DELETE_MESSAGE, obj)

                viewModel.disableDeleteView()

                adapter.asyncListDiffer.submitList(viewModel.messageList.get()?.let { ArrayList(it) })

                deleteMessagesDialog.dismiss()
            }

            txtCancel.setOnClickListener {
                deleteMessagesDialog.dismiss()
            }
        }
    }

    private val PERMISSION_CODE = 103

    private val videoResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.isReply.set(false)
        viewModel.toReplyMessage.set(null)
        viewModel.toReplyImage.set(null)
        viewModel.toReplyName.set(null)

        if (it.resultCode == RESULT_OK) {
            val data = it.data

            val selectedImageUris: ArrayList<Uri> = arrayListOf()
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    selectedImageUris.add(imageUri)
                }
            } else if (data?.data != null) {
                val imageUri = data.data
                if (imageUri != null) {
                    selectedImageUris.add(imageUri)
                }
            }

            if (selectedImageUris.isNotEmpty()) {
                selectedImageUris.forEach { uri ->
                    val path = getRealPathFromUri(uri)

                    if (path != null) {
                        val realPath = getRealPathFromUri(uri)
                        val duration = getVideoDuration(uri)

                        if (duration <= 0L) {
                            Toast.makeText(this, "Upload File not supported", Toast.LENGTH_LONG).show()

                            viewModel.mediaSendType.set(MediaSendType.NOT_SELECTED)
                        } else {
                            val obj = JSONObject()
                            obj.put("sender_id", getUserPrefs().userId)
                            obj.put("receiver_id", viewModel.friendId.get())
                            obj.put("message", "Video")
                            obj.put("is_media", false)
                            obj.put("sender_media_path", realPath)
                            obj.put("message_replay_id", viewModel.replyId.get())
                            obj.put("media_type", viewModel.mediaSendType.get()?.value)
                            SocketManager.emit(SocketConstants.MESSAGE, obj)
                        }
                    }
                }
            }
        }
        viewModel.mediaSendType.set(null)
    }

    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { it ->
        viewModel.isReply.set(false)
        viewModel.toReplyMessage.set(null)
        viewModel.toReplyImage.set(null)
        viewModel.toReplyName.set(null)

        if (it.resultCode == RESULT_OK) {
            val data = it.data
            val files: MutableList<String> = mutableListOf()
            data?.let {
                val clipData = it.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val file = getRealPathFromUri(uri)
                        file?.let { it1 -> files.add(it1) }
                    }
                } else {
                    val file = it.data?.let { it1 ->
                        getRealPathFromUri(it1)
                    }
                    file?.let { it1 -> files.add(it1) }
                }
            }

            for (i in 0 until files.size) {
                if (File(files[i]).length() in 1..104857600) {
                    val obj = JSONObject()
                    obj.put("sender_id", getUserPrefs().userId)
                    obj.put("receiver_id", viewModel.friendId.get())
                    obj.put("message", "Image")
                    obj.put("is_media", false)
                    obj.put("sender_media_path", files[i])
                    obj.put("message_replay_id", viewModel.replyId.get())
                    obj.put("media_type", viewModel.mediaSendType.get()?.value)

                    logger("--message--", gsonString(obj))

                    SocketManager.emit(SocketConstants.MESSAGE, obj)
                } else {
                    Toast.makeText(this@ChatActivity, "uploaded Image size maximum 100MB", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.mediaSendType.set(null)
    }

    private fun readNotification(notificationId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.readNotification(notificationId).collect {
                when (it.status) {
                    Status.LOADING -> {

                    }

                    Status.SIGN_OUT -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@ChatActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }
                    }

                    Status.ADMIN_BLOCKED -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@ChatActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }
                    }

                    Status.ERROR -> {
                        logger("--error--", "notification read error: ${gsonString(it)}")
                    }

                    Status.SUCCESS -> {
                        val unreadNotification = it.data?.get("unreadNotification")?.asJsonPrimitive?.asInt
                        if (unreadNotification != null) {
                            getUserPrefs().unreadNotificationCount = unreadNotification

                            logger("--notification--", "readNotification: $unreadNotification")

                            val intent = Intent("NOTIFICATION_COUNT")
                            intent.putExtra("isNotificationCountChanged", true)
                            LocalBroadcastManager.getInstance(this@ChatActivity).sendBroadcast(intent)
                        }
                    }
                }
            }
        }
    }

    private fun authOut() {
        LoadingDialog.show(this)

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@ChatActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            if (viewModel.mediaSendType.get() == MediaSendType.VIDEO) {
                intent.setType("video/*")
                videoResult.launch(intent)
            } else {
                intent.setType("image/*")
                imageResult.launch(intent)
            }
        } else {
            var openSettings = false

            for (permission in permissions) {
                if (!shouldShowRequestPermissionRationale(permission)) {
                    openSettings = true
                    break
                }
            }

            if (openSettings) {
                permissionDialog.show()

                val btnContinue = permissionDialog.findViewById<TextView>(R.id.btn_open_settings)
                btnContinue.setOnClickListener {
                    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                    with(intent) {
                        data = Uri.fromParts("package", packageName, null)
                        addCategory(CATEGORY_DEFAULT)
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                        addFlags(FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }
                    startActivity(intent)

                    permissionDialog.dismiss()
                }
            }
        }
    }

    private fun getChatList() {
        if (viewModel.messageList.get().isNullOrEmpty()) {
            val appLinkIntent: Intent = intent


            val userId: String? =
                appLinkIntent.data?.getQueryParameter("userId") ?: if (appLinkIntent.hasExtra("userId")) appLinkIntent.getIntExtra("userId", -1)
                    .toString() else null
            val notificationID: String? =
                appLinkIntent.data?.getQueryParameter("notiId") ?: if (appLinkIntent.hasExtra("notiId")) appLinkIntent.getIntExtra("notiId", -1)
                    .toString() else null

            viewModel.friendId.set(userId?.toInt())

            logger("--message--", "userId: ${getUserPrefs().userId}")
            logger("--message--", "friendId: $userId")
            logger("--message--", "notificationID: $notificationID")

            if ((userId != null) && (userId != "-1") && (getUserPrefs().userToken != null)) {
                if ((notificationID != null) && (notificationID != "-1")) {
                    readNotification(notificationID.toInt())
                }

                if (!SocketManager.connected) {
                    val map = mutableMapOf<String, Any?>()
                    map["user_id"] = getUserPrefs().userId
                    map["status"] = true

                    SocketManager.emit(SocketConstants.SET_ONLINE_STATUS, map)

                    Handler(Looper.getMainLooper()).post {
                        val json = JSONObject()
                        json.put("user_id", getUserPrefs().userId)
                        json.put("friend_id", viewModel.friendId.get())

                        SocketManager.emit(SocketConstants.GET_FRIEND_CHAT, json)
                    }
                } else {
                    SocketManager.connect()

                    Handler(Looper.getMainLooper()).post {
                        val map = mutableMapOf<String, Any?>()
                        map["user_id"] = getUserPrefs().userId
                        map["status"] = true

                        SocketManager.emit(SocketConstants.SET_ONLINE_STATUS, map)

                        Handler(Looper.getMainLooper()).post {
                            val json = JSONObject()
                            json.put("user_id", getUserPrefs().userId)
                            json.put("friend_id", viewModel.friendId.get())

                            SocketManager.emit(SocketConstants.GET_FRIEND_CHAT, json)
                        }
                    }
                }
            } else {
                authOut()
            }
        }
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        getChatList()
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            getChatList()
        }
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.UPDATE_PURCHASE -> {
                logger("--event--", "UPDATE_PURCHASE")

                updateSendMessageLayout()
            }

            SocketConstants.GET_FRIEND_CHAT_RESPONSE -> {
                logger("--event--", "GET_FRIEND_CHAT_RESPONSE")

                val selectedMessages = viewModel.messageList.get()?.filter { it.isSelected } ?: ArrayList<Message>()

                val friendChatResponse = value as FriendChatResponse
                logger("--${SocketConstants.GET_FRIEND_CHAT_RESPONSE}--", gsonString(friendChatResponse))

                viewModel.profileName.set(friendChatResponse.friendData.fullName)
                viewModel.profileImage.set(friendChatResponse.friendData.userImage)
                viewModel.isOnline.set(friendChatResponse.isOnline)
                viewModel.isLoading.set(false)
                viewModel.totalPages.set(friendChatResponse.chatData?.totalPages)
                viewModel.currentPage.set(friendChatResponse.chatData?.currentPage)

                if (friendChatResponse.chatData != null) {
                    friendChatResponse.chatData.messages.sortBy { it.id }
                    val list = viewModel.groupChatMessagesByDate(friendChatResponse.chatData.messages)
                    friendChatResponse.chatData.messages = list

                    viewModel.addDateTitles(list, selectedMessages) {
                        adapter.asyncListDiffer.submitList(viewModel.messageList.get()?.let { ArrayList(it) })

                        val messageList = viewModel.messageList.get()
                        val filteredIds =
                            messageList?.filter { !it.isRead && (it.mediaType != 0) && (it.sendBy != getUserPrefs().userId) }?.map { it.id }

                        if (filteredIds?.isNotEmpty() == true) {
                            val jsonObject = JSONObject()
                            val jsonIds = JSONArray()
                            filteredIds.forEach {
                                jsonIds.put(it)
                            }
                            jsonObject.put("message_ids", jsonIds)
                            jsonObject.put("friend_id", viewModel.friendId.get())
                            jsonObject.put("user_id", getUserPrefs().userId)

                            SocketManager.emit(SocketConstants.ON_CHAT_STATUS_UPDATE_REQUEST, jsonObject)

                            logger("--update_unread--", filteredIds)
                        }

                        updateRecyclerView()

                        val downloadList = getUserPrefs().runningFileDownloadList
                        viewModel.downloadingList.set(downloadList)

                        val sendingFiles: ArrayList<FileUploadModel> = arrayListOf()

                        val ids = messageList?.filter {
                            (it.sendBy == getUserPrefs().userId) && ((it.mediaType == MediaSendType.IMAGE.value) || (it.mediaType == MediaSendType.VIDEO.value)) && !it.isMedia
                        }?.map { it.id }
                        if (ids != null) {
                            for (item in ids) {
                                if (viewModel.sendingList.get()!!.none { it.msgId == item }) {
                                    val inx = messageList.indexOfLast { it.id == item }
                                    sendingFiles.add(
                                        FileUploadModel(
                                            file = messageList[inx].senderMediaPath,
                                            userId = getUserPrefs().userId,
                                            friendId = viewModel.friendId.get()!!,
                                            msgId = messageList[inx].id,
                                            mediaType = messageList[inx].mediaType,
                                            sending = 0,
                                        )
                                    )
                                }
                            }
                        }

                        sendingFiles.removeAll { if (it.file == null) true else !File(it.file).exists() }

                        if (sendingFiles.isNotEmpty()) {
                            applicationContext.enqueueUpload(sendingFiles)
                        }

                        sendingFiles.forEach { fileModel ->
                            if (viewModel.sendingList.get()?.none { it.msgId == fileModel.msgId } == true) {
                                viewModel.sendingList.set(viewModel.sendingList.get()?.apply {
                                    add(fileModel)
                                } ?: arrayListOf(fileModel))
                            }
                        }
                    }
                }
            }

            SocketConstants.ON_CHAT_RESPONSE -> {
                logger("--event--", "ON_CHAT_RESPONSE")

                val selectedMessages = viewModel.messageList.get()?.filter { it.isSelected } ?: ArrayList<Message>()

                val chatResponse = value as ChatResponse
                logger("--${SocketConstants.ON_CHAT_RESPONSE}--", gsonString(chatResponse))

                viewModel.isLoading.set(false)
                viewModel.totalPages.set(chatResponse.chatData.totalPages)
                viewModel.currentPage.set(chatResponse.chatData.currentPage)

                chatResponse.chatData.messages.sortBy { it.id }
                val list = viewModel.groupChatMessagesByDate(chatResponse.chatData.messages)
                chatResponse.chatData.messages = list

                viewModel.addDateTitles(list, selectedMessages) {
                    adapter.asyncListDiffer.submitList(viewModel.messageList.get()?.let { ArrayList(it) })

                    val messageList = viewModel.messageList.get()
                    val unreadIds =
                        messageList?.filter { !it.isRead && !it.isDate && (it.mediaType != 0) && (it.sendBy != getUserPrefs().userId) }?.map { it.id }

                    if (unreadIds?.isNotEmpty() == true) {
                        val jsonObject = JSONObject()
                        val jsonIds = JSONArray()
                        unreadIds.forEach {
                            jsonIds.put(it)
                        }
                        jsonObject.put("message_ids", jsonIds)
                        jsonObject.put("friend_id", viewModel.friendId.get())
                        jsonObject.put("user_id", getUserPrefs().userId)

                        SocketManager.emit(SocketConstants.ON_CHAT_STATUS_UPDATE_REQUEST, jsonObject)
                    } else {
                        updateRecyclerView()

                        val downloadList = getUserPrefs().runningFileDownloadList
                        viewModel.downloadingList.set(downloadList)

                        val sendingFiles: ArrayList<FileUploadModel> = arrayListOf()

                        val ids = messageList?.filter {
                            (it.sendBy == getUserPrefs().userId) && ((it.mediaType == MediaSendType.IMAGE.value) || (it.mediaType == MediaSendType.VIDEO.value)) && !it.isMedia
                        }?.map { it.id }
                        if (ids != null) {
                            for (item in ids) {
                                if (viewModel.sendingList.get()!!.none { it.msgId == item }) {
                                    val inx = messageList.indexOfLast { it.id == item }
                                    sendingFiles.add(
                                        FileUploadModel(
                                            file = messageList[inx].senderMediaPath,
                                            userId = getUserPrefs().userId,
                                            friendId = viewModel.friendId.get()!!,
                                            msgId = messageList[inx].id,
                                            mediaType = messageList[inx].mediaType,
                                            sending = 0,
                                        )
                                    )
                                }
                            }
                        }

                        sendingFiles.removeAll { if (it.file == null) true else !File(it.file).exists() }

                        if (sendingFiles.isNotEmpty()) {
                            applicationContext.enqueueUpload(sendingFiles)
                        }

                        sendingFiles.forEach { fileModel ->
                            if (viewModel.sendingList.get()?.none { it.msgId == fileModel.msgId } == true) {
                                viewModel.sendingList.set(viewModel.sendingList.get()?.apply {
                                    add(fileModel)
                                } ?: arrayListOf(fileModel))
                            }
                        }
                    }
                }
            }

            SocketConstants.ON_PAGINATE_RESPONSE -> {
                logger("--event--", "ON_PAGINATE_RESPONSE")

                val chatData = value as PaginateChatData
                viewModel.updatePaginationData(chatData)

                adapter.asyncListDiffer.submitList(viewModel.messageList.get()?.let { ArrayList(it) })

                viewModel.isLoading.set(false)
            }

            SocketConstants.ON_DELETE_MESSAGE_RESPONSE -> {
                logger("--event--", "ON_DELETE_MESSAGE_RESPONSE")

                val deleteMessageModel = value as DeleteMessageModel
                if (deleteMessageModel.userId == getUserPrefs().userId) {
                    viewModel.messageList.set(viewModel.messageList.get()!!.apply {
                        removeAll { it.id in deleteMessageModel.messageIds }
                    })
                    viewModel.totalPages.set(deleteMessageModel.totalPage)

                    viewModel.messageList.set(viewModel.messageList.get()!!.apply {
                        removeAll { (it.mediaType == 0) || (it.id == -1) }
                    })
                    viewModel.messageList.set(viewModel.messageList.get()!!.onEach {
                        it.isProfile = false
                    })

                    viewModel.messageList.set(viewModel.messageList.get()?.apply {
                        sortBy { it.id }
                    })
                    val list = viewModel.groupChatMessagesByDate(viewModel.messageList.get()!!)
                    viewModel.messageList.set(list)

                    viewModel.addDateTitles(viewModel.messageList.get()!!, ArrayList()) {
                        adapter.asyncListDiffer.submitList(viewModel.messageList.get()?.let { ArrayList(it) })

                        val messageList = viewModel.messageList.get()
                        val filteredIds =
                            messageList?.filter { !it.isRead && (it.mediaType != 0) && (it.sendBy != getUserPrefs().userId) }?.map { it.id }

                        if (filteredIds?.isNotEmpty() == true) {
                            val jsonObject = JSONObject()
                            val jsonIds = JSONArray()
                            filteredIds.forEach {
                                jsonIds.put(it)
                            }
                            jsonObject.put("message_ids", jsonIds)
                            jsonObject.put("friend_id", viewModel.friendId.get())
                            jsonObject.put("user_id", getUserPrefs().userId)

                            SocketManager.emit(SocketConstants.ON_CHAT_STATUS_UPDATE_REQUEST, jsonObject)
                        } else {
                            updateRecyclerView()

                            val downloadList = getUserPrefs().runningFileDownloadList
                            viewModel.downloadingList.set(downloadList)

                            val sendingFiles: ArrayList<FileUploadModel> = arrayListOf()

                            val ids = messageList?.filter {
                                (it.sendBy == getUserPrefs().userId) && ((it.mediaType == MediaSendType.IMAGE.value) || (it.mediaType == MediaSendType.VIDEO.value)) && !it.isMedia
                            }?.map { it.id }
                            if (ids != null) {
                                for (item in ids) {
                                    if (viewModel.sendingList.get()!!.none { it.msgId == item }) {
                                        val inx = messageList.indexOfLast { it.id == item }
                                        sendingFiles.add(
                                            FileUploadModel(
                                                file = messageList[inx].senderMediaPath,
                                                userId = getUserPrefs().userId,
                                                friendId = viewModel.friendId.get()!!,
                                                msgId = messageList[inx].id,
                                                mediaType = messageList[inx].mediaType,
                                                sending = 0,
                                            )
                                        )
                                    }
                                }
                            }

                            sendingFiles.removeAll { if (it.file == null) true else !File(it.file).exists() }

                            if (sendingFiles.isNotEmpty()) {
                                applicationContext.enqueueUpload(sendingFiles)
                            }

                            sendingFiles.forEach { fileModel ->
                                if (viewModel.sendingList.get()?.none { it.msgId == fileModel.msgId } == true) {
                                    viewModel.sendingList.set(viewModel.sendingList.get()?.apply {
                                        add(fileModel)
                                    } ?: arrayListOf(fileModel))
                                }
                            }
                        }
                    }
                }
            }

            SocketConstants.TYPING_RESPONSE -> {
                logger("--event--", "TYPING_RESPONSE")

                val typingResponse = value as TypingResponseModel

                if (viewModel.friendId.get() == typingResponse.userId) {
                    if (typingResponse.isTyping) {
                        viewModel.isFriendTyping.set(true)
                    } else {
                        viewModel.isFriendTyping.set(false)
                    }
                }
            }
        }
    }

    override fun onQuoteClick(position: Int) {
        viewModel.replyId.set(viewModel.messageList.get()!![position].replayData?.id!!)
        val inx = viewModel.messageList.get()!!.indexOfLast { it.id == viewModel.replyId.get() && it.mediaType != 0 }
        if (inx == -1) {
            val obj = JSONObject()
            obj.put("friend_id", viewModel.friendId.get())
            obj.put("user_id", getUserPrefs().userId)
            obj.put("messageReplayId", viewModel.replyId.get())
            SocketManager.emit(SocketConstants.GET_UPTO_REPLY_MESSAGE, obj)
        } else {
            binding.rvChat.smoothScrollToPosition(inx)
            viewModel.blinkPosition.set(inx)
            adapter.notifyItemChanged(inx)
        }
    }

    override fun onSelectClick(id: Int) {
        if (viewModel.sendingList.get()?.any { it.msgId == id } == true) {
            Toast.makeText(this, "You cannot select sending file", Toast.LENGTH_SHORT).show()
        } else if (viewModel.downloadingList.get()?.any { it.msgId == id } == true) {
            Toast.makeText(this, "You cannot select downloading file", Toast.LENGTH_SHORT).show()
        } else {
            if (viewModel.isDeleteView.get() == false) {
                hideKeyboard()
                viewModel.isDeleteView.set(true)
            }

            val index = viewModel.messageList.get()!!.indexOfLast { id == it.id }

            if (index != -1) {
                viewModel.messageList.set(viewModel.messageList.get()!!.apply {
                    set(index, viewModel.messageList.get()!![index].apply {
                        isSelected = !isSelected
                    })
                })
                adapter.asyncListDiffer.submitList(viewModel.messageList.get()?.let { ArrayList(it) })
                adapter.notifyItemChanged(index)
            }

            val deleteCount = viewModel.messageList.get()!!.count { it.isSelected }
            viewModel.deleteCount.set(deleteCount)
        }
    }

    override fun onViewClick(imgUrl: String) {

    }

    override fun onDownloadClick(chatMessage: Message) {
        val list: ArrayList<FileDownloadModel> = arrayListOf()
        list.add(
            FileDownloadModel(
                file = getGistPrefs().chatMediaURL + chatMessage.mediaUrl,
                userId = getUserPrefs().userId,
                friendId = viewModel.friendId.get()!!,
                msgId = chatMessage.id,
                mediaType = chatMessage.mediaType,
                mediaName = chatMessage.mediaName,
            )
        )
        viewModel.downloadingList.get().let { it ->
            if (it.isNullOrEmpty()) viewModel.downloadingList.set(list) else viewModel.downloadingList.set(it.apply { addAll(list) })
        }
        applicationContext.enqueueDownload(list)
    }
}
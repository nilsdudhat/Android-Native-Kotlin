package com.belive.dating.activities.dashboard.fragments.message

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.activities.EventBusFragment
import com.belive.dating.activities.chat.ChatActivity
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.api.user.models.friend.FriendDetails
import com.belive.dating.api.user.models.friend_chat.TypingResponseModel
import com.belive.dating.constants.EventConstants
import com.belive.dating.constants.SocketConstants
import com.belive.dating.databinding.FragmentMessageBinding
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.navGraphViewModel
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.socket.SocketManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MessageFragment : EventBusFragment(), FriendMessageAdapter.FriendClickListener, FriendMessageAdapter.OnSwipeStateListener, FriendMessageAdapter.OnBlockListener {

    val binding: FragmentMessageBinding by lazy {
        FragmentMessageBinding.inflate(layoutInflater)
    }

    val viewModel: MessageViewModel by navGraphViewModel(R.id.navigation_message)

    val adapter by lazy {
        FriendMessageAdapter(viewModel, this, this, this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.updateState()
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        viewModel.getState()
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        listenEvents()

        initViews()

        if (((requireActivity() as MainActivity).viewModel.isInitLoading.get() == false) && (viewModel.isDataLoaded.get() == false)) {
            SocketManager.emit(SocketConstants.GET_FRIEND_LIST, getUserPrefs().userId)
        }
    }

    private fun initViews() {
        if (binding.rvFriends.layoutManager == null) {
            binding.rvFriends.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvFriends.adapter == null) {
            binding.rvFriends.adapter = adapter
        }

        // Add item touch listener
        binding.rvFriends.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)
                val position = child?.let { rv.getChildAdapterPosition(it) }

                if (position != viewModel.currentSwipedPosition.get() && viewModel.currentSwipedPosition.get() != -1) {
                    // Close the previously opened item
                    viewModel.currentSwipedPosition.get()?.let { adapter.closePosition(it) }
                    viewModel.currentSwipedPosition.set(-1)
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }
        })
    }

    private fun showBlockDialog(friendId: Int, friendName: String) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_block_user)
        dialog.setCanceledOnTouchOutside(false)
        val window = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT,
        )

        dialog.show()
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btn_cancel)
        val btnBlock = dialog.findViewById<MaterialButton>(R.id.btn_logout)
        val txtBlockUserName = dialog.findViewById<TextView>(R.id.txt_title)

        txtBlockUserName.text = StringBuilder().append("Are you sure, want to block ").append(friendName).append("?")

        btnBlock.setOnClickListener {
            dialog.dismiss()

            blockUser(friendId)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun blockUser(friendId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val map = mutableMapOf<String, Any?>()
            map["block_id"] = friendId

            viewModel.blockUser(map).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(requireActivity())
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(getKoinContext(), "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(getKoinContext(), "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(requireActivity(), it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                val rootJson = it.data

                                if (rootJson.has("status") && rootJson.getAsJsonPrimitive("status").asBoolean) {
                                    viewModel.isDataLoaded.set(false)

                                    SocketManager.emit(SocketConstants.GET_FRIEND_LIST, getUserPrefs().userId)
                                } else {
                                    Toast.makeText(requireActivity(), "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun authOut() {
        LoadingDialog.show(requireActivity())

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(requireActivity())

                startActivity(Intent(requireActivity(), SignInActivity::class.java))
                requireActivity().finishAffinity()
                requireActivity().swipeLeft()
            },
        )
    }

    override fun onFriendClicked(friendId: Int, friendName: String, friendImage: String) {
        startActivity(Intent(requireActivity(), ChatActivity::class.java).apply {
            putExtra("userId", friendId)
        })
        requireActivity().swipeRight()
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.INIT_SUCCESS -> {
                SocketManager.emit(SocketConstants.GET_FRIEND_LIST, getUserPrefs().userId)
            }

            SocketConstants.GET_FRIEND_LIST_RESPONSE -> {
                if (value is ArrayList<*>) {
                    logger("--friend_list--", gsonString(value))

                    val list = value as ArrayList<FriendDetails>

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure parsing as UTC

                    list.sortByDescending { friendDetails ->
                        dateFormat.parse(friendDetails.lastMessageTime)?.time ?: 0L // Convert to milliseconds for sorting
                    }

                    viewModel.currentSwipedPosition.set(-1)
                    viewModel.isDataLoaded.set(true)
                    viewModel.friendList.set(list)
                    viewModel.friendList.get()?.size?.let { adapter.notifyItemRangeChanged(0, it) }
                    adapter.asyncListDiffer.submitList(viewModel.friendList.get())
                }
            }

            SocketConstants.TYPING_RESPONSE -> {
                val typingResponse = value as TypingResponseModel

                for (i in 0 until viewModel.friendList.get()!!.size) {
                    if (viewModel.friendList.get()!![i].friendData.id == typingResponse.userId || viewModel.friendList.get()!![i].userData.id == typingResponse.userId) {
                        if (viewModel.friendList.get()!![i].isTyping != typingResponse.isTyping) {
                            viewModel.friendList.set(viewModel.friendList.get()!!.apply {
                                set(i, viewModel.friendList.get()!![i].apply { isTyping = typingResponse.isTyping })
                            })
                            adapter.asyncListDiffer.submitList(viewModel.friendList.get())
                            adapter.notifyItemChanged(i)
                        }
                    }
                }
            }
        }
    }

    override fun onSwipeOpened(position: Int) {
        logger("--swipe--", "onSwipeOpened: $position")
        if ((viewModel.currentSwipedPosition.get() == -1) || (viewModel.currentSwipedPosition.get() != position)) {
            viewModel.currentSwipedPosition.get()?.let { adapter.closePosition(it) }
        }
        viewModel.currentSwipedPosition.set(position)
    }

    override fun onSwipeClosed(position: Int) {
        logger("--swipe--", "onSwipeOpened: $position")
    }

    override fun onBlockClicked(friendId: Int, fullName: String) {
        showBlockDialog(friendId, fullName)
    }
}
package com.belive.dating.activities.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.chat.ChatActivity
import com.belive.dating.activities.rejection.PhotosRejectionActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.activities.user_details.UserDetailsActivity
import com.belive.dating.api.user.models.notification.NotificationModel
import com.belive.dating.databinding.ActivityNotificationBinding
import com.belive.dating.di.notificationViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.calculateTimeDifference
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationActivity : NetworkReceiverActivity(), NotificationAdapter.NotificationClickListener {

    val binding by lazy {
        ActivityNotificationBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: NotificationViewModel

    val adapter by lazy {
        NotificationAdapter(this)
    }

    private val notificationCountReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if ((intent?.hasExtra("isNotificationCountChanged") == true) && (intent.getBooleanExtra("isNotificationCountChanged", true) == true)) {
                logger("--notification--", "notificationCountReceiver: ${getUserPrefs().unreadNotificationCount}")

                getNotifications(true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        viewModel = tryKoinViewModel(listOf(notificationViewModel))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                swipeLeft()
            }
        })
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        LocalBroadcastManager.getInstance(this).registerReceiver(notificationCountReceiver, IntentFilter("NOTIFICATION_COUNT"))
    }

    private fun setUpRecyclerView() {
        if (binding.rvNotifications.layoutManager == null) {
            binding.rvNotifications.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvNotifications.adapter == null) {
            binding.rvNotifications.adapter = adapter
        }

        adapter.asyncListDiffer.submitList(viewModel.notificationList.get())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // Handles back action
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getNotifications(isRefresh: Boolean = false) {
        if (viewModel.notificationList.get().isNullOrEmpty() || isRefresh) {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getNotificationHistory().collectLatest {
                    launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {
                                viewModel.isLoading.set(true)
                                binding.executePendingBindings()
                                LoadingDialog.show(this@NotificationActivity)
                            }

                            Status.SIGN_OUT -> {
                                Toast.makeText(this@NotificationActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                                authOut()
                            }

                            Status.ADMIN_BLOCKED -> {
                                Toast.makeText(this@NotificationActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                    .show()

                                authOut()
                            }

                            Status.ERROR -> {
                                LoadingDialog.hide()

                                Toast.makeText(this@NotificationActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
                            }

                            Status.SUCCESS -> {
                                LoadingDialog.hide()

                                if (it.data != null) {
                                    val list = it.data.data

                                    list.forEachIndexed { index, notificationData ->
                                        if (notificationData.createdAt != null) {
                                            notificationData.createdAt = calculateTimeDifference(notificationData.createdAt!!)
                                            list[index] = notificationData
                                        }
                                    }

                                    viewModel.notificationList.set(list)

                                    binding.layoutNoData.isVisible = list.isEmpty()

                                    setUpRecyclerView()
                                } else {
                                    Toast.makeText(this@NotificationActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
                                }
                                viewModel.isLoading.set(false)
                                binding.executePendingBindings()
                            }
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

                startActivity(Intent(this@NotificationActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        getNotifications()
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            getNotifications()
        }
    }

    override fun onNotificationClicked(notification: NotificationModel) {
        try {
            if (notification.type == "message") {
                startActivity(Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", notification.profileData?.id)
                    putExtra("notiId", notification.id)
                })
                swipeRight()
            } else if ((notification.type == "profile_view") || (notification.type == "like") || (notification.type == "super_like")) {
                startActivity(Intent(this@NotificationActivity, UserDetailsActivity::class.java).apply {
                    putExtra("userId", notification.profileData?.id)
                    putExtra("notiId", notification.id)
                })
                swipeRight()
            } else if (notification.type == "verify_profile") {
                startActivity(Intent(this@NotificationActivity, PhotosRejectionActivity::class.java).apply {
                    putExtra("notiId", notification.id)
                })
                swipeRight()
            } else {

            }
        } catch (e: Exception) {
            Toast.makeText(this@NotificationActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
        }
    }
}
package com.belive.dating.activities.settings.manage_notifications

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.BigNativeGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.ActivityManageNotificationsBinding
import com.belive.dating.di.profileViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageNotificationsActivity : NetworkReceiverActivity() {

    val binding by lazy {
        ActivityManageNotificationsBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: ManageNotificationsViewModel

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

        viewModel = tryKoinViewModel(listOf(profileViewModel))
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
                if (viewModel.notificationSettings.get() == null) {
                    finish()
                    swipeLeft()
                } else {
                    if ((viewModel.showNewMatchNotifications.get() != (viewModel.notificationSettings.get()?.newMatchAlert)) || (viewModel.showNewMessageNotifications.get() != (viewModel.notificationSettings.get()?.newMessageAlert)) || (viewModel.showNewLikeNotifications.get() != (viewModel.notificationSettings.get()?.newLikeAlert)) || (viewModel.showNewSuperLikeNotifications.get() != (viewModel.notificationSettings.get()?.newSuperLikeAlert))) {
                        updateNotificationSettings()
                    } else {
                        finish()
                        swipeLeft()
                    }
                }
            }
        })

        binding.switchNewMatches.setOnCheckedChangeListener { _, isChecked ->
            viewModel.showNewMatchNotifications.set(isChecked)
        }

        binding.switchNewMessage.setOnCheckedChangeListener { _, isChecked ->
            viewModel.showNewMessageNotifications.set(isChecked)
        }

        binding.switchNewLike.setOnCheckedChangeListener { _, isChecked ->
            viewModel.showNewLikeNotifications.set(isChecked)
        }

        binding.switchNewSuperLike.setOnCheckedChangeListener { _, isChecked ->
            viewModel.showNewSuperLikeNotifications.set(isChecked)
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun updateNotificationSettings() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.updateNotificationSettings().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@ManageNotificationsActivity)
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@ManageNotificationsActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@ManageNotificationsActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@ManageNotificationsActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                val json = it.data

                                if (json.has("data") && json.getAsJsonPrimitive("data").asBoolean) {
                                    Toast.makeText(this@ManageNotificationsActivity, "Notification updates saved", Toast.LENGTH_SHORT).show()

                                    finish()
                                    swipeLeft()
                                } else {
                                    finish()
                                    swipeLeft()

                                    Toast.makeText(
                                        this@ManageNotificationsActivity, it.message ?: "Something went wrong, try again...!", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                finish()
                                swipeLeft()

                                Toast.makeText(
                                    this@ManageNotificationsActivity, it.message ?: "Something went wrong, try again...!", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getNotificationSettings() {
        ManageAds.showNativeSquareAd(BigNativeGroup.Main, binding.adNative)

        if (viewModel.notificationSettings.get() == null) {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getAlertNotificationSettings().collectLatest {
                    launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {
                                LoadingDialog.show(this@ManageNotificationsActivity)
                            }

                            Status.SIGN_OUT -> {
                                Toast.makeText(this@ManageNotificationsActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                    .show()

                                authOut()
                            }

                            Status.ADMIN_BLOCKED -> {
                                Toast.makeText(
                                    this@ManageNotificationsActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT
                                ).show()

                                authOut()
                            }

                            Status.ERROR -> {
                                LoadingDialog.hide()

                                Toast.makeText(this@ManageNotificationsActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
                            }

                            Status.SUCCESS -> {
                                LoadingDialog.hide()

                                if (it.data?.notificationSettings != null) {
                                    viewModel.notificationSettings.set(it.data.notificationSettings)

                                    viewModel.showNewMatchNotifications.set(it.data.notificationSettings.newMatchAlert)
                                    viewModel.showNewMessageNotifications.set(it.data.notificationSettings.newMessageAlert)
                                    viewModel.showNewLikeNotifications.set(it.data.notificationSettings.newLikeAlert)
                                    viewModel.showNewSuperLikeNotifications.set(it.data.notificationSettings.newSuperLikeAlert)
                                } else {
                                    Toast.makeText(this@ManageNotificationsActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    }
                }
            }
        }
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

    private fun authOut() {
        LoadingDialog.show(this)

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@ManageNotificationsActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        getNotificationSettings()
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            getNotificationSettings()
        }
    }
}
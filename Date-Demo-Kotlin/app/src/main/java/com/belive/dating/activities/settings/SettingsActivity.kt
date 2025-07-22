package com.belive.dating.activities.settings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.ActionBar
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.paywalls.subscriptions.subscription.SubscriptionActivity
import com.belive.dating.activities.settings.content_visibility.ContentVisibilityActivity
import com.belive.dating.activities.settings.manage_notifications.ManageNotificationsActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.constants.EventConstants
import com.belive.dating.constants.PROFILE_VISIBILITY
import com.belive.dating.databinding.ActivitySettingsBinding
import com.belive.dating.databinding.DialogDeleteAccountBinding
import com.belive.dating.databinding.DialogLogoutBinding
import com.belive.dating.di.settingsViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.openBrowser
import com.belive.dating.extensions.setBackgroundAnimation
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.swipeUp
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : NetworkReceiverActivity() {

    val binding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: SettingsViewModel

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(SettingsActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(SettingsActivity::class.java.simpleName)
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

        viewModel = tryKoinViewModel(listOf(settingsViewModel))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            listenEvents()
            observeNetwork()
        }

        initViews()

        clickListeners()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        viewModel.isPremiumAvailable.set(!getUserPrefs().activePackage.isNullOrEmpty())

        premiumBannerUI()
    }

    var currentIndex = 0

    val sentences = listOf(
        "Unlock Messaging",
        "Get Unlimited Likes",
        "Enjoy Unlimited Rewinds",
        "Receive More Gems Every Month",
        "Get More Super Likes Per Week",
        "Enjoy More Boost Per Month",
        "Earn an Exclusive Gold Badge",
        "Browse Without Ads",
        "See Who Likes You",
        "Use Location Filters",
        "Access the AI Matchmaker",
    )

    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {

            binding.txtFeature.post {

                val slideOut = ObjectAnimator.ofFloat(binding.txtFeature, "translationX", 0f, -binding.txtFeature.width.toFloat()).apply {
                    duration = 200
                }
                val slideIn = ObjectAnimator.ofFloat(binding.txtFeature, "translationX", binding.txtFeature.width.toFloat(), 0f).apply {
                    duration = 200
                }

                slideOut.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentIndex = (currentIndex + 1) % sentences.size
                        binding.feature = sentences[currentIndex] // Update text
                        binding.txtFeature.translationX = binding.txtFeature.width.toFloat() // Reset position
                        slideIn.start()
                    }
                })

                slideOut.start()
            }

            handler.postDelayed(this, 2000) // Next text update after 2s
        }
    }

    private fun premiumBannerUI() {
        viewModel.isPremiumAvailable.set(!getUserPrefs().activePackage.isNullOrEmpty())

        binding.layoutPremium.clearAnimation()
        binding.txtFeature.clearAnimation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler.hasCallbacks(runnable)) {
                handler.removeCallbacks(runnable)
            }
        } else {
            handler.removeCallbacksAndMessages(null)
        }

        if (viewModel.isPremiumAvailable.get() == false) {
            val premiumTitle = "Level Up Your Dating Life!"
            val spannablePremiumTitle = SpannableString(premiumTitle)

            binding.premiumTitle = spannablePremiumTitle

            binding.feature = sentences[currentIndex]

            handler.postDelayed(runnable, 2000)

            binding.layoutPremium.setBackgroundAnimation(
                intArrayOf(
                    "#303F9F".toColorInt(),
                    "#1976D2".toColorInt(),
                    "#512DA8".toColorInt(),
                    "#C2185B".toColorInt(),
                    "#D32F2F".toColorInt(),
                )
            )
        }
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                swipeLeft()
            }
        })

        binding.layoutPremium.setOnClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
            swipeUp()
        }

        binding.layoutManageNotification.setOnClickListener {
            startActivity(Intent(this, ManageNotificationsActivity::class.java))
            swipeRight()
        }

        binding.switchRemoveAds.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.switchRemoveAds.postDelayed({
                    binding.switchRemoveAds.isChecked = false

                    startActivity(Intent(this, SubscriptionActivity::class.java))
                    swipeUp()
                }, 200)
            }
        }

        binding.layoutSlandered.setOnClickListener {
            viewModel.profileVisibilityType.set(PROFILE_VISIBILITY.SLANDERED.profileVisibilityIndex)
        }

        binding.layoutIncognito.setOnClickListener {
            viewModel.profileVisibilityType.set(PROFILE_VISIBILITY.INCOGNITO.profileVisibilityIndex)
        }

        binding.layoutHidden.setOnClickListener {
            viewModel.profileVisibilityType.set(PROFILE_VISIBILITY.HIDDEN.profileVisibilityIndex)
        }

        binding.layoutSharedContent.setOnClickListener {
            startActivity(Intent(this, ContentVisibilityActivity::class.java))
            swipeRight()
        }

        binding.layHelpCenter.setOnClickListener {
            openBrowser(resources.getString(R.string.helpUrl))
        }

        binding.layPrivacyPolicy.setOnClickListener {
            openBrowser(getGistPrefs().privacyPolicy)
        }

        binding.layTerms.setOnClickListener {
            openBrowser(getGistPrefs().termCondition)
        }

        binding.layFeedback.setOnClickListener {
            openBrowser(resources.getString(R.string.feedbackUrl))
        }

        binding.layRateUs.setOnClickListener {
            showRateDialog()
        }

        binding.layLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.layDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showLogoutDialog() {
        val bindingLogout = DialogLogoutBinding.inflate(layoutInflater)

        val logoutDialog = Dialog(this)
        logoutDialog.setCancelable(true)
        logoutDialog.setCanceledOnTouchOutside(true)
        logoutDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        logoutDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        logoutDialog.window?.setDimAmount(0.75f)
        logoutDialog.setContentView(bindingLogout.main)
        logoutDialog.show()

        bindingLogout.btnCancel.setOnClickListener {
            logoutDialog.dismiss()
        }
        bindingLogout.btnLogout.setOnClickListener {
            logOut()

            logoutDialog.dismiss()
        }
    }

    private fun showRateDialog() {
        val rateDialog = Dialog(this)
        rateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        rateDialog.setCancelable(true)
        rateDialog.setCanceledOnTouchOutside(true)
        rateDialog.setContentView(R.layout.dialog_rate_app)
        val window = rateDialog.window
        window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setDimAmount(0.75f)
        rateDialog.show()

        val btnDoItLater = rateDialog.findViewById<Button>(R.id.btn_do_it_later)
        val btnRateApp = rateDialog.findViewById<Button>(R.id.btn_rate_app)

        btnDoItLater.setOnClickListener {
            rateDialog.dismiss()
        }
        btnRateApp.setOnClickListener {
            rateDialog.dismiss()

            try {
                val uri = ("market://details?id=$packageName").toUri()
                val goMarket = Intent(Intent.ACTION_VIEW, uri)
                startActivity(goMarket)
            } catch (e: ActivityNotFoundException) {
                openBrowser("https://play.google.com/store/apps/details?id=$packageName")
            }
        }
    }

    private fun showDeleteAccountDialog() {
        val bindingDeleteAccount = DialogDeleteAccountBinding.inflate(layoutInflater)

        val deleteAccountDialog = Dialog(this)
        deleteAccountDialog.setCancelable(true)
        deleteAccountDialog.setCanceledOnTouchOutside(true)
        deleteAccountDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        deleteAccountDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        deleteAccountDialog.window?.setDimAmount(0.75f)
        deleteAccountDialog.setContentView(bindingDeleteAccount.root)
        deleteAccountDialog.show()

        bindingDeleteAccount.btnCancel.setOnClickListener {
            deleteAccountDialog.dismiss()
        }
        bindingDeleteAccount.btnLogout.setOnClickListener {
            deleteAccount()

            deleteAccountDialog.dismiss()
        }
    }

    private fun logOut() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.logout().collect {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@SettingsActivity)
                        }

                        Status.SIGN_OUT -> {
                            authOut()
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@SettingsActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@SettingsActivity, "Something went wrong....", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            val rootJson = it.data?.asJsonObject
                            if ((rootJson != null) && rootJson.has("data") && rootJson.getAsJsonPrimitive("data").asBoolean) {
                                authOut()
                            } else {
                                LoadingDialog.hide()

                                Toast.makeText(this@SettingsActivity, "Something went wrong....", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun deleteAccount() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteUser().collect {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@SettingsActivity)
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@SettingsActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@SettingsActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@SettingsActivity, "Something went wrong....", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            val rootJson = it.data?.asJsonObject

                            if (rootJson != null && rootJson.has("data") && !rootJson.getAsJsonArray("data").isEmpty) {
                                authOut()
                            } else {
                                LoadingDialog.hide()

                                Toast.makeText(this@SettingsActivity, "Something went wrong....", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun authOut() {
        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@SettingsActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
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

    override fun onInternetAvailableForFirstTime() {

    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {

    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.UPDATE_PURCHASE -> {
                viewModel.isPremiumAvailable.set(!getUserPrefs().activePackage.isNullOrEmpty())
            }
        }
    }
}
package com.belive.dating.activities.dashboard.main

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import com.belive.dating.BuildConfig
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.filter.FiltersActivity
import com.belive.dating.activities.introduction.birthdate.BirthdateActivity
import com.belive.dating.activities.introduction.choose_interest.ChooseInterestActivity
import com.belive.dating.activities.introduction.gender.GenderActivity
import com.belive.dating.activities.introduction.name.NameActivity
import com.belive.dating.activities.introduction.opposite_gender.OppositeGenderActivity
import com.belive.dating.activities.introduction.relationship_goal.RelationshipGoalActivity
import com.belive.dating.activities.introduction.sexual_orientation.SexualOrientationActivity
import com.belive.dating.activities.introduction.upload_photo.UploadPhotoActivity
import com.belive.dating.activities.notification.NotificationActivity
import com.belive.dating.activities.permission.PermissionsManagerActivity
import com.belive.dating.activities.rejection.PhotosRejectionActivity
import com.belive.dating.activities.search_user.SearchUserActivity
import com.belive.dating.activities.settings.SettingsActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.activities.user_details.UserDetailsActivity
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.ads.ManageAds
import com.belive.dating.api.ads_settings.AdsSettingsRepository
import com.belive.dating.api.gist.GISTRepository
import com.belive.dating.api.user.models.payment.PaymentResponse
import com.belive.dating.api.user.models.user.Purchase
import com.belive.dating.constants.EventConstants
import com.belive.dating.constants.PrefConst
import com.belive.dating.constants.SocketConstants
import com.belive.dating.databinding.ActivityMainBinding
import com.belive.dating.di.adsSettingsModule
import com.belive.dating.di.filtersViewModel
import com.belive.dating.di.gistModule
import com.belive.dating.di.introductionModule
import com.belive.dating.di.introductionViewModels
import com.belive.dating.di.mainViewModel
import com.belive.dating.di.notificationViewModel
import com.belive.dating.di.searchUserViewModel
import com.belive.dating.di.settingsViewModel
import com.belive.dating.di.signInViewModel
import com.belive.dating.di.splashDataModule
import com.belive.dating.dialogs.AppDialog
import com.belive.dating.dialogs.EnableGPSDialog
import com.belive.dating.dialogs.InitFailedDialog
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.checkPermissions
import com.belive.dating.extensions.convertLongToStringDate
import com.belive.dating.extensions.fadeIn
import com.belive.dating.extensions.getAdsPrefs
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getDeviceID
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getIntroductionPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.isAppUpdateRequired
import com.belive.dating.extensions.isDateInPast
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.reOpenApp
import com.belive.dating.extensions.safeApiCallResponse
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.setupWithNavController
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.swipeUp
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.app_update.AppUpdateHelper
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.authentication.isUserAvailable
import com.belive.dating.helpers.helper_functions.current_location.CurrentLocation
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import com.belive.dating.helpers.helper_functions.socket.SocketManager
import com.belive.dating.helpers.helper_functions.splash_data.SplashData
import com.belive.dating.onesignal.ManualOneSignal
import com.belive.dating.payment.PaymentUtils
import com.belive.dating.payment.ProductType
import com.belive.dating.payment.activePlan
import com.belive.dating.preferences.pref_helpers.UserPrefs
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

class MainActivity : NetworkReceiverActivity() {

    private val TAG = "--main--"

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var savedInstanceState: Bundle? = null
    private var navController: NavController? = null

    lateinit var viewModel: MainViewModel

    private val appUpdateResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
        // handle callback
        logger("--install--", "resultCode: ${result.resultCode}")

        when (result.resultCode) {
            RESULT_OK -> {
                // Update completed
                logger("--install--", "Update completed successfully.")
            }

            RESULT_CANCELED -> {
                // Update cancelled by the user
                logger("--install--", "Update cancelled by user.")
            }

            else -> {
                // Update failed or other cases
                logger("--install--", "Update failed with resultCode: ${result.resultCode}")
            }
        }
    }

    private val enableGPSLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                // GPS Enabled
                currentLocation.startLocationUpdates()
            }

            RESULT_CANCELED -> {
                // GPS Not Enabled

                viewModel.isGPSEnabled.set(false)

                EnableGPSDialog.showEnableGPSDialog(onTurnOnLocation = {

                }, onError = {
                    initialFlowFailed()
                })
            }
        }
    }

    private val currentLocation by lazy {
        CurrentLocation(this@MainActivity, object : () -> Unit {
            override fun invoke() {
                updateLocation()
            }
        })
    }

    private val notificationCountReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if ((intent?.hasExtra("isNotificationCountChanged") == true) && (intent.getBooleanExtra("isNotificationCountChanged", true) == true)) {
                logger("--notification--", "notificationCountReceiver: ${getUserPrefs().unreadNotificationCount}")

                invalidateOptionsMenu()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if ((getUserPrefs().userToken != null) && checkPermissions()) {
            mixPanel?.timeEvent(MainActivity::class.java.simpleName)
        }
    }

    override fun onPause() {
        super.onPause()

        if ((getUserPrefs().userToken != null) && checkPermissions()) {
            mixPanel?.track(MainActivity::class.java.simpleName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
        this.savedInstanceState = savedInstanceState

        if ((savedInstanceState != null) || (intent.hasExtra("display_splash") && !intent.getBooleanExtra("display_splash", false))) {
            setTheme(R.style.AppTheme)

            super.onCreate(savedInstanceState)

            viewModel = tryKoinViewModel(listOf(mainViewModel))

            if (getUserPrefs().userToken.isNullOrEmpty() || !isUserAvailable()) {
                startIntroductionFlow()
            } else if (!checkPermissions()) {
                startActivity(Intent(this, PermissionsManagerActivity::class.java).apply {
                    putExtra("load_gist", true)
                })
                finish()
                fadeIn()
            } else {
                setContentView(binding.root)

                ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                    insets
                }

                binding.root.post {
                    setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

                    setSupportActionBar(binding.toolbar)
                    setUpNavigationView()

                    listenEvents()

                    observeNetwork()
                }
            }
        } else {
            super.onCreate(null)

            viewModel = tryKoinViewModel(listOf(mainViewModel))

            displaySplash()
        }

        enableEdgeToEdge()
    }

    private fun setUpNavigationView() {
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationCountReceiver, IntentFilter("NOTIFICATION_COUNT"))

        val navGraphIds = listOf(R.navigation.nav_home, R.navigation.nav_ai, R.navigation.nav_ls, R.navigation.nav_message, R.navigation.nav_profile)

        navController = binding.bottomNavView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            onItemClicked = {
                when (it) {
                    R.id.navigation_home -> {
                        viewModel.currentFragment.set("home")
                        invalidateOptionsMenu()
                    }

                    R.id.navigation_ai -> {
                        viewModel.currentFragment.set("ai")
                        invalidateOptionsMenu()
                    }

                    R.id.navigation_ls -> {
                        viewModel.currentFragment.set("ls")
                        invalidateOptionsMenu()
                    }

                    R.id.navigation_message -> {
                        viewModel.currentFragment.set("message")
                        invalidateOptionsMenu()
                    }

                    R.id.navigation_profile -> {
                        viewModel.currentFragment.set("profile")
                        invalidateOptionsMenu()
                    }
                }
            },
            onBackPressedDispatcher = onBackPressedDispatcher,
        )

        viewModel.currentFragment.set("home")
        invalidateOptionsMenu()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() ?: super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option_main, menu)
        return true
    }

    var badgeDrawable: BadgeDrawable? = null

    @ExperimentalBadgeUtils
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)

        try {
            BadgeUtils.detachBadgeDrawable(badgeDrawable, binding.toolbar, R.id.notification)

            // Initialize the badge drawable
            badgeDrawable = BadgeDrawable.create(this)
            badgeDrawable?.number = UserPrefs.unreadNotificationCount
            badgeDrawable?.isVisible = UserPrefs.unreadNotificationCount > 0
            badgeDrawable?.backgroundColor = ContextCompat.getColor(this, R.color.primary_color)

            // Attach the badge to the menu item
            menu?.findItem(R.id.notification)?.let {
                badgeDrawable?.let { it1 -> BadgeUtils.attachBadgeDrawable(it1, binding.toolbar, R.id.notification) }
            }

            logger("--notification--", "onPrepareOptionsMenu: ${getUserPrefs().unreadNotificationCount}")
        } catch (e: Exception) {
            catchLog("onPrepareOptionsMenu: ${gsonString(e)}")
        }

        // Control menu item visibility based on the fragment
        if (viewModel.currentFragment.get() == "home") {
            menu?.findItem(R.id.search)?.isVisible = BuildConfig.DEBUG
            menu?.findItem(R.id.notification)?.isVisible = true
            menu?.findItem(R.id.filter)?.isVisible = true
            menu?.findItem(R.id.settings)?.isVisible = false
        } else if (viewModel.currentFragment.get() == "ai") {
            menu?.findItem(R.id.search)?.isVisible = false
            menu?.findItem(R.id.notification)?.isVisible = false
            menu?.findItem(R.id.filter)?.isVisible = false
            menu?.findItem(R.id.settings)?.isVisible = false
        } else if (viewModel.currentFragment.get() == "profile") {
            menu?.findItem(R.id.search)?.isVisible = false
            menu?.findItem(R.id.notification)?.isVisible = false
            menu?.findItem(R.id.filter)?.isVisible = false
            menu?.findItem(R.id.settings)?.isVisible = true
        } else {
            menu?.findItem(R.id.search)?.isVisible = false
            menu?.findItem(R.id.notification)?.isVisible = false
            menu?.findItem(R.id.filter)?.isVisible = false
            menu?.findItem(R.id.settings)?.isVisible = false
        }

        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.search -> {
                // Handle search click
                loadKoinModules(searchUserViewModel)

                startActivity(Intent(this@MainActivity, SearchUserActivity::class.java))
                swipeRight()
                true
            }

            R.id.notification -> {
                // Handle notification click
                loadKoinModules(notificationViewModel)

                startActivity(Intent(this, NotificationActivity::class.java))
                swipeRight()
                true
            }

            R.id.filter -> {
                // Handle filter click
                openFilters()
                true
            }

            R.id.settings -> {
                // Handle settings click
                loadKoinModules(settingsViewModel)

                startActivity(Intent(this, SettingsActivity::class.java))
                swipeRight()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun askForAppReview() {
        if (viewModel.isAppReviewed.get() == false) {
            val manager = ReviewManagerFactory.create(this@MainActivity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        // We got the ReviewInfo object
                        val reviewInfo = task.result
                        val flow = manager.launchReviewFlow(this@MainActivity, reviewInfo)
                        flow.addOnCompleteListener { _ ->
                            // The flow has finished. The API does not indicate whether the user
                            // reviewed or not, or even whether the review dialog was shown. Thus, no
                            // matter the result, we continue our app flow.

                            viewModel.isAppReviewed.set(true)
                        }
                    }
                } else {
                    // There was some problem, log or handle the error code.
                    logger("--error--", task.exception)

                    viewModel.isAppReviewed.set(true)
                }
            }
        }
    }

    fun openFilters() {
        loadKoinModules(filtersViewModel)

        filterActivityLauncher.launch(Intent(this, FiltersActivity::class.java))
        swipeRight()
    }

    private val filterActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if ((result.data?.hasExtra("is_filter") == true) && (result.data?.getBooleanExtra("is_filter", false) == true)) {
                EventManager.postEvent(Event(EventConstants.FILTERS_UPDATED, null))
            }
        }
    }

    private fun displaySplash() {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.view.animate().alpha(0f).setDuration(500).start()

            if (getUserPrefs().userToken.isNullOrEmpty() || !isUserAvailable()) {
                startIntroductionFlow()
            } else if (!checkPermissions()) {
                startActivity(Intent(this, PermissionsManagerActivity::class.java).apply {
                    putExtra("load_gist", true)
                })
                finish()
                fadeIn()
            } else {
                setContentView(binding.root)

                ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                    insets
                }

                binding.root.post {
                    setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

                    setSupportActionBar(binding.toolbar)
                    setUpNavigationView()

                    listenEvents()

                    observeNetwork()

                    LoadingDialog.show(this)
                }
            }
        }
        splashScreen.setKeepOnScreenCondition(condition = SplashScreen.KeepOnScreenCondition {
            return@KeepOnScreenCondition false
        })
    }

    private fun getGistData() {
        val isGistAvailable = try {
            getKoinObject().get<GISTRepository>()
            true
        } catch (e: Exception) {
            false
        }
        if (isGistAvailable) {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getRaw().collectLatest {
                    launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {

                            }

                            Status.SIGN_OUT -> {

                            }

                            Status.ADMIN_BLOCKED -> {

                            }

                            Status.ERROR -> {
                                initialFlowFailed()
                            }

                            Status.SUCCESS -> {
                                // gist success

                                val responseJson = it.data
                                if (responseJson?.has("success") == true) {
                                    unloadKoinModules(gistModule)

                                    loadAds()
                                } else {
                                    initialFlowFailed()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            loadAds()
        }
    }

    private fun loadAds() {
        val isAdsModuleAvailable = try {
            getKoinObject().get<AdsSettingsRepository>()
            true
        } catch (e: Exception) {
            false
        }
        if (isAdsModuleAvailable) {
            lifecycleScope.launch(Dispatchers.IO) {
                val adsSettingsRepository = getKoinObject().get<AdsSettingsRepository>()

                val adsResponse = safeApiCallResponse {
                    adsSettingsRepository.getAdsSettings()
                }
                val adsErrorBody = adsResponse.errorBody()?.string()

                logger("--ads_settings--", "request.url: ${adsResponse.raw().request.url}")
                logger("--ads_settings--", "request.body: ${gsonString(adsResponse.raw().request.body)}")
                logger("--ads_settings--", "code: ${adsResponse.code()}")
                logger("--ads_settings--", "isSuccessful: ${adsResponse.isSuccessful}")
                logger("--ads_settings--", "errorBody: $adsErrorBody")
                logger("--ads_settings--", "body: ${gsonString(adsResponse.body())}")

                if (adsResponse.isSuccessful) {
                    if (adsResponse.body() != null) {
                        getAdsPrefs().setAdsSettings(adsResponse.body()!!)

                        unloadKoinModules(adsSettingsModule)
                    }
                }

                ManageAds.startLoadingAds = true

                ManageAds.loadAds()

                launch(Dispatchers.Main) {
                    if ((intent.hasExtra("display_splash") && !intent.getBooleanExtra("display_splash", false))) {
                        LoadingDialog.hide()

                        afterGist()
                    } else {
                        AdmobAds.showAppOpenAdAfterSplash {
                            LoadingDialog.hide()

                            afterGist()
                        }
                    }
                }
            }
        } else {
            ManageAds.startLoadingAds = true

            ManageAds.loadAds()

            lifecycleScope.launch(Dispatchers.Main) {
                if ((intent.hasExtra("display_splash") && !intent.getBooleanExtra("display_splash", false))) {
                    LoadingDialog.hide()

                    afterGist()
                } else {
                    AdmobAds.showAppOpenAdAfterSplash {
                        LoadingDialog.hide()

                        afterGist()
                    }
                }
            }
        }
    }

    private fun afterGist() {
        when (getUserPrefs().activePackage) {
            "Gold" -> {
                viewModel.activePackage.set(ProductType.GOLD)
            }

            "Platinum" -> {
                viewModel.activePackage.set(ProductType.PLATINUM)
            }

            "Lifetime" -> {
                viewModel.activePackage.set(ProductType.LIFETIME)
            }

            else -> {
                viewModel.activePackage.set(null)
            }
        }

        if (getGistPrefs().appRedirectOtherAppStatus) {
            AppDialog.showAppRedirectDialog(
                context = this@MainActivity,
                onManage = {
                    logger(TAG, "redirectPath: ${getGistPrefs().appNewPackageName}")

                    try {
                        val marketUri = getGistPrefs().appNewPackageName.toUri()
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (ignored: Exception) {
                        Toast.makeText(this@MainActivity, "Something want wrong", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        } else if (isAppUpdateRequired()) {
            val isFlexible = getGistPrefs().appUpdateAppDialogStatus

            AppDialog.showAppUpdateDialog(
                context = this@MainActivity,
                isFlexible = isFlexible,
                onClose = {
                    // continue with app flow
                    if (getUserPrefs().userToken.isNullOrEmpty() || !isUserAvailable()) {
                        startIntroductionFlow()
                    } else if (!checkPermissions()) {
                        startActivity(Intent(this@MainActivity, PermissionsManagerActivity::class.java))
                        finish()
                    } else {
                        loadUser()
                    }
                },
                onManage = {
                    val packageName = getGistPrefs().appUpdatePackageName
                    logger(TAG, "packageName: $packageName")

                    try {
                        val marketUri = "https://play.google.com/store/apps/details?id=${packageName}".toUri()
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (ignored: Exception) {
                        Toast.makeText(this@MainActivity, "Something want wrong", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        } else {
            // continue with app flow
            if (getUserPrefs().userToken.isNullOrEmpty() || !isUserAvailable()) {
                startIntroductionFlow()
            } else if (!checkPermissions()) {
                startActivity(Intent(this@MainActivity, PermissionsManagerActivity::class.java))
                finish()
            } else {
                loadUser()
            }
        }
    }

    private fun startIntroductionFlow() {
        loadKoinModules(listOf(introductionModule, signInViewModel, introductionViewModels))

        val signInIntent = Intent(this@MainActivity, SignInActivity::class.java)
        val nameIntent = Intent(this@MainActivity, NameActivity::class.java)
        val birthdateIntent = Intent(this@MainActivity, BirthdateActivity::class.java)
        val genderIntent = Intent(this@MainActivity, GenderActivity::class.java)
        val oppositeGenderIntent = Intent(this@MainActivity, OppositeGenderActivity::class.java)
        val sexualityIntent = Intent(this@MainActivity, SexualOrientationActivity::class.java)
        val relationshipIntent = Intent(this@MainActivity, RelationshipGoalActivity::class.java)
        val interestsIntent = Intent(this@MainActivity, ChooseInterestActivity::class.java)
        val photosIntent = Intent(this@MainActivity, UploadPhotoActivity::class.java)

        val photoList = if (getIntroductionPrefs().photoList.isNullOrEmpty()) {
            null
        } else {
            val list = ArrayList(getIntroductionPrefs().photoList!!)
            list.removeAll { !File(it).exists() }
            list
        }

        getIntroductionPrefs().photoList = photoList

        val selfie = if (getIntroductionPrefs().selfie.isNullOrEmpty()) {
            null
        } else {
            if (!File(getIntroductionPrefs().selfie!!).exists()) {
                null
            } else {
                getIntroductionPrefs().selfie
            }
        }

        getIntroductionPrefs().selfie = selfie

        if (getIntroductionPrefs().email.isNullOrEmpty()) {
            startActivity(signInIntent.apply {
                putExtra("load_gist", true)
            })
        } else if (getIntroductionPrefs().name.isNullOrEmpty()) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent.apply {
                        putExtra("load_gist", true)
                    },
                )
            )
        } else if (getIntroductionPrefs().gender == 0) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent,
                    genderIntent.apply {
                        putExtra("load_gist", true)
                    },
                )
            )
        } else if (getIntroductionPrefs().birthDate.isNullOrEmpty()) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent, genderIntent,
                    birthdateIntent.apply {
                        putExtra("load_gist", true)
                    },
                )
            )
        } else if (getIntroductionPrefs().oppositeGender == 0) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent, genderIntent, birthdateIntent,
                    oppositeGenderIntent.apply {
                        putExtra("load_gist", true)
                    },
                )
            )
        } else if (getIntroductionPrefs().sexualOrientation == -1) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent, genderIntent, birthdateIntent, oppositeGenderIntent,
                    sexualityIntent.apply {
                        putExtra("load_gist", true)
                    },
                )
            )
        } else if (getIntroductionPrefs().relationshipGoal == -1) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent, genderIntent, birthdateIntent, oppositeGenderIntent, sexualityIntent,
                    relationshipIntent.apply {
                        putExtra("load_gist", true)
                    },
                )
            )
        } else if (getIntroductionPrefs().interestList.isNullOrEmpty()) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent, genderIntent, birthdateIntent, oppositeGenderIntent, sexualityIntent, relationshipIntent,
                    interestsIntent.apply {
                        putExtra("load_gist", true)
                    },
                )
            )
        } else if (getIntroductionPrefs().photoList.isNullOrEmpty()) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent,
                    genderIntent,
                    birthdateIntent,
                    oppositeGenderIntent,
                    sexualityIntent,
                    relationshipIntent,
                    interestsIntent,
                    photosIntent.apply {
                        putExtra("load_gist", true)
                        putExtra("isPhotosAvailable", false)
                    },
                )
            )
        } else if (getIntroductionPrefs().selfie.isNullOrEmpty()) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent,
                    genderIntent,
                    birthdateIntent,
                    oppositeGenderIntent,
                    sexualityIntent,
                    relationshipIntent,
                    interestsIntent,
                    photosIntent.apply {
                        putExtra("load_gist", true)
                        putExtra("isPhotosAvailable", true)
                        putExtra("isSelfieAvailable", false)
                    },
                )
            )
        } else if (!getIntroductionPrefs().selfie.isNullOrEmpty()) {
            startActivities(
                arrayOf(
                    signInIntent.apply {
                        putExtra("signInAutomatically", false)
                    },
                    nameIntent,
                    genderIntent,
                    birthdateIntent,
                    oppositeGenderIntent,
                    sexualityIntent,
                    relationshipIntent,
                    interestsIntent,
                    photosIntent.apply {
                        putExtra("load_gist", true)
                        putExtra("isPhotosAvailable", true)
                        putExtra("isSelfieAvailable", true)
                    },
                )
            )
        }
        finish()
        fadeIn()
    }

    private fun loadUser() {
        AppUpdateHelper(appUpdateResultLauncher).checkInAppUpdate()

        currentLocation.init(enableGPSLauncher)

        uploadSplashData()
    }

    private fun uploadSplashData() {
        val isSplashDataAvailable = try {
            getKoinObject().get<SplashData>()
            true
        } catch (e: Exception) {
            false
        }
        if (isSplashDataAvailable) {
            unloadKoinModules(splashDataModule)
        }
    }

    private fun initialFlowFailed() {
        viewModel.isInitFailed.set(true)

        InitFailedDialog.showAppUpdateDialog(onTryAgain = {
            viewModel.isInitFailed.set(false)
            getGistData()
        }, onReOpenApp = {
            viewModel.isInitFailed.set(false)
            reOpenApp()
        })
    }

    private fun updateLocation() {
        currentLocation.getCurrentLocationData { locationMap ->
            if (locationMap == null) {
                viewModel.isGPSEnabled.set(false)

                InitFailedDialog.showAppUpdateDialog(onTryAgain = {
                    viewModel.isInitFailed.set(false)
                    getGistData()
                }, onReOpenApp = {
                    viewModel.isInitFailed.set(false)
                    reOpenApp()
                })
            } else {
                viewModel.isGPSEnabled.set(true)

                if (getUserPrefs().activePackage == "Lifetime") {
                    locationMap[PrefConst.CUSTOM_LATITUDE] = getUserPrefs().customLatitude ?: locationMap[PrefConst.CURRENT_LATITUDE]!!
                    locationMap[PrefConst.CUSTOM_LONGITUDE] = getUserPrefs().customLongitude ?: locationMap[PrefConst.CURRENT_LONGITUDE]!!
                    locationMap[PrefConst.CUSTOM_COUNTRY] = getUserPrefs().customCountry ?: locationMap[PrefConst.CURRENT_COUNTRY]!!
                } else {
                    if (getUserPrefs().purchaseEndDate.isNullOrEmpty()) {
                        getUserPrefs().customLocationName = null

                        locationMap[PrefConst.CUSTOM_LATITUDE] = locationMap[PrefConst.CURRENT_LATITUDE]!!
                        locationMap[PrefConst.CUSTOM_LONGITUDE] = locationMap[PrefConst.CURRENT_LONGITUDE]!!
                        locationMap[PrefConst.CUSTOM_COUNTRY] = locationMap[PrefConst.CURRENT_COUNTRY]!!
                    } else {
                        if (isDateInPast(getUserPrefs().purchaseEndDate!!)) {
                            getUserPrefs().customLocationName = null

                            locationMap[PrefConst.CUSTOM_LATITUDE] = locationMap[PrefConst.CURRENT_LATITUDE]!!
                            locationMap[PrefConst.CUSTOM_LONGITUDE] = locationMap[PrefConst.CURRENT_LONGITUDE]!!
                            locationMap[PrefConst.CUSTOM_COUNTRY] = locationMap[PrefConst.CURRENT_COUNTRY]!!
                        } else {
                            locationMap[PrefConst.CUSTOM_LATITUDE] = getUserPrefs().customLatitude ?: locationMap[PrefConst.CURRENT_LATITUDE]!!
                            locationMap[PrefConst.CUSTOM_LONGITUDE] = getUserPrefs().customLongitude ?: locationMap[PrefConst.CURRENT_LONGITUDE]!!
                            locationMap[PrefConst.CUSTOM_COUNTRY] = getUserPrefs().customCountry ?: locationMap[PrefConst.CURRENT_COUNTRY]!!
                        }
                    }
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.updateUserLocation(locationMap).collectLatest {
                        launch(Dispatchers.Main) {
                            when (it.status) {
                                Status.LOADING -> {

                                }

                                Status.SIGN_OUT -> {
                                    Toast.makeText(this@MainActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                                    authOut()
                                }

                                Status.ADMIN_BLOCKED -> {
                                    Toast.makeText(this@MainActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                        .show()

                                    authOut()
                                }

                                Status.ERROR -> {
                                    Toast.makeText(this@MainActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()

                                    initialFlowFailed()
                                }

                                Status.SUCCESS -> {
                                    if (it.data != null) {
                                        ManualOneSignal.login(this@MainActivity, "${it.data.user.id}")
                                        ManualOneSignal.setEmail(this@MainActivity, it.data.user.email)
                                        ManualOneSignal.setLanguage(this@MainActivity)
                                        ManualOneSignal.shareLocation(this@MainActivity)

                                        getUserPrefs().updateUserPrefs(it.data.user)

                                        if (getUserPrefs().userImages.isNullOrEmpty() || (getUserPrefs().userImages!!.size < 2) || getUserPrefs().userImages?.any { it.verification == "rejected" } == true) {
                                            startActivity(Intent(this@MainActivity, PhotosRejectionActivity::class.java))
                                            finishAffinity()
                                            swipeRight()
                                        } else {
                                            updateUI()

                                            initializeSocket()

                                            EventManager.postEvent(Event(EventConstants.INIT_SUCCESS, null))

                                            checkActiveSubscriptions(it.data.user.purchase)

                                            askForAppReview()
                                        }
                                    } else {
                                        initialFlowFailed()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initializeSocket() {
        if (getGistPrefs().socURL.isNotEmpty()) {
            if (SocketManager.connected) {
                val map = mutableMapOf<String, Any?>()
                map["user_id"] = getUserPrefs().userId
                map["status"] = true

                SocketManager.emit(SocketConstants.SET_ONLINE_STATUS, map)
            } else {
                SocketManager.connect()

                val map = mutableMapOf<String, Any?>()
                map["user_id"] = getUserPrefs().userId
                map["status"] = true

                SocketManager.emit(SocketConstants.SET_ONLINE_STATUS, map)
            }
        }
    }

    private fun updateUI() {
        invalidateOptionsMenu()

        viewModel.isInitLoading.set(false)

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        when (getUserPrefs().activePackage) {
            "Gold" -> {
                viewModel.activePackage.set(ProductType.GOLD)
            }

            "Platinum" -> {
                viewModel.activePackage.set(ProductType.PLATINUM)
            }

            "Lifetime" -> {
                viewModel.activePackage.set(ProductType.LIFETIME)
            }

            else -> {
                viewModel.activePackage.set(null)
            }
        }

        if (viewModel.activePackage.get() != ProductType.LIFETIME) {
            checkDateWithOnlineMode()
        }

        logger("--token--", getDeviceID())
        logger("--token--", getUserPrefs().userId)
        logger("--token--", getUserPrefs().userToken)
    }

    private val profileDetailActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data?.hasExtra("route") == true) {
                when (result.data?.getStringExtra("route")) {
                    "home" -> (EventManager.postEvent(Event(EventConstants.USER_DETAIL_PAGE_ACTION, result.data)))
                    "ai" -> (EventManager.postEvent(
                        Event(EventConstants.USER_DETAIL_PAGE_ACTION, result.data)
                    ))/*"like" -> (lsFragment?.onDetailsActivityCallBackReceived(result.data, "like"))
                    "super_like" -> (lsFragment?.onDetailsActivityCallBackReceived(
                        result.data, "super_like"
                    ))*/
                }
            }
        }
    }

    fun openProfileDetails(userId: Int, route: String) {
        val intent = Intent(this, UserDetailsActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("route", route)
        profileDetailActivityLauncher.launch(intent)
        swipeUp()
    }

    private fun checkActiveSubscriptions(backend: Purchase?) {
        // find mismatch between backend and revenuecat ===========================================================

        logger("--active_plans--", "backend: ${gsonString(backend)}")

        if ((backend != null) && (backend.productID == "lifetime_pkg")) {

        } else {
            PaymentUtils.getActiveSubscription { it ->
                val expiryDate = it?.expirationDate
                if (expiryDate != null) {
                    val currentDate = Calendar.getInstance().time
                    if (expiryDate.after(currentDate)) {
                        activePlan = it

                        logger("--active_plans--", "it activePlan: ${gsonString(it)}")

                        val map = mutableMapOf<String, Any?>()

                        map["is_topup"] = 0

                        map["product_id"] = "${activePlan!!.productPlanIdentifier}"

                        if (activePlan!!.expirationDate != null) {
                            map["end_date"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Timestamp(activePlan!!.expirationDate!!.time).toInstant().epochSecond
                            } else {
                                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                calendar.timeInMillis = activePlan!!.expirationDate!!.time
                                calendar.timeInMillis / 1000 // Convert milliseconds to seconds
                            }
                        }

                        map["start_date"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Timestamp(activePlan!!.latestPurchaseDate.time).toInstant().epochSecond
                        } else {
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = activePlan!!.latestPurchaseDate.time
                            calendar.timeInMillis / 1000 // Convert milliseconds to seconds
                        }

                        map["is_guest"] = false

                        if (backend != null) {
                            val activePlanDate = activePlan!!.expirationDate!!
                            val backendEndDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Instant.parse(backend.endDate).toEpochMilli() // API 26+
                            } else {
                                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                sdf.timeZone = TimeZone.getTimeZone("UTC") // Ensure UTC interpretation
                                sdf.parse(backend.endDate)?.time ?: 0L // Convert to milliseconds
                            }

                            logger("--active_plans--", "backendDate: $backendEndDate")
                            logger("--active_plans--", "activePlanDate: ${activePlanDate.time}")

                            val isProductIdSame = backend.productID == activePlan!!.productPlanIdentifier
                            val isExpiryDateSame = backendEndDate == activePlanDate.time

                            if (!isProductIdSame || !isExpiryDateSame) {
                                lifecycleScope.launch {
                                    viewModel.makePurchase(map).collectLatest { payment ->
                                        observePurchase(payment)
                                    }
                                }
                            }
                        } else {
                            lifecycleScope.launch {
                                viewModel.makePurchase(map).collectLatest { payment ->
                                    observePurchase(payment)
                                }
                            }
                        }
                    } else {
                        logger("--active_plans--", "checkActiveSubscriptions: plan expired")

                        PaymentUtils.reInit(onError = {
                            logger("--active_plans--", "error: ${gsonString(it)}")
                        }, onSuccess = {
                            checkActiveSubscriptions(backend)
                        })
                    }
                } else {
                    logger("--active_plans--", "checkActiveSubscriptions: expiryDate = null")

                    if (backend != null) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.cancelPurchase().collectLatest { cancel ->
                                observeCancelPurchase(cancel)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkDateWithOnlineMode() {
        val oldDate = getUserPrefs().currentDateForDailyCheck
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        val lastDate = oldDate?.let { it1 -> formatter.parse(it1) }
        val currentDate = formatter.parse(convertLongToStringDate(getUserPrefs().currentDateFromAPI))
        val currentDateString = currentDate?.let { formatter.format(it) }

        if (oldDate != null) {
            val differenceInMillis = abs((currentDate?.time ?: 0) - lastDate!!.time)
            val totalDays = differenceInMillis / (1000 * 60 * 60 * 24)

            if (currentDate != null) {
                if (currentDate > lastDate) {
                    if (totalDays.toInt() == 1) {
                        var day = getUserPrefs().checkInDay
                        logger("--time--", "DAY_CHECK_IN: $day")
                        logger("--time--", "ONLINE_TIME: ${getUserPrefs().dailyOnlineTime}")

                        when (day) {
                            7 -> {
                                getUserPrefs().dailyOnlineTime = 0
                                getUserPrefs().checkInDay = 1
//                                dailyCheckInApi()

                                getUserPrefs().isDailyCheckInAvailable = true
                            }

                            0 -> {
                                getUserPrefs().checkInDay = 1
//                                dailyCheckInApi()

                                getUserPrefs().isDailyCheckInAvailable = true
                            }

                            else -> {
                                getUserPrefs().dailyOnlineTime = 0
                                day += 1
                                getUserPrefs().checkInDay = day
//                                dailyCheckInApi()

                                getUserPrefs().isDailyCheckInAvailable = true
                            }
                        }
                    } else {
                        // call api with first day
                        getUserPrefs().dailyOnlineTime = 0
                        getUserPrefs().checkInDay = 1
//                        dailyCheckInApi()

                        getUserPrefs().isDailyCheckInAvailable = true
                    }
                } else if (currentDate < lastDate) {
                    // call api with first day
                    getUserPrefs().dailyOnlineTime = 0
                    getUserPrefs().checkInDay = 1
//                    dailyCheckInApi()

                    getUserPrefs().isDailyCheckInAvailable = true
                }
            }
        } else {
            getUserPrefs().dailyOnlineTime = 0
            getUserPrefs().checkInDay = 1
            getUserPrefs().currentDateForDailyCheck = currentDateString
//            dailyCheckInApi()

            getUserPrefs().isDailyCheckInAvailable = true
        }

        if (oldDate != currentDateString) {
            if (currentDateString != null) {
                getUserPrefs().currentDateForDailyCheck = currentDateString
            }
            getUserPrefs().dailyOnlineTime = 0
        }

        // todo: runtime
        /*Intent(this, OnlineTimeService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            startService(intent)
        }*/
//        startService( Intent(this, OnlineTimeService::class.java))
    }

    private fun CoroutineScope.observeCancelPurchase(cancelResource: Resource<JsonObject?>) {
        launch(Dispatchers.IO) {
            when (cancelResource.status) {
                Status.LOADING -> {
                    LoadingDialog.show(this@MainActivity)
                }

                Status.SIGN_OUT -> {
                    Toast.makeText(this@MainActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ADMIN_BLOCKED -> {
                    Toast.makeText(this@MainActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ERROR -> {
                    Toast.makeText(this@MainActivity, cancelResource.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                }

                Status.SUCCESS -> {
                    if (cancelResource.data != null) {
                        val jsonResponse = cancelResource.data

                        if (jsonResponse.has("data")) {
                            val isCancelled = jsonResponse.getAsJsonPrimitive("data").asBoolean

                            if (isCancelled) {
                                getUserPrefs().activePackage = null
                                getUserPrefs().purchaseStartDate = null
                                getUserPrefs().purchaseEndDate = null
                                getUserPrefs().isUnlimitedLikes = false
                                getUserPrefs().isUnlimitedRewinds = false
                                getUserPrefs().isLifetimeBadge = false
                                getUserPrefs().isPlatinumBadge = false
                                getUserPrefs().isGoldBadge = false
                                getUserPrefs().isLocationFilter = false
                                getUserPrefs().isAiMatchMaker = false
                                getUserPrefs().isNoAds = false

                                getUserPrefs().customLatitude = getUserPrefs().currentLatitude
                                getUserPrefs().customLongitude = getUserPrefs().currentLongitude
                                getUserPrefs().customLocationName = null
                                getUserPrefs().customCountry = getUserPrefs().currentCountry

                                EventManager.postEvent(Event(EventConstants.UPDATE_PURCHASE, null))

                                when (getUserPrefs().activePackage) {
                                    "Gold" -> {
                                        viewModel.activePackage.set(ProductType.GOLD)
                                    }

                                    "Platinum" -> {
                                        viewModel.activePackage.set(ProductType.PLATINUM)
                                    }

                                    "Lifetime" -> {
                                        viewModel.activePackage.set(ProductType.LIFETIME)
                                    }

                                    else -> {
                                        viewModel.activePackage.set(null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun CoroutineScope.observePurchase(paymentResource: Resource<PaymentResponse?>) {
        launch(Dispatchers.Main) {
            when (paymentResource.status) {
                Status.LOADING -> {
                    LoadingDialog.show(this@MainActivity)
                }

                Status.SIGN_OUT -> {
                    Toast.makeText(this@MainActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ADMIN_BLOCKED -> {
                    Toast.makeText(this@MainActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ERROR -> {
                    Toast.makeText(this@MainActivity, paymentResource.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                }

                Status.SUCCESS -> {
                    LoadingDialog.hide()

                    val paymentData = paymentResource.data?.payment

                    if (paymentResource.data != null && paymentData?.purchaseModel?.isTopUp == 0) {
                        getUserPrefs().activePackage = paymentData.purchaseModel.planName
                        getUserPrefs().purchaseStartDate = paymentData.purchaseModel.startDate
                        getUserPrefs().purchaseEndDate = paymentData.purchaseModel.endDate
                        getUserPrefs().isUnlimitedLikes = paymentData.purchaseModel.unlimitedLikes
                        getUserPrefs().isUnlimitedRewinds = paymentData.purchaseModel.unlimitedRewinds
                        getUserPrefs().isLifetimeBadge = paymentData.purchaseModel.lifetimeBadge
                        getUserPrefs().isPlatinumBadge = paymentData.purchaseModel.platinumBadge
                        getUserPrefs().isGoldBadge = paymentData.purchaseModel.goldBadge
                        getUserPrefs().isLocationFilter = paymentData.purchaseModel.locationFilter
                        getUserPrefs().isAiMatchMaker = paymentData.purchaseModel.aiMatchmaker
                        getUserPrefs().isNoAds = paymentData.purchaseModel.noAds
                        getUserPrefs().remainingDiamonds = paymentData.profileBalanceModel.totalDiamonds
                        getUserPrefs().remainingBoosts = paymentData.profileBalanceModel.totalBoosts
                        getUserPrefs().remainingSuperLikes = paymentData.profileBalanceModel.totalSuperLikes

                        getUserPrefs().customLatitude = getUserPrefs().currentLatitude
                        getUserPrefs().customLongitude = getUserPrefs().currentLongitude
                        getUserPrefs().customLocationName = null
                        getUserPrefs().customCountry = getUserPrefs().currentCountry

                        EventManager.postEvent(Event(EventConstants.UPDATE_PURCHASE, null))

                        when (getUserPrefs().activePackage) {
                            "Gold" -> {
                                viewModel.activePackage.set(ProductType.GOLD)
                            }

                            "Platinum" -> {
                                viewModel.activePackage.set(ProductType.PLATINUM)
                            }

                            "Lifetime" -> {
                                viewModel.activePackage.set(ProductType.LIFETIME)
                            }

                            else -> {
                                viewModel.activePackage.set(null)
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

                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        logger(TAG, "onInternetAvailableForFirstTime")

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        if (savedInstanceState == null && viewModel.isInitLoading.get() == true) {
            viewModel.isInitLoading.set(true)

            getGistData()
        } else {
            updateUI()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        logger(TAG, "onInternetConfigurationChanged")
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.UPDATE_NOTIFICATION_COUNT -> {

            }

            EventConstants.UPDATE_PURCHASE -> {
                when (getUserPrefs().activePackage) {
                    "Gold" -> {
                        viewModel.activePackage.set(ProductType.GOLD)
                    }

                    "Platinum" -> {
                        viewModel.activePackage.set(ProductType.PLATINUM)
                    }

                    "Lifetime" -> {
                        viewModel.activePackage.set(ProductType.LIFETIME)
                    }

                    else -> {
                        viewModel.activePackage.set(null)
                    }
                }
            }
        }
    }
}
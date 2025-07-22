package com.belive.dating.activities.user_details

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.LruCache
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.chat.ChatActivity
import com.belive.dating.activities.dashboard.fragments.home.UserCardAdapter.StoryPagerAdapter
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.activities.filter.FiltersActivity
import com.belive.dating.activities.paywalls.topups.like.LikePaywallActivity
import com.belive.dating.activities.paywalls.topups.super_like.SuperLikePaywallActivity
import com.belive.dating.activities.report.ReportUserActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.InterstitialGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.api.user.models.user_details.UserDetails
import com.belive.dating.databinding.ActivityUserDetailsBinding
import com.belive.dating.di.deepLinkViewModels
import com.belive.dating.di.reportViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.formatStringsForWidth
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getGlide
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getMaxAvailableWidth
import com.belive.dating.extensions.getScreenHeight
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gone
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.isOnlyActivity
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeDown
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.swipeUp
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules
import java.util.Locale
import kotlin.math.roundToInt

class UserDetailsActivity : NetworkReceiverActivity() {

    val binding by lazy {
        ActivityUserDetailsBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: UserDetailsViewModel

    private var isPreview = true

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(FiltersActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(FiltersActivity::class.java.simpleName)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.updateState()
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        viewModel.getState()
        super.onRestoreInstanceState(savedInstanceState)
    }

    private val reportActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = Intent()
            intent.putExtra("resultData", "Report")
            if (!isPreview) {
                intent.putExtra("route", this.intent.getStringExtra("route"))
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        logger("--user_details--", "onCreate")

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = tryKoinViewModel(listOf(deepLinkViewModels))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        initViews()

        clickListeners()
    }

    private fun getUserDetails() {
        isPreview = if (intent.hasExtra("isPreview")) {
            intent.getBooleanExtra("isPreview", false)
        } else {
            false
        }

        if (viewModel.userDetails.get() != null) {
            loadUser()
        } else {
            val appLinkIntent: Intent = intent
            val userId: String? =
                appLinkIntent.data?.getQueryParameter("userId") ?: if (appLinkIntent.hasExtra("userId")) appLinkIntent.getIntExtra("userId", -1)
                    .toString() else null
            val notificationID: String? =
                appLinkIntent.data?.getQueryParameter("notiId") ?: if (appLinkIntent.hasExtra("notiId")) appLinkIntent.getIntExtra("notiId", -1)
                    .toString() else null

            lifecycleScope.launch(Dispatchers.IO) {
                if (getUserPrefs().userId == -1) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@UserDetailsActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }

                    authOut()
                } else {
                    launch {
                        viewModel.getUserDetails(userId!!.toInt()).collectLatest {
                            launch(Dispatchers.Main) {
                                when (it.status) {
                                    Status.LOADING -> {
                                        LoadingDialog.show(this@UserDetailsActivity)
                                    }

                                    Status.ADMIN_BLOCKED -> {
                                        Toast.makeText(
                                            this@UserDetailsActivity,
                                            "Admin has blocked you, because of security reasons.",
                                            Toast.LENGTH_SHORT,
                                        ).show()

                                        authOut()
                                    }

                                    Status.SIGN_OUT -> {
                                        Toast.makeText(this@UserDetailsActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                            .show()

                                        authOut()
                                    }

                                    Status.ERROR -> {
                                        LoadingDialog.hide()

                                        Toast.makeText(this@UserDetailsActivity, it.message, Toast.LENGTH_SHORT).show()

                                        onBackPressedDispatcher.onBackPressed()
                                    }

                                    Status.SUCCESS -> {
                                        LoadingDialog.hide()

                                        if (it.data != null) {
                                            viewModel.userDetails.set(it.data.userDetails)

                                            loadUser()

                                            if ((notificationID == null) || (notificationID == "-1")) {
                                                launch(Dispatchers.IO) {
                                                    viewModel.readProfile().collectLatest {

                                                    }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(this@UserDetailsActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if ((notificationID != null) && (notificationID != "-1")) {
                        launch {
                            viewModel.readNotification(notificationID.toInt())
                                .collect {
                                    when (it.status) {
                                        Status.LOADING -> {

                                        }

                                        Status.ADMIN_BLOCKED -> {
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@UserDetailsActivity,
                                                    "Admin has blocked you, because of security reasons.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }

                                            authOut()
                                        }

                                        Status.SIGN_OUT -> {
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@UserDetailsActivity,
                                                    "Your session has expired, Please log in again.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }

                                            authOut()
                                        }

                                        Status.ERROR -> {
                                            logger("--error--", "notification read error: ${gsonString(it)}")
                                        }

                                        Status.SUCCESS -> {
                                            val unreadNotification = it.data?.get("unreadNotification")?.asJsonPrimitive?.asInt
                                            if (unreadNotification != null) {
                                                getUserPrefs().unreadNotificationCount = unreadNotification

                                                logger("--notification--", "unreadNotification: $unreadNotification")

                                                val intent = Intent("NOTIFICATION_COUNT")
                                                intent.putExtra("isNotificationCountChanged", true)
                                                LocalBroadcastManager.getInstance(this@UserDetailsActivity).sendBroadcast(intent)
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    private fun loadUser() {
        ManageAds.showSmallNativeAd(SmallNativeGroup.Detail, binding.adSmallNative)

        if ((viewModel.userDetails.get()?.fullName != null) && (viewModel.userDetails.get()?.age != null)) {
            binding.txtTitle.getMaxAvailableWidth {
                formatStringsForWidth(
                    viewModel.userDetails.get()!!.fullName!!.trim(),
                    StringBuilder().append("• ").append(viewModel.userDetails.get()?.age).toString().trim(),
                    binding.txtTitle,
                    it,
                ) { spannableString ->
                    binding.title = spannableString
                }
            }
        }

        binding.location = getLocation()
        binding.distance = getDistance()
        binding.gender = getGender()
        binding.oppositeGender = getOppositeGender()
        binding.height = getHeight()
        binding.languages = getKnownLanguages()
        binding.school = getSchool()

        val showBasics = getBasics()
        binding.showBasics = showBasics
        val showStyles = getStyles()
        binding.showStyles = showStyles
        val showLifeStyles = getLifeStyles()
        binding.showLifeStyles = showLifeStyles

        if (showLifeStyles || showStyles || showBasics) {
            (binding.dividerShare.layoutParams as LinearLayout.LayoutParams).apply {
                topMargin = getDimensionPixelOffset(com.intuit.sdp.R.dimen._32sdp)
            }
        } else {
            (binding.dividerShare.layoutParams as LinearLayout.LayoutParams).apply {
                topMargin = getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
            }
        }

        storyView(viewModel.userDetails.get())

        defaultImage()
    }

    private fun getLifeStyles(): Boolean {
        val userDetails = viewModel.userDetails.get() ?: return false

        val pet = if (userDetails.pet.isNullOrEmpty()) {
            null
        } else {
            userDetails.pet
        }
        val drinking = if (userDetails.drinking.isNullOrEmpty()) {
            null
        } else {
            userDetails.drinking
        }
        val smoking = if (userDetails.smoking.isNullOrEmpty()) {
            null
        } else {
            userDetails.smoking
        }
        val workout = if (userDetails.workout.isNullOrEmpty()) {
            null
        } else {
            userDetails.workout
        }
        val preferredDiet = if (userDetails.preferredDiet.isNullOrEmpty()) {
            null
        } else {
            userDetails.preferredDiet
        }
        val socialStatus = if (userDetails.socialStatus.isNullOrEmpty()) {
            null
        } else {
            userDetails.socialStatus
        }
        val sleepHabit = if (userDetails.sleepHabit.isNullOrEmpty()) {
            null
        } else {
            userDetails.sleepHabit
        }

        binding.pet = pet
        binding.drinking = drinking
        binding.smoking = smoking
        binding.workout = workout
        binding.preferredDiet = preferredDiet
        binding.socialStatus = socialStatus
        binding.sleepHabit = sleepHabit

        return !((pet == null) && (drinking == null) && (smoking == null) && (workout == null) && (preferredDiet == null) && (socialStatus == null) && (sleepHabit == null))
    }

    private fun getStyles(): Boolean {
        val userDetails = viewModel.userDetails.get() ?: return false

        val personalityType = if (userDetails.personalityType.isNullOrEmpty()) {
            null
        } else {
            if (!userDetails.personalityType.contains(",")) {
                userDetails.personalityType
            } else {
                val list = userDetails.personalityType.split(",")

                list.joinToString(", ") { it }
            }
        }
        val communicationType = if (userDetails.communicationType.isNullOrEmpty()) {
            null
        } else {
            if (!userDetails.communicationType.contains(",")) {
                userDetails.communicationType
            } else {
                val list = userDetails.communicationType.split(",")

                list.joinToString(", ") { it }
            }
        }
        val loveType = if (userDetails.loveType.isNullOrEmpty()) {
            null
        } else {
            if (!userDetails.loveType.contains(",")) {
                userDetails.loveType
            } else {
                val list = userDetails.loveType.split(",")

                list.joinToString(", ") { it }
            }
        }

        binding.personalityType = personalityType
        binding.communicationType = communicationType
        binding.loveType = loveType

        return !((personalityType == null) && (communicationType == null) && (loveType == null))
    }

    private fun getBasics(): Boolean {
        val userDetails = viewModel.userDetails.get() ?: return false

        val zodiac = if (userDetails.zodiac?.name.isNullOrEmpty()) {
            null
        } else {
            userDetails.zodiac?.name
        }
        val education = if (userDetails.education?.name.isNullOrEmpty()) {
            null
        } else {
            userDetails.education?.name
        }
        val religion = if (userDetails.religion?.name.isNullOrEmpty()) {
            null
        } else {
            userDetails.religion?.name
        }
        val maritalStatus = if (userDetails.maritalStatus?.name.isNullOrEmpty()) {
            null
        } else {
            userDetails.maritalStatus?.name
        }
        val familyPlan = if (userDetails.familyPlan?.name.isNullOrEmpty()) {
            null
        } else {
            userDetails.familyPlan?.name
        }

        binding.zodiac = zodiac
        binding.education = education
        binding.religion = religion
        binding.maritalStatus = maritalStatus
        binding.familyPlan = familyPlan

        return !((zodiac == null) && (education == null) && (religion == null) && (maritalStatus == null) && (familyPlan == null))
    }

    private fun getGender(): String? {
        return viewModel.userDetails.get()?.gender?.lowercase()?.let {
            it.replaceFirstChar { char -> char.uppercaseChar() }
        }
    }

    private fun getOppositeGender(): String? {
        return viewModel.userDetails.get()?.oppositeGender?.lowercase()?.let {
            it.replaceFirstChar { char -> char.uppercaseChar() }
        }
    }

    private fun getKnownLanguages(): String? {
        val languages = viewModel.userDetails.get()?.knownLanguage

        return if (languages.isNullOrEmpty()) {
            null
        } else {
            if (!languages.contains(",")) {
                languages
            } else {
                val list = languages.split(",")

                list.joinToString(", ") { it }
            }
        }
    }

    private fun getSchool(): String? {
        val school = viewModel.userDetails.get()?.school

        return if (school.isNullOrEmpty()) {
            null
        } else {
            school
        }
    }

    private fun getHeight(): String? {
        return if (viewModel.userDetails.get()?.height == null) {
            null
        } else {
            val feet = viewModel.userDetails.get()!!.height!!.feet
            val inch = viewModel.userDetails.get()!!.height!!.inch
            val centimeters = ((feet * 30.48) + (inch * 2.54)).roundToInt()

            return "${feet}` ${inch}`` ( $centimeters cm )"
        }
    }

    private fun getDistance(): String? {
        return if (viewModel.userDetails.get()?.isFake == true) {
            StringBuilder().append(" ~ ")
                .append("${(11..50).random()} ${if (getUserPrefs().countryCode.equals("IN", true)) "km" else "miles"}").append(" away").toString()
        } else if (viewModel.userDetails.get()?.isFake == false) {
            if (getUserPrefs().countryCode == "IN") {
                if (viewModel.userDetails.get()?.distance?.toInt() == 0) {
                    StringBuilder().append(" ~ ").append("${(11..50).random()} km away").toString()
                } else {
                    StringBuilder().append(" ~ ").append(String.format(Locale.getDefault(), "%.2f", viewModel.userDetails.get()?.distance))
                        .append(" km away").toString()
                }
            } else {
                if (viewModel.userDetails.get()?.distance?.toInt() == 0) {
                    StringBuilder().append(" ~ ").append("${(11..50).random()} miles away")
                        .toString()
                } else {
                    StringBuilder().append(" ~ ").append(String.format(Locale.getDefault(), "%.2f", viewModel.userDetails.get()?.distance))
                        .append(" miles away").toString()
                }
            }
        } else {
            null
        }
    }

    private fun getLocation(): String? {
        return if (viewModel.userDetails.get()?.isFake == true) {
            if (!getUserPrefs().currentCity.isNullOrEmpty()) {
                getUserPrefs().currentCity!!
            } else if (!getUserPrefs().currentState.isNullOrEmpty()) {
                getUserPrefs().currentState!!
            } else if (!getUserPrefs().currentCountry.isNullOrEmpty()) {
                getUserPrefs().currentCountry!!
            } else {
                null
            }
        } else {
            if (!viewModel.userDetails.get()?.city.isNullOrEmpty() && !viewModel.userDetails.get()?.state.isNullOrEmpty() && !viewModel.userDetails.get()?.country.isNullOrEmpty()) {
                "${viewModel.userDetails.get()?.city}, ${viewModel.userDetails.get()?.state}, ${viewModel.userDetails.get()?.country}"
            } else if (!viewModel.userDetails.get()?.state.isNullOrEmpty() && !viewModel.userDetails.get()?.country.isNullOrEmpty()) {
                "${viewModel.userDetails.get()?.state}, ${viewModel.userDetails.get()?.country}"
            } else if (!viewModel.userDetails.get()?.country.isNullOrEmpty()) {
                viewModel.userDetails.get()?.country
            } else {
                null
            }
        }
    }

    private fun defaultImage() {
        if (viewModel.userDetails.get()?.id?.let { getBitmapFromCache(it, binding.viewPager.currentItem) } != null) {
            try {
                val bitmap = getBitmapFromCache(viewModel.userDetails.get()!!.id, binding.viewPager.currentItem)
                if (bitmap != null) {
                    binding.userImage = bitmap
                }
            } catch (e: Exception) {
                catchLog("CardStackAdapter: ${gsonString(e)}")
            }
        } else {
            viewModel.userDetails.get()?.userImages?.getOrNull(0)?.let { first ->
                getGlide().asBitmap().load(getGistPrefs().imagesURL + first)
                    .override(getKoinContext().resources.displayMetrics.widthPixels) // Keep original aspect ratio
                    .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                    .priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL).into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            binding.imgUser.gone()

                            binding.userImage = resource

                            addBitmapToCache(viewModel.userDetails.get()!!.id, binding.viewPager.currentItem, resource)

                            if ((viewModel.userDetails.get()!!.userImages != null) && (viewModel.userDetails.get()!!.userImages!!.size > 1)) {
                                try {
                                    viewModel.userDetails.get()!!.userImages?.get(1).let { second ->
                                        getGlide().asBitmap().load(getGistPrefs().imagesURL + second)
                                            .override(getKoinContext().resources.displayMetrics.widthPixels) // Keep original aspect ratio
                                            .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                                            .priority(Priority.LOW).diskCacheStrategy(DiskCacheStrategy.ALL).into(object : CustomTarget<Bitmap>() {
                                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                    try {
                                                        addBitmapToCache(viewModel.userDetails.get()!!.id, 1, resource)
                                                    } catch (e: Exception) {
                                                        catchLog("CardStackAdapter: ${gsonString(e)}")
                                                    }
                                                }

                                                override fun onLoadCleared(placeholder: Drawable?) {

                                                }
                                            })
                                    }
                                } catch (e: Exception) {
                                    catchLog("CardStackAdapter: ${gsonString(e)}")
                                }
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            }
        }
    }

    private fun displayForwardImage() {
        if (viewModel.userDetails.get() == null) {
            return
        }

        if ((viewModel.userDetails.get()!!.userImages != null) && (binding.viewPager.currentItem >= 0) && (binding.viewPager.currentItem < viewModel.userDetails.get()!!.userImages!!.size - 1)) {
            binding.viewPager.currentItem += 1

            val bitmap = getBitmapFromCache(viewModel.userDetails.get()!!.id, binding.viewPager.currentItem)

            if (bitmap != null) {
                binding.userImage = bitmap
            } else {
                binding.userImage = null

                getGlide().asBitmap().load(getGistPrefs().imagesURL + viewModel.userDetails.get()!!.userImages!![binding.viewPager.currentItem])
                    .override(getKoinContext().resources.displayMetrics.widthPixels) // Keep original aspect ratio
                    .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                    .priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL).into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            try {
                                binding.userImage = resource

                                addBitmapToCache(viewModel.userDetails.get()!!.id, binding.viewPager.currentItem, resource)

                                if ((viewModel.userDetails.get()!!.userImages!!.size - 1) > binding.viewPager.currentItem) {
                                    if (getBitmapFromCache(viewModel.userDetails.get()!!.id, binding.viewPager.currentItem + 1) == null) {
                                        getGlide().asBitmap()
                                            .load(getGistPrefs().imagesURL + viewModel.userDetails.get()!!.userImages!![binding.viewPager.currentItem + 1])
                                            .priority(Priority.LOW)
                                            .override(getKoinContext().resources.displayMetrics.widthPixels) // Keep original aspect ratio
                                            .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                                            .diskCacheStrategy(DiskCacheStrategy.ALL).into(object : CustomTarget<Bitmap>() {
                                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                    try {
                                                        addBitmapToCache(
                                                            viewModel.userDetails.get()!!.id,
                                                            binding.viewPager.currentItem + 1,
                                                            resource
                                                        )
                                                    } catch (e: Exception) {
                                                        catchLog("CardStackAdapter: ${gsonString(e)}}")
                                                    }
                                                }

                                                override fun onLoadCleared(placeholder: Drawable?) {

                                                }
                                            })
                                    }
                                }
                            } catch (e: Exception) {
                                catchLog("CardStackAdapter: ${gsonString(e)}")
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            }
        }
    }

    private fun displayBackwardImage() {
        if (viewModel.userDetails.get() == null) {
            return
        }

        if ((viewModel.userDetails.get()!!.userImages != null) && (binding.viewPager.currentItem > 0) && (binding.viewPager.currentItem <= viewModel.userDetails.get()!!.userImages!!.size - 1)) {
            try {
                binding.viewPager.currentItem -= 1

                val bitmap = getBitmapFromCache(viewModel.userDetails.get()!!.id, binding.viewPager.currentItem)

                if (bitmap != null) {
                    try {
                        binding.userImage = bitmap
                    } catch (e: Exception) {
                        catchLog("CardStackAdapter: ${gsonString(e)}")
                    }
                } else {
                    binding.userImage = null

                    getGlide().asBitmap().load(getGistPrefs().imagesURL + viewModel.userDetails.get()!!.userImages!![binding.viewPager.currentItem])
                        .override(getKoinContext().resources.displayMetrics.widthPixels) // Keep original aspect ratio
                        .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                        .priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL).into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                binding.userImage = resource

                                addBitmapToCache(viewModel.userDetails.get()!!.id, binding.viewPager.currentItem, resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {

                            }
                        })
                }
            } catch (e: Exception) {
                catchLog("CardStackAdapter: ${gsonString(e)}}")
            }
        }
    }

    private val bitmapCache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt() // Use 1/8th of available memory
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            logger(
                "--lru--", "bitmapCache ram size: ${Runtime.getRuntime().maxMemory() / 1024 / 8}"
            )
            logger("--lru--", "bitmapCache bitmap size: ${(value.byteCount / 1024) / 1024}")
            return value.byteCount / 1024 // Measure size in kilobytes
        }
    }

    // Add bitmap to cache
    private fun addBitmapToCache(userId: Int, imagePosition: Int, bitmap: Bitmap) {
        logger("--lru--", "addBitmapToCache imagePosition: $imagePosition")
        logger("--lru--", "addBitmapToCache userId: $userId")
        logger("--lru--", "addBitmapToCache bitmap size: ${(bitmap.byteCount / 1024) / 1024}")

        lifecycleScope.launch(Dispatchers.IO) {
            if (bitmapCache["$userId$imagePosition"] == null) {
                bitmapCache.put("$userId$imagePosition", bitmap)
            }
        }
    }

    // Retrieve bitmap from cache
    private fun getBitmapFromCache(userId: Int, imagePosition: Int): Bitmap? {
        val bitmap = bitmapCache["$userId$imagePosition"]
        if (bitmap != null) {
            logger("--lru--", "getBitmapFromCache userId: $userId")
            logger("--lru--", "getBitmapFromCache imagePosition: $imagePosition")
            logger("--lru--", "getBitmapFromCache bitmap size: ${(bitmap.byteCount / 1024) / 1024}")
        }
        return bitmap
    }

    private fun storyView(userDetails: UserDetails?) {
        if ((userDetails == null) || (userDetails.userImages == null)) {
            binding.tabIndicator.gone()
            return
        }

        binding.viewPager.isUserInputEnabled = false

        binding.viewPager.adapter = userDetails.userImages.size.let {
            StoryPagerAdapter(this, it)
        }

        if ((binding.viewPager.adapter != null) && (binding.viewPager.adapter!!.itemCount > 1)) {
            binding.tabIndicator.visibility = View.VISIBLE
            TabLayoutMediator(
                binding.tabIndicator,
                binding.viewPager,
            ) { _, _ ->
            }.attach()

            for (i in 0 until binding.tabIndicator.tabCount) {
                val tab = (binding.tabIndicator.getChildAt(0) as ViewGroup).getChildAt(i)
                val p = tab.layoutParams as MarginLayoutParams
                p.setMargins(
                    getKoinContext().resources.getDimension(com.intuit.sdp.R.dimen._2sdp).toInt(),
                    0,
                    getKoinContext().resources.getDimension(com.intuit.sdp.R.dimen._2sdp).toInt(),
                    0,
                )
                tab.requestLayout()
            }
        } else {
            binding.tabIndicator.visibility = View.GONE
        }
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ManageAds.showInterstitialAd(InterstitialGroup.Detail) {
                    if (isOnlyActivity()) {
                        startActivity(
                            Intent(
                                this@UserDetailsActivity, MainActivity::class.java
                            ).apply {
                                putExtra("display_splash", false)
                            })
                    } else {
                        if (intent.getStringExtra("route") == "ai") {
                            setResult(RESULT_OK, Intent().apply {
                                putExtra("route", intent.getStringExtra("route"))
                            })
                        }
                    }
                    finish()
                    swipeDown()
                }
            }
        })

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.backward.setOnClickListener {
            displayBackwardImage()
        }

        binding.forward.setOnClickListener {
            displayForwardImage()
        }

        binding.btnSkip.setOnClickListener {
            if (isPreview) {
                return@setOnClickListener
            }

            if (viewModel.userDetails.get() == null) {
                Toast.makeText(this, "Something went wrong...!", Toast.LENGTH_SHORT).show()
            } else {
                skipProfile()
            }
        }

        binding.btnSuperLike.setOnClickListener {
            if (isPreview) {
                return@setOnClickListener
            }

            if (viewModel.userDetails.get() == null) {
                return@setOnClickListener
            } else {
                superLikeProfile()
            }
        }

        binding.btnLike.setOnClickListener {
            if (isPreview) {
                return@setOnClickListener
            }

            if (viewModel.userDetails.get() == null) {
                return@setOnClickListener
            } else {
                likeProfile()
            }
        }

        binding.btnMessage.setOnClickListener {
            if (isPreview) {
                return@setOnClickListener
            }

            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("userId", viewModel.userDetails.get()?.id)
            })
            swipeRight()
        }

        binding.btnShare.setOnClickListener {
            if (isPreview) {
                return@setOnClickListener
            }

            viewModel.userDetails.get()?.let { userDetails -> shareUser(userDetails.id) }
        }

        binding.btnReport.setOnClickListener {
            if (isPreview) {
                return@setOnClickListener
            }

            loadKoinModules(reportViewModel)

            reportActivityLauncher.launch(Intent(this, ReportUserActivity::class.java).apply {
                putExtra("profile_id", viewModel.userDetails.get()?.id)
            })
            swipeUp()
        }

        binding.btnBlock.setOnClickListener {
            if (isPreview) {
                return@setOnClickListener
            }

            showBlockDialog()
        }
    }

    private fun shareUser(userId: Int) {
        // Generate the shareable link
        val shareableLink =
            "https://belivedating.com/link.html?redirect=profile&userId=$userId" // Replace with your actual link

        // Create the Intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // Plain text for the link
            putExtra(Intent.EXTRA_SUBJECT, "Check out this profile!")
            putExtra(Intent.EXTRA_TEXT, "Hey! Check out this profile: $shareableLink")
        }

        // Launch the share sheet
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun likeProfile() {
        if (getUserPrefs().isUnlimitedLikes || (getUserPrefs().remainingLikes > 0)) {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.likeProfile(1).collectLatest {
                    launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {
                                LoadingDialog.show(this@UserDetailsActivity)
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

                                Toast.makeText(this@UserDetailsActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }

                            Status.SUCCESS -> {
                                LoadingDialog.hide()

                                if (it.data != null) {
                                    if (it.data.likeProfile != null) {
                                        getUserPrefs().remainingLikes = it.data.likeProfile.totalLike
                                        getUserPrefs().remainingSuperLikes = it.data.likeProfile.totalSuperLike

                                        if (isOnlyActivity()) {
                                            startActivity(Intent(this@UserDetailsActivity, MainActivity::class.java).apply {
                                                putExtra("display_splash", false)
                                            })
                                        } else {
                                            val intent = Intent()
                                            intent.putExtra("resultData", "Like")
                                            intent.putExtra("deductFrom", it.data.likeProfile.deductFrom)
                                            intent.putExtra("profileId", viewModel.userDetails.get()?.id)
                                            if (!isPreview) {
                                                intent.putExtra("route", this@UserDetailsActivity.intent.getStringExtra("route"))
                                            }
                                            setResult(RESULT_OK, intent)
                                        }
                                    } else {
                                        Toast.makeText(this@UserDetailsActivity, it.data.message, Toast.LENGTH_SHORT).show()
                                    }
                                    finish()
                                    swipeDown()
                                } else {
                                    Toast.makeText(this@UserDetailsActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            startActivity(Intent(this, LikePaywallActivity::class.java))
            swipeUp()
        }
    }

    private fun superLikeProfile() {
        if (getUserPrefs().remainingSuperLikes > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.likeProfile(2).collectLatest {
                    launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {
                                LoadingDialog.show(this@UserDetailsActivity)
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

                                Toast.makeText(this@UserDetailsActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }

                            Status.SUCCESS -> {
                                LoadingDialog.hide()

                                if (it.data != null) {
                                    if (it.data.likeProfile != null) {
                                        getUserPrefs().remainingLikes = it.data.likeProfile.totalLike
                                        getUserPrefs().remainingSuperLikes = it.data.likeProfile.totalSuperLike

                                        if (isOnlyActivity()) {
                                            startActivity(Intent(this@UserDetailsActivity, MainActivity::class.java).apply {
                                                putExtra("display_splash", false)
                                            })
                                        } else {
                                            val intent = Intent()
                                            intent.putExtra("resultData", "SuperLike")
                                            intent.putExtra("deductFrom", it.data.likeProfile.deductFrom)
                                            intent.putExtra("profileId", viewModel.userDetails.get()?.id)
                                            if (!isPreview) {
                                                intent.putExtra("route", this@UserDetailsActivity.intent.getStringExtra("route"))
                                            }
                                            setResult(RESULT_OK, intent)
                                        }
                                    } else {
                                        Toast.makeText(this@UserDetailsActivity, it.data.message, Toast.LENGTH_SHORT).show()
                                    }
                                    finish()
                                    swipeDown()
                                } else {
                                    Toast.makeText(this@UserDetailsActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            startActivity(Intent(this, SuperLikePaywallActivity::class.java))
            swipeUp()
        }
    }

    private fun skipProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.skipProfile().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@UserDetailsActivity)
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

                            Toast.makeText(this@UserDetailsActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                if (isOnlyActivity()) {
                                    startActivity(Intent(this@UserDetailsActivity, MainActivity::class.java).apply {
                                        putExtra("display_splash", false)
                                    })
                                } else {
                                    val intent = Intent()
                                    intent.putExtra("resultData", "Skip")
                                    intent.putExtra("profileId", viewModel.userDetails.get()?.id)
                                    if (!isPreview) {
                                        intent.putExtra("route", this@UserDetailsActivity.intent.getStringExtra("route"))
                                    }
                                    setResult(RESULT_OK, intent)
                                }
                                finish()
                                swipeDown()
                            } else {
                                Toast.makeText(this@UserDetailsActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false) // Disable back button

        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START

        binding.rvInterests.layoutManager = layoutManager

        val adapter = UserInterestAdapter(viewModel)
        binding.rvInterests.adapter = adapter

        binding.imageContainer.layoutParams.height = (getScreenHeight() / 1.5).toInt()
    }

    private fun showBlockDialog() {
        val dialog = Dialog(this)
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

        txtBlockUserName.text = StringBuilder().append("Are you sure, want to block ").append(viewModel.userDetails.get()?.fullName).append("?")

        btnBlock.setOnClickListener {
            dialog.dismiss()

            blockUser()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun blockUser() {
        lifecycleScope.launch(Dispatchers.IO) {
            val map = mutableMapOf<String, Any?>()
            map["block_id"] = viewModel.userDetails.get()?.id

            viewModel.blockUser(map).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@UserDetailsActivity)
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

                            Toast.makeText(this@UserDetailsActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                val rootJson = it.data

                                if (rootJson.has("status") && rootJson.getAsJsonPrimitive("status").asBoolean) {
                                    if (isOnlyActivity()) {
                                        startActivity(Intent(this@UserDetailsActivity, MainActivity::class.java).apply {
                                            putExtra("display_splash", false)
                                        })
                                    } else {
                                        val intent = Intent()
                                        intent.putExtra("resultData", "Block")
                                        if (!isPreview) {
                                            intent.putExtra("route", this@UserDetailsActivity.intent.getStringExtra("route"))
                                        }
                                        setResult(RESULT_OK, intent)
                                        finish()
                                        swipeDown()
                                    }
                                } else {
                                    Toast.makeText(this@UserDetailsActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                }
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

                startActivity(Intent(this@UserDetailsActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        getUserDetails()
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            getUserDetails()
        }
    }
}
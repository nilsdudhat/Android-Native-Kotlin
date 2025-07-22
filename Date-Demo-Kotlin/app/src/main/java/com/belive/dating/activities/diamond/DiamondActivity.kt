package com.belive.dating.activities.diamond

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.diamond.history.DiamondHistoryActivity
import com.belive.dating.activities.paywalls.topups.diamond.DiamondPaywallActivity
import com.belive.dating.activities.profile.ProfileActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.InterstitialGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.api.user.models.diamond_question.Description
import com.belive.dating.api.user.models.diamond_question.Question
import com.belive.dating.api.user.models.diamond_question.QuestionData
import com.belive.dating.constants.EventConstants
import com.belive.dating.constants.IntroductionConstants
import com.belive.dating.constants.UserConstants
import com.belive.dating.databinding.ActivityDiamondBinding
import com.belive.dating.di.diamondViewModel
import com.belive.dating.di.profileViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.convertMinutesToHoursMinutes
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.openBrowser
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.swipeUp
import com.belive.dating.extensions.throttleFirstClick
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DiamondActivity : NetworkReceiverActivity(), DailyCheckInBottomDialog.OnClaimListener {

    val binding by lazy {
        ActivityDiamondBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: DiamondViewModel

    private val dailyCheckInDiamondBottomSheet by lazy { DailyCheckInBottomDialog(this, viewModel.questionData.get()!!.dailyCheckInReward, this) }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(DiamondActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(DiamondActivity::class.java.simpleName)
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = tryKoinViewModel(listOf(diamondViewModel))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
            listenEvents()
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                unloadKoinModules(diamondViewModel)

                finish()
                swipeLeft()
            }
        })

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnHistory.setOnClickListener {
            ManageAds.showInterstitialAd(InterstitialGroup.Diamond) {
                startActivity(Intent(this, DiamondHistoryActivity::class.java))
                swipeRight()
            }
        }

        binding.layoutDiamonds.setOnClickListener {
            startActivity(Intent(this, DiamondPaywallActivity::class.java))
            swipeUp()
        }

        binding.btnWatchAds.throttleFirstClick {

        }

        binding.btnCheckIn.throttleFirstClick {
            dailyCheckInDiamondBottomSheet.show()
        }

        binding.btnOnlineTime.throttleFirstClick {
            lifecycleScope.launch(Dispatchers.IO) {
                val map = mapOf(
                    "que_cat_id" to 1,
                    "que_id" to 3,
                    "is_daily" to false,
                ).toMutableMap<String, Any?>()

                viewModel.claimDiamond(map).collect {
                    launch(Dispatchers.Main) {
                        manageDiamondResult(it) {

                        }
                    }
                }
            }
        }

        binding.btnRateApp.throttleFirstClick {
            lifecycleScope.launch(Dispatchers.IO) {
                val map = mapOf(
                    "que_cat_id" to 2,
                    "que_id" to 5,
                    "is_daily" to false,
                ).toMutableMap<String, Any?>()

                viewModel.claimDiamond(map).collect {
                    launch(Dispatchers.Main) {
                        manageDiamondResult(it) {
                            openBrowser("https://play.google.com/store/apps/details?id=$packageName")
                        }
                    }
                }
            }
        }

        binding.btnInviteFriend.throttleFirstClick {

        }

        binding.btnCompleteProfile.throttleFirstClick {
            val completeProfile = getUserPrefs().completeProfilePercentage
            val profile: Description? = getQuestionData(binding.txtCompleteProfileTitle.text.toString(), 2)
            if (profile!!.isClaimed) {
                viewModel.isCompleteProfileEnabled.set(false)
            } else {
                if (completeProfile < 100) {
                    loadKoinModules(profileViewModel)

                    startActivity(Intent(this, ProfileActivity::class.java))
                    swipeRight()
                } else {
                    val map = mutableMapOf<String, Any?>(
                        "que_cat_id" to 3,
                        "que_id" to 7,
                        "is_daily" to false,
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.claimDiamond(map).collect {
                            launch(Dispatchers.Main) {
                                manageDiamondResult(it) {

                                }
                            }
                        }
                    }
                }
            }
        }

        binding.btnAboutMe.throttleFirstClick {
            val bio = getUserPrefs().aboutMe
            val aboutMe: Description? = getQuestionData(binding.txtAboutMeTitle.text.toString(), 2)
            if (aboutMe!!.isClaimed) {
                viewModel.isWriteAboutMeEnabled.set(false)
            } else {
                if (bio.isNullOrEmpty()) {
                    loadKoinModules(profileViewModel)

                    startActivity(Intent(this, ProfileActivity::class.java))
                    swipeRight()
                } else {
                    LoadingDialog.show(this)

                    val map = mutableMapOf<String, Any?>(
                        "que_cat_id" to 3,
                        "que_id" to 8,
                        "is_daily" to false,
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.claimDiamond(map).collect {
                            launch(Dispatchers.Main) {
                                manageDiamondResult(it) {

                                }
                            }
                        }
                    }
                }
            }
        }

        binding.btnUploadPhotos.throttleFirstClick {
            val userImageArrayList = getUserPrefs().userImages

            if (userImageArrayList?.size == 6) {
                val map = mutableMapOf<String, Any?>(
                    "que_cat_id" to 3,
                    "que_id" to 9,
                    "is_daily" to false,
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.claimDiamond(map).collect {
                        launch(Dispatchers.Main) {
                            manageDiamondResult(it) {

                            }
                        }
                    }
                }
            } else {
                loadKoinModules(profileViewModel)

                startActivity(Intent(this, ProfileActivity::class.java))
                swipeRight()
            }
        }
    }

    private fun initViews() {

    }

    private fun setWatchAds() {
        //
        /*val watchAds: DiamondDescription? =
            getQuestionData(binding.txtWatchAdDiamonds.text.toString(), 0)
        if (watchAds != null) {
            binding.txtWatchAdDiamonds.text = StringBuilder().append(watchAds.diamond)
        } else {
            binding.containerWatchAds.visibility = View.GONE
        }*/

        viewModel.isWatchAdsAvailable.set(false)
    }

    private fun setDailyCheckIn() {
        val day = getUserPrefs().checkInDay

        val dailyCheckIn: Description? = getQuestionData(binding.txtDailyCheckInTitle.text.toString(), 0)

        if (dailyCheckIn != null) {
            viewModel.isDailyCheckInAvailable.set(true)

            try {
                val dailyDiamondMap = viewModel.questionData.get()!!.dailyCheckInReward
                val diamond: Int = dailyDiamondMap[day.toString()]!!
                binding.txtCheckInDiamonds.text = StringBuilder().append(diamond)

                val isDailyCheckIn = getUserPrefs().isDailyCheckInAvailable

                logger("--daily_check--", "setDailyCheckIn: IS_DAILY_CHECK_IN = $isDailyCheckIn")

                viewModel.isDailyCheckInEnabled.set(isDailyCheckIn)
            } catch (e: Exception) {
                catchLog("setDailyCheckIn: ${gsonString(e)}")

                viewModel.isDailyCheckInAvailable.set(false)
            }
        } else {
            viewModel.isDailyCheckInAvailable.set(false)
        }
    }

    private fun setDailyOnlineTime() {
        binding.seekTime.max = UserConstants.ONLINE_TIME_MAX
        binding.seekTime.isEnabled = false

        val onlineTime: Description? = getQuestionData(binding.txtDailyOnlineTimeTitle.text.toString(), 0)

        if (onlineTime != null) { // online time found
            viewModel.isDailyOnlineTimeAvailable.set(true)

            binding.txtDailyOnlineTimeDiamonds.text = StringBuilder().append(onlineTime.diamond)

            if (onlineTime.isClaimed) {
                val onlineTimeSpent = convertMinutesToHoursMinutes(UserConstants.ONLINE_TIME_MAX)

                binding.seekTime.progress = UserConstants.ONLINE_TIME_MAX
                binding.txtTotalTime.text = StringBuilder().append("${onlineTimeSpent}/${UserConstants.ONLINE_TIME_MAX}").append(" min")

                viewModel.dailyOnlineTimeSpent.set(onlineTimeSpent.toInt())
                viewModel.isDailyOnlineTimeEnabled.set(false)
            } else {
                val lastTime = getUserPrefs().dailyOnlineTime

                if (lastTime >= UserConstants.ONLINE_TIME_MAX) {
                    val onlineTimeSpent = convertMinutesToHoursMinutes(UserConstants.ONLINE_TIME_MAX)

                    binding.seekTime.progress = UserConstants.ONLINE_TIME_MAX
                    binding.txtTotalTime.text = StringBuilder().append("${onlineTimeSpent}/${UserConstants.ONLINE_TIME_MAX}").append(" min")

                    viewModel.dailyOnlineTimeSpent.set(onlineTimeSpent.toInt())
                    viewModel.isDailyOnlineTimeEnabled.set(true)
                } else {
                    val onlineTimeSpent = convertMinutesToHoursMinutes(lastTime)

                    binding.txtTotalTime.text =
                        StringBuilder().append("${convertMinutesToHoursMinutes(lastTime)}/${UserConstants.ONLINE_TIME_MAX}").append(" min")
                    binding.seekTime.progress = lastTime

                    viewModel.dailyOnlineTimeSpent.set(onlineTimeSpent.toInt())
                    viewModel.isDailyOnlineTimeEnabled.set(false)
                }
            }
        } else { // daily container not found
            viewModel.isDailyOnlineTimeAvailable.set(false)
        }
    }

    private fun getDiamondQuestions(isDiamondsUpdated: Boolean? = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getDiamondQuestion().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@DiamondActivity)
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@DiamondActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@DiamondActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.show(this@DiamondActivity)

                            Toast.makeText(this@DiamondActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                viewModel.questionData.set(it.data.questionData)

                                if (isDiamondsUpdated == true) {
                                    getUserPrefs().remainingDiamonds = it.data.questionData.diamonds
                                    EventManager.postEvent(Event(EventConstants.UPDATE_DIAMOND_COUNT, null))
                                }

                                // watch ads
                                setWatchAds()

                                // daily check in
                                setDailyCheckIn()

                                // daily online time
                                setDailyOnlineTime()

                                // rate app
                                setRateApp()

                                // invite friend
                                setInviteFriend()

                                // complete profile
                                setCompleteProfile()

                                // write about me
                                setAboutMe()

                                // upload photos
                                setUploadPhotos()

                                binding.executePendingBindings()
                            } else {
                                Toast.makeText(this@DiamondActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setRateApp() {
        val rateUs: Description? = getQuestionData(binding.txtRateAppTitle.text.toString(), 1)
        if (rateUs != null) {
            binding.txtRateAppDiamonds.text = StringBuilder().append(rateUs.diamond)
            viewModel.isRateAppAvailable.set(true)
            viewModel.isRateAppEnabled.set(!rateUs.isClaimed)
        } else {
            viewModel.isRateAppAvailable.set(false)
        }
    }

    private fun setInviteFriend() {
        val inviteFriend: Description? = getQuestionData(binding.txtInviteFriendTitle.text.toString(), 1)
        if (inviteFriend != null) {
            binding.txtInviteFriendDiamonds.text = StringBuilder().append(inviteFriend.diamond)

            viewModel.isInviteFriendAvailable.set(true)
            viewModel.isInviteFriendEnabled.set(!inviteFriend.isClaimed)
        } else {
            viewModel.isInviteFriendAvailable.set(false)
        }
    }

    private fun setCompleteProfile() {
        val completeProfile: Description? = getQuestionData(binding.txtCompleteProfileTitle.text.toString(), 2)
        if (completeProfile != null) {
            binding.txtCompleteProfileDiamonds.text = StringBuilder().append(completeProfile.diamond)

            viewModel.isCompleteProfileAvailable.set(true)
            viewModel.isCompleteProfileEnabled.set(!completeProfile.isClaimed)
        } else {
            viewModel.isCompleteProfileAvailable.set(false)
        }
    }

    private fun setAboutMe() {
        val writeAboutMe: Description? = getQuestionData(binding.txtAboutMeTitle.text.toString(), 2)
        if (writeAboutMe != null) {
            binding.txtAboutMeDiamonds.text = StringBuilder().append(writeAboutMe.diamond)

            viewModel.isWriteAboutMeAvailable.set(true)
            viewModel.isWriteAboutMeEnabled.set(!writeAboutMe.isClaimed)
        } else {
            binding.containerAboutMe.visibility = View.GONE
        }
    }

    private fun setUploadPhotos() {
        val uploadPhotos: Description? = getQuestionData(binding.txtUploadPhotosTitle.text.toString(), 2)

        if (uploadPhotos != null) {
            viewModel.isUploadPhotoAvailable.set(true)
            try {
                binding.txtUploadPhotosDiamonds.text = StringBuilder().append(uploadPhotos.diamond)

                val userImageArrayList = getUserPrefs().userImages!!

                binding.seekPhotos.progress = userImageArrayList.size
                binding.seekPhotos.max = IntroductionConstants.UPLOAD_PHOTO_LIMIT
                binding.txtTotalPhotos.text =
                    StringBuilder().append("${userImageArrayList.size}/${IntroductionConstants.UPLOAD_PHOTO_LIMIT}").append(" photos")

                viewModel.isUploadPhotoEnabled.set(!uploadPhotos.isClaimed)
            } catch (e: Exception) {
                viewModel.isUploadPhotoAvailable.set(false)
            }
        } else {
            viewModel.isUploadPhotoAvailable.set(false)
        }
    }

    private fun getQuestionData(
        titles: String,
        sectionId: Int,
    ): Description? {
        if (viewModel.questionData.get() == null) {
            return null
        }
        val questionData: QuestionData = viewModel.questionData.get()!!
        val questions: List<Question> = questionData.questionList
        val question: Question = questions[sectionId]
        val descriptions: List<Description> = question.descriptionList
        return descriptions.find { it.title.equals(titles, true) }
    }

    private fun manageDiamondResult(it: Resource<JsonObject?>, onSuccessListener: () -> Unit) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (it.status) {
                Status.LOADING -> {
                    LoadingDialog.show(this@DiamondActivity)
                }

                Status.SIGN_OUT -> {
                    Toast.makeText(this@DiamondActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ADMIN_BLOCKED -> {
                    Toast.makeText(this@DiamondActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ERROR -> {
                    LoadingDialog.hide()

                    Toast.makeText(this@DiamondActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                }

                Status.SUCCESS -> {
                    if (it.data != null) {
                        val rootJson = it.data
                        if (rootJson.has("data")) {

                            val code = if (rootJson.has("code")) rootJson.getAsJsonPrimitive("code").asInt else null

                            if ((code != null) && (code == 409)) {
                                Toast.makeText(
                                    getKoinContext(),
                                    if (rootJson.has("data")) rootJson.getAsJsonPrimitive("data").asString else "Diamond already claimed",
                                    Toast.LENGTH_SHORT,
                                ).show()

                                getDiamondQuestions(true)

                                onSuccessListener.invoke()
                            } else {
                                val dataJson = rootJson.getAsJsonObject("data")

                                if (dataJson.has("isClaim") && dataJson.has("diamonds")) {
                                    val isClaimed = dataJson.getAsJsonPrimitive("isClaim").asBoolean

                                    if (isClaimed) {
                                        getDiamondQuestions(true)

                                        onSuccessListener.invoke()
                                    } else {
                                        val message = rootJson.getAsJsonPrimitive("message").asString
                                        Toast.makeText(this@DiamondActivity, message ?: "Something went wrong, try again!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val message = rootJson.getAsJsonPrimitive("message").asString
                                    Toast.makeText(this@DiamondActivity, message ?: "Something went wrong, try again!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            val message = rootJson.getAsJsonPrimitive("message").asString
                            Toast.makeText(this@DiamondActivity, message ?: "Something went wrong, try again!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        LoadingDialog.hide()
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

                startActivity(Intent(this@DiamondActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        ManageAds.showSmallNativeAd(SmallNativeGroup.Diamond, binding.adSmallNative)

        if (viewModel.questionData.get() == null) {
            getDiamondQuestions()
        } else {
            // watch ads
            setWatchAds()

            // daily check in
            setDailyCheckIn()

            // daily online time
            setDailyOnlineTime()

            // rate app
            setRateApp()

            // invite friend
            setInviteFriend()

            // complete profile
            setCompleteProfile()

            // write about me
            setAboutMe()

            // upload photos
            setUploadPhotos()

            binding.executePendingBindings()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            ManageAds.showSmallNativeAd(SmallNativeGroup.Diamond, binding.adSmallNative)

            if (viewModel.questionData.get() == null) {
                getDiamondQuestions()
            } else {
                // watch ads
                setWatchAds()

                // daily check in
                setDailyCheckIn()

                // daily online time
                setDailyOnlineTime()

                // rate app
                setRateApp()

                // invite friend
                setInviteFriend()

                // complete profile
                setCompleteProfile()

                // write about me
                setAboutMe()

                // upload photos
                setUploadPhotos()

                binding.executePendingBindings()
            }
        }
    }

    override fun onDailyDiamondClaim() {
        val map = mapOf(
            "que_cat_id" to 1,
            "que_id" to 3,
            "is_daily" to true,
            "day" to getUserPrefs().checkInDay,
        ).toMutableMap<String, Any?>()

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.claimDiamond(map).collect {
                launch(Dispatchers.Main) {
                    manageDiamondResult(it) {
                        getUserPrefs().isDailyCheckInAvailable = false
                    }
                }
            }
        }
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.UPDATE_DIAMOND_COUNT -> {
                getDiamondQuestions()
            }
        }
    }
}
package com.belive.dating.activities.dashboard.fragments.home

import android.Manifest
import android.app.ActionBar
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.belive.dating.R
import com.belive.dating.activities.EventBusFragment
import com.belive.dating.activities.chat.ChatActivity
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.activities.diamond.DailyCheckInBottomDialog
import com.belive.dating.activities.paywalls.topups.boost.BoostPaywallActivity
import com.belive.dating.activities.paywalls.topups.like.LikePaywallActivity
import com.belive.dating.activities.paywalls.topups.rewind.RewindPaywallActivity
import com.belive.dating.activities.paywalls.topups.super_like.SuperLikePaywallActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.api.user.models.home_profiles.HomeUsersResponse
import com.belive.dating.api.user.models.home_profiles.User
import com.belive.dating.api.user.models.like_profile.LikeProfileResponse
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.FragmentHomeBinding
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.formatTimeForBoost
import com.belive.dating.extensions.getAdsPrefs
import com.belive.dating.extensions.getGlide
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gone
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.invisible
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.navGraphViewModel
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.swipeUp
import com.belive.dating.extensions.throttleFirstClick
import com.belive.dating.extensions.visible
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import com.belive.dating.helpers.helper_views.card_swiper.CardStackLayoutManager
import com.belive.dating.helpers.helper_views.card_swiper.CardStackListener
import com.belive.dating.helpers.helper_views.card_swiper.CardStackSmoothScroller
import com.belive.dating.helpers.helper_views.card_swiper.Direction
import com.belive.dating.helpers.helper_views.card_swiper.Duration
import com.belive.dating.helpers.helper_views.card_swiper.RewindAnimationSetting
import com.belive.dating.helpers.helper_views.card_swiper.ScrollType
import com.belive.dating.helpers.helper_views.card_swiper.StackFrom
import com.belive.dating.helpers.helper_views.card_swiper.SwipeAnimationSetting
import com.belive.dating.helpers.helper_views.card_swiper.SwipeableMethod
import com.belive.dating.helpers.helper_views.circle_timer.CircularTimerView
import com.belive.dating.services.BoostService
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil

class HomeFragment : EventBusFragment(), CardStackListener, UserCardAdapter.UserCardClickListener, DailyCheckInBottomDialog.OnClaimListener {

    val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    val viewModel: HomeViewModel by navGraphViewModel(R.id.navigation_home)

    private var manager: CardStackLayoutManager? = null
    private var adapter: UserCardAdapter? = null

    private var boostDialog: Dialog? = null
    private var progressCircle: CircularTimerView? = null
    private var txtStartTime: TextView? = null
    private var txtEndTime: TextView? = null
    private var txtTitle: TextView? = null
    private var imgClose: ImageButton? = null

    private var rewindProfileList: ArrayList<Int> = arrayListOf()

    private var state = false
    private var direct = false
    private var rewinding = false
    private var isAlreadyActioned = false

    private var likeMode = 0

    private var isCheckInDisplayed = false
    private val dailyCheckInDiamondBottomSheet by lazy {
        DailyCheckInBottomDialog(
            requireContext(), viewModel.questionData.get()!!.dailyCheckInReward, this
        )
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val millisUntilFinished = intent?.getLongExtra("time_left", 0)
            val state = intent?.getBooleanExtra("finish", false)
            updateUI(millisUntilFinished!!, state!!)
        }
    }

    fun updateUI(millisUntilFinished: Long, state: Boolean) {
        viewModel.boostProgress.set(millisUntilFinished)
        viewModel.isBoostEnabled.set(state)

        if (!state) {
            if (boostDialog?.isShowing == false) {
                boostDialog = null
                progressCircle = null
                txtTitle = null
                imgClose = null
            } else {
                boostDialog?.dismiss()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.updateState()
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        viewModel.getState()
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onDestroy() {
        try {
            requireContext().unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            catchLog("--unregister-- ${e.printStackTrace()}")
        }
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel.getState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logger("--fragment--", "onViewCreated: HomeFragment")

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initViews()

        clickListeners()

        listenEvents()

        observeData()

        if (((requireActivity() as MainActivity).viewModel.isInitLoading.get() == false) && (viewModel.isDataLoaded.get() == false)) {
            gettingProfilesForFirstTime()
        }
    }

    private fun gettingProfilesForFirstTime() {
        if (getUserPrefs().activePackage != "Lifetime") {
            if (!isCheckInDisplayed && getUserPrefs().isDailyCheckInAvailable) {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getDiamondQuestion().collectLatest {
                        launch(Dispatchers.Main) {
                            when (it.status) {
                                Status.LOADING -> {

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
                                    Toast.makeText(requireContext(), it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                }

                                Status.SUCCESS -> {
                                    viewModel.questionData.set(it.data?.questionData)

                                    dailyCheckInDiamondBottomSheet.show()
                                    isCheckInDisplayed = true
                                }
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            launch {
                viewModel.getProfileBalance().collectLatest {
                    when (it.status) {
                        Status.LOADING -> {

                        }

                        Status.SIGN_OUT -> {
                            launch(Dispatchers.Main) {
                                Toast.makeText(getKoinContext(), "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                                authOut()
                            }
                        }

                        Status.ADMIN_BLOCKED -> {
                            launch(Dispatchers.Main) {
                                Toast.makeText(getKoinContext(), "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                                authOut()
                            }
                        }

                        Status.ERROR -> {
                            launch(Dispatchers.Main) {
                                Toast.makeText(requireContext(), it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        Status.SUCCESS -> {
                            if (it.data?.profileBalance != null) {
                                getUserPrefs().remainingRewinds = it.data.profileBalance.rewinds
                                getUserPrefs().remainingLikes = it.data.profileBalance.likes
                                getUserPrefs().remainingSuperLikes = it.data.profileBalance.superLikes
                                getUserPrefs().remainingDiamonds = it.data.profileBalance.diamonds
                                getUserPrefs().remainingBoosts = it.data.profileBalance.boosts

                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_BALANCE, null))
                            }
                        }
                    }
                }
            }

            if (viewModel.isLoadingProfiles.get() == false) {
                getUserPrefs().countryCode?.let { countryCode ->
                    launch {
                        viewModel.isLoadingProfiles.set(true)
                        viewModel.getHomeProfiles(countryCode).collectLatest {
                            appendProfiles(it)
                        }
                    }
                }
            }
        }
    }

    private fun clickListeners() {
        binding.btnFilter.setOnClickListener {
            (requireActivity() as MainActivity).openFilters()
        }

        binding.btnRewind.throttleFirstClick {
            if ((viewModel.homeUserList.get()?.size ?: 0) > 0) {
                if ((manager!!.getTopPosition() > 0) && (manager!!.getTopPosition() <= adapter!!.getAdapterPosition(viewModel.homeUserList.get()!!.size - 1))) {
                    if (adapter!!.isAdPosition(manager!!.getTopPosition())) {
                        rewinding = true

                        val setting = RewindAnimationSetting.Builder().setDirection(Direction.Bottom).setDuration(Duration.Normal.duration)
                            .setInterpolator(DecelerateInterpolator()).build()
                        manager!!.setRewindAnimationSetting(setting)
                        binding.cardStackView.rewind()
                    } else {
                        if (getUserPrefs().isUnlimitedRewinds) {
                            deductRewind()
                        } else {
                            if (getUserPrefs().remainingRewinds > 0) {
                                deductRewind()
                            } else {
                                startActivity(Intent(requireActivity(), RewindPaywallActivity::class.java))
                                requireActivity().swipeUp()
                            }
                        }
                    }
                }
            }
        }

        binding.btnSkip.throttleFirstClick {
            lifecycleScope.launch {
                viewModel.dragState.emit(Pair(viewModel.topPosition.value, Direction.Left))

                binding.btnSkip.postDelayed({
                    state = true
                    val setting = SwipeAnimationSetting.Builder().setDirection(Direction.Left).setDuration(Duration.Normal.duration)
                        .setInterpolator(AccelerateInterpolator()).build()
                    manager!!.setSwipeAnimationSetting(setting)
                    binding.cardStackView.swipe()
                }, 500)
            }
        }

        binding.btnSuperLike.throttleFirstClick {
            lifecycleScope.launch {
                viewModel.dragState.emit(Pair(viewModel.topPosition.value, Direction.Top))

                binding.btnSuperLike.postDelayed({
                    state = true
                    val setting = SwipeAnimationSetting.Builder().setDirection(Direction.Top).setDuration(Duration.Normal.duration)
                        .setInterpolator(AccelerateInterpolator()).build()
                    manager!!.setSwipeAnimationSetting(setting)
                    binding.cardStackView.swipe()
                }, 500)
            }
        }

        binding.btnLike.throttleFirstClick {
            lifecycleScope.launch {
                viewModel.dragState.emit(Pair(viewModel.topPosition.value, Direction.Right))

                binding.btnLike.postDelayed({
                    state = true
                    val setting = SwipeAnimationSetting.Builder().setDirection(Direction.Right).setDuration(Duration.Normal.duration)
                        .setInterpolator(AccelerateInterpolator()).build()
                    manager!!.setSwipeAnimationSetting(setting)
                    binding.cardStackView.swipe()
                }, 500)
            }
        }

        binding.progressCircular.throttleFirstClick {
            if (boostDialog != null) {
                boostDialog?.show()
            } else {
                showBoostDialog()
            }
        }

        binding.btnBoost.throttleFirstClick {
            if (boostDialog != null) {
                boostDialog?.show()
            } else {
                if ((requireActivity() as MainActivity).isNetworkAvailable) {
                    if (getUserPrefs().remainingBoosts == 0) {
                        startActivity(Intent(requireActivity(), BoostPaywallActivity::class.java))
                        requireActivity().swipeUp()
                    } else {
                        checkAndRequestPermissions()
                    }
                } else {
                    Toast.makeText(requireContext(), "Internet Connection required to Boost your Profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val permission = Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 123)
            } else {
                deductBoost()
            }
        } else {
            deductBoost()
        }
    }

    private fun deductBoost() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deductBoost().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(getKoinActivity())
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

                            Toast.makeText(requireContext(), it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            val rootJson = it.data

                            if (rootJson != null) {
                                if (rootJson.has("data")) {

                                    val data = rootJson.getAsJsonObject("data")

                                    if (data.has("is_boost")) {
                                        if (data.getAsJsonPrimitive("is_boost").asBoolean) {
                                            if (data.has("boosts")) {
                                                val remainingBoosts = data.getAsJsonPrimitive("boosts").asInt
                                                getUserPrefs().remainingBoosts = remainingBoosts

                                                EventManager.postEvent(Event(EventConstants.UPDATE_BOOST_COUNT, null))
                                            }
                                            if (data.has("boost_min")) {
                                                getUserPrefs().boostTime = data.getAsJsonPrimitive("boost_min").asFloat * 1000 * 60
                                            } else {
                                                getUserPrefs().boostTime = (30 * 1000 * 60).toFloat()
                                            }
                                            startBoostService()
                                        } else {
                                            startActivity(Intent(requireActivity(), BoostPaywallActivity::class.java))
                                            requireActivity().swipeUp()
                                        }
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startBoostService() {
        val serviceIntent = Intent(requireContext(), BoostService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent)
        } else {
            requireContext().startService(serviceIntent)
        }

        showBoostDialog()
    }

    private fun showBoostDialog() {
        boostDialog = Dialog(requireContext())
        boostDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        boostDialog?.setCancelable(false)
        boostDialog?.setContentView(R.layout.dialog_boost)
        boostDialog?.setCanceledOnTouchOutside(true)
        boostDialog?.window?.setDimAmount(0.75f)
        boostDialog?.window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        boostDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        progressCircle = boostDialog?.findViewById(R.id.progress_circular)
        progressCircle?.setMaxValue(getUserPrefs().boostTime / 1000)
        val imgGif = boostDialog?.findViewById<ImageView>(R.id.animation_view)
        txtEndTime = boostDialog?.findViewById(R.id.txtEndTimeAt)
        txtStartTime = boostDialog?.findViewById(R.id.txtBoost)
        txtTitle = boostDialog?.findViewById(R.id.txtTitle)
        imgClose = boostDialog?.findViewById(R.id.btn_close)
        imgClose?.setOnClickListener {
            boostDialog?.dismiss()
        }
        boostDialog?.setOnDismissListener {
            if (viewModel.isBoostEnabled.get() == false) {
                boostDialog = null
                progressCircle = null
                txtTitle = null
                imgClose = null
            }
        }

        getGlide().load(R.drawable.boost_gif).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(object : DrawableImageViewTarget(imgGif) {
            override fun setResource(resource: Drawable?) {
                if (resource is GifDrawable) {
                    resource.setLoopCount(GifDrawable.LOOP_FOREVER) // Infinite Loop
                }
                super.setResource(resource)
            }
        })

        boostDialog?.show()
    }

    private fun initViews() {
        binding.isSkeleton = true
        binding.executePendingBindings()

        binding.btnRewind.setImageResource(R.drawable.enable_rewind)
        binding.btnRewind.isEnabled = false
        binding.btnSkip.setImageResource(R.drawable.enable_skip)
        binding.btnSkip.isEnabled = false
        binding.btnLike.setImageResource(R.drawable.enable_like)
        binding.btnLike.isEnabled = false
        binding.btnSuperLike.setImageResource(R.drawable.enable_super_like)
        binding.btnSuperLike.isEnabled = false
        binding.btnBoost.setImageResource(R.drawable.enable_boost)
        binding.btnBoost.isEnabled = false
    }

    private fun initCardStackView() {
        manager = CardStackLayoutManager(requireContext(), this)
        manager?.setStackFrom(StackFrom.None)
        manager?.setVisibleCount(2)
        manager?.setTranslationInterval(8.0f)
        manager?.setScaleInterval(0.95f)
        manager?.setSwipeThreshold(0.3f)
        manager?.setMaxDegree(20.0f)
        manager?.setDirections(Direction.HORIZONTAL)
        manager?.setCanScrollHorizontal(true)
        manager?.setCanScrollVertical(true)
        manager?.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager?.setOverlayInterpolator(LinearInterpolator())

        binding.cardStackView.layoutManager = manager
        binding.cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
        binding.cardStackView.recycledViewPool.setMaxRecycledViews(0, 6)

        cardSnapHelper().attachToRecyclerView(binding.cardStackView)
    }

    private fun observeData() {

        viewModel.boostProgress.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                viewModel.boostProgress.get()?.let {
                    val timeFormatted: String = formatTimeForBoost(it)

                    binding.progressCircular.setProgress(ceil((it / 1000f).toDouble()).toFloat())
                    binding.progressCircular.setText(timeFormatted)

                    progressCircle?.setProgress(ceil((it / 1000f).toDouble()).toFloat())
                    progressCircle?.setText(timeFormatted)

                    txtStartTime?.text = getUserPrefs().boostStartTime
                    txtEndTime?.text = getUserPrefs().boostEndTime
                }
            }
        })

        viewModel.homeUserList.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                // display data

                logger("--home_profiles--", "addOnPropertyChangedCallback: " + viewModel.homeUserList.get()?.size)

                lifecycleScope.launch(Dispatchers.Main) {
                    if (viewModel.homeUserList.get() == null) {
                        // error OR sign out OR loading data

                        binding.layComData.gone()
                        binding.buttonContainer.visible()
                        binding.cardStackView.visible()

                        binding.isSkeleton = true
                        binding.executePendingBindings()
                    } else {
                        // success

                        if ((adapter == null) || (manager == null)) {
                            binding.isSkeleton = false
                            binding.executePendingBindings()

                            binding.btnRewind.setImageResource(R.drawable.rewind)
                            binding.btnRewind.isEnabled = true
                            binding.btnSkip.setImageResource(R.drawable.skip)
                            binding.btnSkip.isEnabled = true
                            binding.btnLike.setImageResource(R.drawable.like)
                            binding.btnLike.isEnabled = true
                            binding.btnSuperLike.setImageResource(R.drawable.super_like)
                            binding.btnSuperLike.isEnabled = true
                            binding.btnBoost.setImageResource(R.drawable.boost)
                            binding.btnBoost.isEnabled = true

                            if (viewModel.homeUserList.get().isNullOrEmpty()) {
                                // no data found for user to display
                                binding.layComData.visible()
                                binding.buttonContainer.gone()
                                binding.cardStackView.gone()
                            } else {
                                // profile list available to display
                                initCardStackView()

                                adapter = UserCardAdapter(requireActivity(), viewModel, this@HomeFragment)
                                binding.cardStackView.adapter = adapter
                            }
                        } else {
                            if (manager!!.getTopPosition() == adapter?.itemCount) {
                                binding.layComData.visible()
                                binding.buttonContainer.gone()
                                binding.cardStackView.gone()
                            } else {
                                binding.layComData.gone()
                                binding.buttonContainer.visible()
                                binding.cardStackView.visible()
                            }
                        }

                        setBoostBroadcast()
                    }
                }
            }
        })
    }

    private fun setBoostBroadcast() {
        val intentFilter = IntentFilter("countdown_tick")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                broadcastReceiver,
                intentFilter,
                AppCompatActivity.RECEIVER_EXPORTED,
            )
        } else {
            ContextCompat.registerReceiver(requireContext(), broadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
    }

    private fun appendProfiles(data: Resource<HomeUsersResponse?>) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (data.status) {
                Status.LOADING -> {

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
                    Toast.makeText(requireContext(), data.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                }

                Status.SUCCESS -> {
                    viewModel.isDataLoaded.set(true)
                    viewModel.isLoadingProfiles.set(false)

                    logger("--pagination--", "new: ${gsonString(data.data?.userList?.map { it.id }?.toList()?.joinToString(",") { it.toString() })}")

                    val list = ArrayList(viewModel.homeUserList.get() ?: arrayListOf())
                    val previousSize = list.size
                    /*data.data?.userList?.let { users ->
                        users.forEach { user ->
                            if (list.none { it.id == user.id }) {
                                list.add(user)
                            }
                        }
                    }*/
                    data.data?.userList?.let { list.addAll(it) }
                    viewModel.homeUserList.set(list)

                    adapter?.notifyItemRangeInserted(previousSize - 1, viewModel.homeUserList.get()?.size ?: 0)
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

    override fun onCardDragging(direction: Direction, ratio: Float) {
        logger("----on_card----", "onCardDragging ------ Direction: $direction, ratio: $ratio")

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.dragState.emit(Pair(viewModel.topPosition.value, direction))
        }

        if (!state) {
            if (direction == Direction.Top) {
                binding.btnRewind.invisible()
                binding.btnSkip.invisible()
                binding.btnLike.invisible()
                binding.layBoost.invisible()
                binding.btnSuperLike.visible()

                if ((likeMode == 3) || (likeMode == 2)) {
                    binding.btnSuperLike.setImageResource(R.drawable.enable_super_like)
                } else {
                    binding.btnSuperLike.setImageResource(R.drawable.super_like)

                    binding.btnSuperLike.isPressed = true
                }
            } else if (direction == Direction.Left) {
                binding.btnSkip.isPressed = true

                binding.btnSkip.visible()
                binding.btnLike.invisible()
                binding.btnRewind.invisible()
                binding.layBoost.invisible()
                binding.btnSuperLike.invisible()
            } else if (direction == Direction.Right) {
                if (likeMode == 3 || likeMode == 1) {
                    binding.btnLike.setImageResource(R.drawable.enable_like)
                } else {
                    binding.btnLike.setImageResource(R.drawable.like)

                    binding.btnLike.isPressed = true
                }

                binding.btnSkip.invisible()
                binding.btnLike.visible()
                binding.btnRewind.invisible()
                binding.layBoost.invisible()
                binding.btnSuperLike.invisible()
            } else {
                direct = true
                if (likeMode == 3 || likeMode == 1) {
                    binding.btnLike.setImageResource(R.drawable.enable_like)
                } else {
                    binding.btnLike.setImageResource(R.drawable.like)

                    binding.btnLike.isPressed = false
                }
                if (likeMode == 3 || likeMode == 2) {
                    binding.btnSuperLike.setImageResource(R.drawable.enable_super_like)
                } else {
                    binding.btnSuperLike.setImageResource(R.drawable.super_like)

                    binding.btnSuperLike.isPressed = false
                }

                binding.btnSkip.isPressed = false

                binding.btnSkip.visible()
                binding.btnLike.visible()
                binding.btnRewind.visible()
                binding.btnSuperLike.visible()
                binding.layBoost.visible()
            }
        }
    }

    override fun onCardSwiped(direction: Direction) {
        logger("----on_card----", "onCardSwiped ------- Direction: $direction")

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.dragState.emit(Pair(-1, null))
        }

        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        if (likeMode == 3 || likeMode == 1) {
            binding.btnLike.setImageResource(R.drawable.enable_like)
        } else {
            binding.btnLike.setImageResource(R.drawable.like)
            binding.btnLike.isPressed = false
        }

        if (likeMode == 3 || likeMode == 2) {
            binding.btnSuperLike.setImageResource(R.drawable.enable_super_like)
        } else {
            binding.btnSuperLike.isPressed = false
        }

        binding.btnSkip.isPressed = false

        if (state) {
            state = false
        } else {
            binding.btnSkip.visibility = View.VISIBLE
            binding.btnRewind.visibility = View.VISIBLE
            binding.layBoost.visibility = View.VISIBLE
            binding.btnSuperLike.visibility = View.VISIBLE
            binding.btnLike.visibility = View.VISIBLE

            when (direction) {
                Direction.Top -> {
                    binding.btnLike.startAnimation(animation)
                    binding.btnSkip.startAnimation(animation)
                    binding.btnRewind.startAnimation(animation)
                    binding.layBoost.startAnimation(animation)
                }

                Direction.Left -> {
                    binding.btnRewind.startAnimation(animation)
                    binding.btnLike.startAnimation(animation)
                    binding.layBoost.startAnimation(animation)
                    binding.btnSuperLike.startAnimation(animation)
                }

                Direction.Right -> {
                    binding.btnRewind.startAnimation(animation)
                    binding.btnSkip.startAnimation(animation)
                    binding.btnSuperLike.startAnimation(animation)
                    binding.layBoost.startAnimation(animation)
                }

                else -> {
                    direct = true
                }
            }
        }

        if (isAlreadyActioned) {
            isAlreadyActioned = false
        } else {
            if (!adapter!!.isAdPosition(manager!!.getTopPosition())) {
                when (direction) {
                    Direction.Left -> {
                        skipProfile()
                    }

                    Direction.Right -> {
                        likeProfile()
                    }

                    Direction.Top -> {
                        superLikeProfile()
                    }

                    Direction.Bottom -> {}
                }
            }
        }
    }

    private fun deductRewind() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deductRewind(
                viewModel.homeUserList.get()!![adapter!!.getContentPosition(manager!!.getTopPosition() - 1)].id,
                viewModel.homeUserList.get()!![adapter!!.getContentPosition(manager!!.getTopPosition() - 1)].deductFrom,
            ).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(getKoinActivity())
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

                            Toast.makeText(requireContext(), it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                if (it.data.rewind.isRewind) {
                                    getUserPrefs().remainingRewinds = it.data.rewind.totalRewind
                                    getUserPrefs().remainingLikes = it.data.rewind.totalLike
                                    getUserPrefs().remainingSuperLikes = it.data.rewind.totalSuperLike

                                    EventManager.postEvent(Event(EventConstants.UPDATE_REWIND_COUNT, null))
                                    EventManager.postEvent(Event(EventConstants.UPDATE_LIKE_COUNT, null))
                                    EventManager.postEvent(Event(EventConstants.UPDATE_SUPER_LIKE_COUNT, null))

                                    viewModel.homeUserList.get()?.find { data ->
                                        data.id == viewModel.homeUserList.get()?.get(adapter!!.getContentPosition(manager!!.getTopPosition() - 1))?.id
                                    }?.apply { deductFrom = null }

                                    rewinding = true

                                    val setting =
                                        RewindAnimationSetting.Builder().setDirection(Direction.Bottom).setDuration(Duration.Normal.duration)
                                            .setInterpolator(DecelerateInterpolator()).build()
                                    manager!!.setRewindAnimationSetting(setting)
                                    binding.cardStackView.rewind()
                                } else {
                                    startActivity(Intent(requireActivity(), RewindPaywallActivity::class.java))
                                    requireActivity().swipeUp()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun skipProfile() {
        viewModel.homeUserList.get()?.get(adapter!!.getContentPosition(manager!!.getTopPosition() - 1))?.id?.let {
            viewModel.skipProfile(it)

            if (manager!!.getTopPosition() == adapter?.itemCount) {
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.layComData.visible()
                    binding.buttonContainer.gone()
                    binding.cardStackView.gone()
                }
            }
        }
    }

    private fun superLikeProfile() {
        viewModel.homeUserList.get()?.get(adapter!!.getContentPosition(manager!!.getTopPosition() - 1))?.id?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.likeProfile(it, 2).collectLatest {
                    manageSuperLikeResponse(it)
                }
            }
        }
    }

    private fun manageSuperLikeResponse(resource: Resource<LikeProfileResponse?>) {
        when (resource.status) {
            Status.LOADING -> {

            }

            Status.SIGN_OUT -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(getKoinContext(), "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }
            }

            Status.ADMIN_BLOCKED -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(getKoinContext(), "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                    authOut()
                }
            }

            Status.ERROR -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), resource.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()

                    binding.cardStackView.post {
                        rewinding = true

                        val setting = RewindAnimationSetting.Builder().setDirection(Direction.Top).setDuration(Duration.Normal.duration)
                            .setInterpolator(DecelerateInterpolator()).build()
                        manager!!.setRewindAnimationSetting(setting)
                        binding.cardStackView.rewind()
                    }
                }
            }

            Status.SUCCESS -> {
                if (resource.data?.likeProfile?.isLike == true) {
                    getUserPrefs().remainingLikes = resource.data.likeProfile.totalLike
                    getUserPrefs().remainingSuperLikes = resource.data.likeProfile.totalSuperLike

                    EventManager.postEvent(Event(EventConstants.UPDATE_SUPER_LIKE_COUNT, null))

                    viewModel.homeUserList.get()?.find { data ->
                        data.id == viewModel.homeUserList.get()?.get(manager!!.getTopPosition() - 1)?.id
                    }?.apply { deductFrom = resource.data.likeProfile.deductFrom }

                    if (manager!!.getTopPosition() == adapter?.itemCount) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            binding.layComData.visible()
                            binding.buttonContainer.gone()
                            binding.cardStackView.gone()
                        }
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.cardStackView.post {
                            rewinding = true

                            val setting = RewindAnimationSetting.Builder().setDirection(Direction.Top).setDuration(Duration.Normal.duration)
                                .setInterpolator(DecelerateInterpolator()).build()
                            manager!!.setRewindAnimationSetting(setting)
                            binding.cardStackView.rewind()

                            startActivity(Intent(getKoinActivity(), SuperLikePaywallActivity::class.java))
                            getKoinActivity().swipeUp()
                        }
                    }
                }
            }
        }
    }

    private fun likeProfile() {
        viewModel.homeUserList.get()?.get(adapter!!.getContentPosition(manager!!.getTopPosition() - 1))?.id?.let { it ->
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.likeProfile(it, 1).collectLatest {
                    manageLikeResponse(it)
                }
            }
        }
    }

    private fun manageLikeResponse(resource: Resource<LikeProfileResponse?>) {
        when (resource.status) {
            Status.LOADING -> {

            }

            Status.SIGN_OUT -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(getKoinContext(), "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }
            }

            Status.ADMIN_BLOCKED -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(getKoinContext(), "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                    authOut()
                }
            }

            Status.ERROR -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), resource.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()

                    binding.cardStackView.post {
                        rewinding = true

                        val setting = RewindAnimationSetting.Builder().setDirection(Direction.Right).setDuration(Duration.Normal.duration)
                            .setInterpolator(DecelerateInterpolator()).build()
                        manager!!.setRewindAnimationSetting(setting)
                        binding.cardStackView.rewind()
                    }
                }
            }

            Status.SUCCESS -> {
                if (resource.data?.likeProfile?.isLike == true) {
                    getUserPrefs().remainingLikes = resource.data.likeProfile.totalLike
                    getUserPrefs().remainingSuperLikes = resource.data.likeProfile.totalSuperLike

                    EventManager.postEvent(Event(EventConstants.UPDATE_LIKE_COUNT, null))

                    viewModel.homeUserList.get()?.find { data ->
                        data.id == viewModel.homeUserList.get()?.get(adapter!!.getContentPosition(manager!!.getTopPosition() - 1))?.id
                    }?.apply { deductFrom = resource.data.likeProfile.deductFrom }

                    if (manager!!.getTopPosition() == adapter?.itemCount) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            binding.layComData.visible()
                            binding.buttonContainer.gone()
                            binding.cardStackView.gone()
                        }
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.cardStackView.post {
                            rewinding = true

                            val setting = RewindAnimationSetting.Builder().setDirection(Direction.Right).setDuration(Duration.Normal.duration)
                                .setInterpolator(DecelerateInterpolator()).build()
                            manager!!.setRewindAnimationSetting(setting)
                            binding.cardStackView.rewind()

                            startActivity(Intent(requireActivity(), LikePaywallActivity::class.java))
                            requireActivity().swipeUp()
                        }
                    }
                }
            }
        }
    }

    override fun onCardRewound() {
        logger("----on_card----", "onCardRewound")
    }

    override fun onCardCanceled() {
        logger("----on_card----", "onCardCanceled")

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.dragState.emit(Pair(-1, null))
        }

        state = false
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        binding.btnSkip.visible()
        binding.btnLike.visible()
        binding.btnRewind.visible()
        binding.layBoost.visible()
        binding.btnSuperLike.visible()

        binding.btnSkip.isPressed = false

        if (likeMode == 3 || likeMode == 1) {
            binding.btnLike.setImageResource(R.drawable.enable_like)
        } else {
            binding.btnLike.setImageResource(R.drawable.like)
            binding.btnLike.isPressed = false
        }
        if (((manager!!.getTopPosition() > 0) &&
                    ManageAds.isHomeCardAdsEnabled() &&
                    ((manager!!.getTopPosition() + 1) % getAdsPrefs().swipeCardsAdInterval) == 0) ||
            likeMode == 3 ||
            likeMode == 2
        ) {
            binding.btnSuperLike.setImageResource(R.drawable.enable_super_like)
        } else {
            binding.btnSuperLike.setImageResource(R.drawable.super_like)
            binding.btnSuperLike.isPressed = false
        }

        if (direct) {
            direct = false
        } else {
            binding.btnRewind.startAnimation(animation)
            binding.btnSkip.startAnimation(animation)
            binding.btnSuperLike.startAnimation(animation)
            binding.btnBoost.startAnimation(animation)
            binding.btnLike.startAnimation(animation)
        }
    }

    override fun onCardAppeared(view: View, position: Int) {
        logger("----on_card----", "onCardAppeared ------- position: $position")

        lifecycleScope.launch {
            viewModel.topPosition.emit(position)
        }

        try {
            if (position == 0) {
                binding.btnRewind.setImageResource(R.drawable.enable_rewind)
                binding.btnRewind.isEnabled = false
            } else {
                binding.btnRewind.setImageResource(R.drawable.rewind)
                binding.btnRewind.isEnabled = true
            }

            if (ManageAds.isHomeCardAdsEnabled() &&
                ((position > 0 && ((position + 1) % getAdsPrefs().swipeCardsAdInterval) == 0) == true)
            ) {
                binding.btnSuperLike.setImageResource(R.drawable.enable_super_like)
                binding.btnSuperLike.isEnabled = false
                binding.btnBoost.setImageResource(R.drawable.enable_boost)
                binding.btnBoost.isEnabled = false
            } else {
                if (rewinding) {
                    rewinding = false
                    binding.btnLike.setImageResource(R.drawable.like)
                    binding.btnSuperLike.setImageResource(R.drawable.super_like)
                }

                if (position != 0) {
                    val addId = rewindProfileList.any { it == viewModel.homeUserList.get()?.get(position - 1)?.id }

                    if (!addId) {
                        viewModel.homeUserList.get()?.get(position - 1)?.id?.let { rewindProfileList.add(it) }
                    }
                }

                val removeId = rewindProfileList.any { it == viewModel.homeUserList.get()?.get(position - 1)?.id }

                if (removeId) {
                    rewindProfileList.remove(viewModel.homeUserList.get()?.get(position - 1)?.id)
                }

                likeMode = 0

                binding.btnLike.setImageResource(R.drawable.like)
                binding.btnLike.isEnabled = true

                binding.btnSuperLike.setImageResource(R.drawable.super_like)
                binding.btnSuperLike.isEnabled = true
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Something went wrong...!", Toast.LENGTH_SHORT).show()

            logger("--catch--", "profileIdList: ${e.printStackTrace()}")
        }
    }

    override fun onCardDisappeared(view: View, position: Int) {
        try {
            logger("----on_card----", "onCardDisappeared ------- position: $position")

            val lst = (viewModel.homeUserList.get()?.size ?: (0 - 1)) - 10

            if ((position >= lst) && (viewModel.isLoadingProfiles.get() == false)) {
                val remainingList = mutableListOf<Int>()

                for (index in lst until viewModel.homeUserList.get()?.size!!) {
                    if (viewModel.homeUserList.get()?.size!! > index) {
                        remainingList.add(viewModel.homeUserList.get()!![index].id)
                    }
                }

                logger("--pagination--", "pagination position: $position")
                logger("--pagination--", "all ids: ${viewModel.homeUserList.get()?.map { it.id }?.toList()?.joinToString(",") { it.toString() }}")
                logger("--pagination--", "remaining ids: ${gsonString(remainingList)}")
                logger("--pagination--", "total size: ${viewModel.homeUserList.get()?.size}")

                getUserPrefs().countryCode?.let { countryCode ->
                    viewModel.isLoadingProfiles.set(true)
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.getHomeProfiles(countryCode, remainingList).collectLatest {
                            appendProfiles(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            catchLog("onCardDisappeared: ${gsonString(e)}")

            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun cardSnapHelper() = (object : SnapHelper() {

        private var velocityX = 0
        private var velocityY = 0

        override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {

            if (layoutManager is CardStackLayoutManager) {
                if (layoutManager.findViewByPosition(layoutManager.getTopPosition()) != null) {
                    val x = targetView.translationX.toInt()
                    val y = targetView.translationY.toInt()

                    if (x != 0 || y != 0) {
                        val setting = layoutManager.getCardStackSetting()
                        val horizontal = abs(x) / targetView.width.toFloat()
                        val vertical = abs(y) / targetView.height.toFloat()
                        val duration = Duration.fromVelocity(if (velocityY < velocityX) velocityX else velocityY)

                        if (duration == Duration.Fast || setting.swipeThreshold < horizontal || setting.swipeThreshold < vertical) {
                            val state = layoutManager.getCardStackState()

                            if (state.getDirection() == Direction.Top) {
                                if (likeMode == 3 || likeMode == 2) {
                                    val scroller = CardStackSmoothScroller(ScrollType.ManualCancel, layoutManager)
                                    scroller.targetPosition = layoutManager.getTopPosition()
                                    layoutManager.startSmoothScroll(scroller)
                                } else {
                                    state.targetPosition = state.topPosition + 1

                                    val swipeAnimationSetting =
                                        SwipeAnimationSetting.Builder().setDirection(setting.swipeAnimationSetting.getDirection())
                                            .setDuration(duration.duration).setInterpolator(setting.swipeAnimationSetting.getInterpolator()).build()
                                    layoutManager.setSwipeAnimationSetting(swipeAnimationSetting)

                                    velocityX = 0
                                    velocityY = 0

                                    val scroller = CardStackSmoothScroller(ScrollType.ManualSwipe, layoutManager)
                                    scroller.targetPosition = layoutManager.getTopPosition()
                                    layoutManager.startSmoothScroll(scroller)

                                    // Super Like Profile
                                }
                            } else if (state.getDirection() == Direction.Right) {
                                if (likeMode == 3 || likeMode == 1) {
                                    val scroller = CardStackSmoothScroller(ScrollType.ManualCancel, layoutManager)
                                    scroller.targetPosition = layoutManager.getTopPosition()
                                    layoutManager.startSmoothScroll(scroller)
                                } else {
                                    state.targetPosition = state.topPosition + 1

                                    val swipeAnimationSetting =
                                        SwipeAnimationSetting.Builder().setDirection(setting.swipeAnimationSetting.getDirection())
                                            .setDuration(duration.duration).setInterpolator(setting.swipeAnimationSetting.getInterpolator()).build()
                                    layoutManager.setSwipeAnimationSetting(swipeAnimationSetting)

                                    velocityX = 0
                                    velocityY = 0

                                    val scroller = CardStackSmoothScroller(ScrollType.ManualSwipe, layoutManager)
                                    scroller.targetPosition = layoutManager.getTopPosition()
                                    layoutManager.startSmoothScroll(scroller)

                                    // Like Profile
                                }
                            } else {
                                if (setting.directions.contains(state.getDirection())) {
                                    state.targetPosition = state.topPosition + 1

                                    val swipeAnimationSetting =
                                        SwipeAnimationSetting.Builder().setDirection(setting.swipeAnimationSetting.getDirection())
                                            .setDuration(duration.duration).setInterpolator(setting.swipeAnimationSetting.getInterpolator()).build()
                                    layoutManager.setSwipeAnimationSetting(swipeAnimationSetting)

                                    velocityX = 0
                                    velocityY = 0

                                    val scroller = CardStackSmoothScroller(ScrollType.ManualSwipe, layoutManager)
                                    scroller.targetPosition = layoutManager.getTopPosition()
                                    layoutManager.startSmoothScroll(scroller)
                                } else {
                                    val scroller = CardStackSmoothScroller(ScrollType.ManualCancel, layoutManager)
                                    scroller.targetPosition = layoutManager.getTopPosition()
                                    layoutManager.startSmoothScroll(scroller)
                                }
                            }
                        } else {
                            val scroller = CardStackSmoothScroller(ScrollType.ManualCancel, layoutManager)
                            scroller.targetPosition = layoutManager.getTopPosition()
                            layoutManager.startSmoothScroll(scroller)
                        }
                    }
                }
            }

            return intArrayOf(0, 0)
        }

        override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
            if (layoutManager is CardStackLayoutManager) {
                val view = layoutManager.findViewByPosition(layoutManager.getTopPosition())
                if (view != null) {
                    val x = view.translationX.toInt()
                    val y = view.translationY.toInt()
                    if (x == 0 && y == 0) {
                        return null
                    }
                    return view
                }
            }
            return null
        }

        override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager?, velocityX: Int, velocityY: Int): Int {
            this.velocityX = abs(velocityX)
            this.velocityY = abs(velocityY)
            if (layoutManager is CardStackLayoutManager) {
                return layoutManager.getTopPosition()
            }
            return RecyclerView.NO_POSITION
        }
    })

    override fun onUserDetailsClick(userId: Int) {
        (requireActivity() as MainActivity).openProfileDetails(userId, "home")
    }

    override fun onMessageClick(userId: Int) {
        startActivity(Intent(requireActivity(), ChatActivity::class.java).apply {
            putExtra("userId", userId)
        })
        requireActivity().swipeRight()
    }

    override fun onDailyDiamondClaim() {
        lifecycleScope.launch(Dispatchers.IO) {
            val map = mapOf(
                "que_cat_id" to 1,
                "que_id" to 3,
                "is_daily" to true,
                "day" to getUserPrefs().checkInDay,
            ).toMutableMap<String, Any?>()

            viewModel.claimDiamond(map).collect {
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

                            Toast.makeText(getKoinContext(), it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                try {
                                    val rootJson = it.data

                                    if (rootJson.has("data")) {

                                        val code = if (rootJson.has("code")) rootJson.getAsJsonPrimitive("code").asInt else null

                                        if ((code != null) && (code == 409)) {
                                            Toast.makeText(
                                                getKoinContext(),
                                                if (rootJson.has("data")) rootJson.getAsJsonPrimitive("data").asString else "Diamond already claimed",
                                                Toast.LENGTH_SHORT,
                                            ).show()

                                            getUserPrefs().isDailyCheckInAvailable = false
                                        } else {
                                            val dataJson = rootJson.getAsJsonObject("data")

                                            if (dataJson.has("isClaim") && dataJson.has("diamonds")) {
                                                val isClaimed = dataJson.getAsJsonPrimitive("isClaim").asBoolean

                                                if (isClaimed) {
                                                    val totalDiamonds = dataJson.getAsJsonPrimitive("diamonds").asInt
                                                    getUserPrefs().remainingDiamonds = totalDiamonds

                                                    getUserPrefs().isDailyCheckInAvailable = false

                                                    EventManager.postEvent(Event(EventConstants.UPDATE_DIAMOND_COUNT, null))
                                                } else {
                                                    val message = rootJson.getAsJsonPrimitive("message").asString
                                                    Toast.makeText(
                                                        getKoinContext(), message ?: "Something went wrong, try again!", Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                val message = rootJson.getAsJsonPrimitive("message").asString
                                                Toast.makeText(getKoinContext(), message ?: "Something went wrong, try again!", Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                        }
                                    } else {
                                        val message = rootJson.getAsJsonPrimitive("message").asString
                                        Toast.makeText(getKoinContext(), message ?: "Something went wrong, try again!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    catchLog("onDailyDiamondClaim: ${gsonString(e)}")

                                    FirebaseCrashlytics.getInstance().recordException(e)
                                }
                            } else {
                                Toast.makeText(getKoinContext(), "Something went wrong, try again!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    fun remove(data: User) {
        val pos = findCardByKey(data.id)
        if (pos == 0) {
            viewModel.homeUserList.set(viewModel.homeUserList.get()?.apply {
                removeAt(pos)
            })
            adapter?.getAdapterPosition(pos)?.let { adapter?.notifyItemRemoved(it) }
        } else {
            viewModel.homeUserList.set(viewModel.homeUserList.get()?.apply {
                removeAt(pos)
            })
            adapter?.getAdapterPosition(pos)?.let { adapter?.notifyItemRemoved(it) }
        }
    }

    private fun findCardByKey(searchKey: Int): Int {
        if (viewModel.homeUserList.get().isNullOrEmpty()) {
            return -1
        }
        for (i in viewModel.homeUserList.get()!!.indices) {
            if (viewModel.homeUserList.get()!![i].id == searchKey) {
                return i
            }
        }
        return -1
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.INIT_SUCCESS -> {
                if (viewModel.isDataLoaded.get() == false) {
                    gettingProfilesForFirstTime()
                }
            }

            EventConstants.FILTERS_UPDATED -> {
                binding.cardStackView.onFlingListener = null
                binding.cardStackView.adapter = null
                adapter = null
                manager = null

                initViews()

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.homeUserList.set(null)
                    viewModel.isDataLoaded.set(false)

                    if (viewModel.isLoadingProfiles.get() == false) {
                        getUserPrefs().countryCode?.let { countryCode ->
                            viewModel.isLoadingProfiles.set(true)
                            viewModel.getHomeProfiles(countryCode).collectLatest {
                                appendProfiles(it)
                            }
                        }
                    }
                }
            }

            EventConstants.USER_DETAIL_PAGE_ACTION -> {
                val intentData = value as? Intent
                if ((intentData != null) && intentData.hasExtra("resultData")) {
                    when (intentData.getStringExtra("resultData")) {
                        "Like" -> {
                            if (intentData.hasExtra("deductFrom")) {
                                val profileId = if (intentData.hasExtra("profileId")) {
                                    intentData.getIntExtra("profileId", 0)
                                } else {
                                    viewModel.homeUserList.get()?.get(adapter!!.getContentPosition(manager!!.getTopPosition() - 1))?.id
                                }

                                viewModel.homeUserList.get()?.find { data ->
                                    data.id == profileId
                                }?.apply { deductFrom = intentData.getStringExtra("deductFrom") }
                            }

                            isAlreadyActioned = true
                            state = true
                            val setting = SwipeAnimationSetting.Builder().setDirection(Direction.Right).setDuration(Duration.Normal.duration)
                                .setInterpolator(AccelerateInterpolator()).build()
                            manager!!.setSwipeAnimationSetting(setting)
                            binding.cardStackView.swipe()
                        }

                        "SuperLike" -> {
                            if (intentData.hasExtra("deductFrom")) {
                                val profileId = if (intentData.hasExtra("profileId")) {
                                    intentData.getIntExtra("profileId", 0)
                                } else {
                                    viewModel.homeUserList.get()?.get(adapter!!.getContentPosition(manager!!.getTopPosition() - 1))?.id
                                }

                                viewModel.homeUserList.get()?.find { data ->
                                    data.id == profileId
                                }?.apply { deductFrom = intentData.getStringExtra("deductFrom") }
                            }

                            isAlreadyActioned = true
                            state = true
                            val setting = SwipeAnimationSetting.Builder().setDirection(Direction.Top).setDuration(Duration.Normal.duration)
                                .setInterpolator(AccelerateInterpolator()).build()
                            manager!!.setSwipeAnimationSetting(setting)
                            binding.cardStackView.swipe()
                        }

                        "Skip" -> {
                            isAlreadyActioned = true
                            state = true
                            val setting = SwipeAnimationSetting.Builder().setDirection(Direction.Left).setDuration(Duration.Normal.duration)
                                .setInterpolator(AccelerateInterpolator()).build()
                            manager!!.setSwipeAnimationSetting(setting)
                            binding.cardStackView.swipe()
                        }

                        "Block" -> {
                            viewModel.homeUserList.get()?.get(adapter!!.getContentPosition(manager!!.getTopPosition()))
                                .let { it?.let { it1 -> remove(it1) } }
                        }

                        "Report" -> {
                            viewModel.homeUserList.get()?.get(adapter!!.getContentPosition(manager!!.getTopPosition()))
                                .let { it?.let { it1 -> remove(it1) } }
                        }
                    }
                }
            }
        }
    }
}
package com.belive.dating.activities.edit_profile.your_styles.personality_type

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityEditPersonalityBinding
import com.belive.dating.di.profileViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Activity for editing the user's personality types.
 *
 * This activity allows users to view and modify their selected personality types.
 * It interacts with the [EditPersonalityViewModel] for data management and
 * the [PersonalityAdapter] to display the list of personality types.  The user's
 * selection is persisted to user preferences and synchronized with a remote server.
 *
 * Key features:
 *  - Displays a list of personality types using a FlexboxLayoutManager for flexible arrangement.
 *  - Allows users to select up to 3 personality types.
 *  - Saves the selected personality types to user preferences and a remote server.
 *  - Handles network connectivity changes and authentication state.
 *  - Provides a back button and save button for navigation and data persistence.
 *  - Uses Mixpanel for activity tracking (resume/pause).
 *  - Integrates with a loading dialog to indicate ongoing operations.
 *
 *  @see EditPersonalityViewModel
 *  @see PersonalityAdapter
 *  @see NetworkReceiverActivity
 *  @see PersonalityAdapter.PersonalityClickListener
 */
class EditPersonalityActivity : NetworkReceiverActivity(), PersonalityAdapter.PersonalityClickListener {

    val binding by lazy {
        ActivityEditPersonalityBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditPersonalityViewModel

    val adapter by lazy {
        PersonalityAdapter(viewModel, this)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditPersonalityActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditPersonalityActivity::class.java.simpleName)
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

        setSystemBarColors(getColorFromAttr(R.attr.windowBackground))

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
                finish()
                swipeLeft()
            }
        })

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            savePersonalityTypes()
        }
    }

    private fun initViews() {
        val personalityList = arrayListOf<String>()
        getUserPrefs().personalityTypes?.let { personalityList.addAll(it) }

        viewModel.selectedPersonalityList.set(personalityList)
        viewModel.personalityCount.set(personalityList.size)

        if (binding.rvPersonalityType.layoutManager == null) {
            val layoutManager = FlexboxLayoutManager(this)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            binding.rvPersonalityType.layoutManager = layoutManager
        }
        if (binding.rvPersonalityType.adapter == null) {
            binding.rvPersonalityType.adapter = adapter
        }
    }

    private fun savePersonalityTypes() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.savePersonalityTypes().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditPersonalityActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditPersonalityActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditPersonalityActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditPersonalityActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().personalityTypes = if (it.data.user.personalityType.isNullOrEmpty()) {
                                    null
                                } else if (it.data.user.personalityType.contains(",")) {
                                    it.data.user.personalityType.split(",").toList()
                                } else {
                                    listOf(it.data.user.personalityType)
                                }
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_PERSONALITY, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditPersonalityActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
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

                startActivity(Intent(this@EditPersonalityActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)
        }
    }

    override fun onPersonalityClick(position: Int) {
        val personalityType = viewModel.personalityList[position]

        var isValid = false

        if (viewModel.selectedPersonalityList.get()?.contains(personalityType) == true) {
            viewModel.selectedPersonalityList.set(viewModel.selectedPersonalityList.get()!!.apply {
                remove(personalityType)
            })

            adapter.notifyItemChanged(position)

            if (viewModel.selectedPersonalityList.get()!!.size == getUserPrefs().personalityTypes?.size) {
                viewModel.selectedPersonalityList.get()?.forEach {
                    if (getUserPrefs().personalityTypes?.contains(it) == false) {
                        isValid = true
                    }
                }
            } else {
                isValid = true
            }
        } else {
            if (viewModel.selectedPersonalityList.get()!!.size >= 3) {
                Toast.makeText(this, "Maximum 3 personality types can be selected", Toast.LENGTH_SHORT).show()

                if (viewModel.selectedPersonalityList.get()!!.size == getUserPrefs().personalityTypes?.size) {
                    viewModel.selectedPersonalityList.get()?.forEach {
                        if (getUserPrefs().personalityTypes?.contains(it) == false) {
                            isValid = true
                            return@forEach
                        }
                    }
                } else {
                    isValid = true
                }
            } else {
                viewModel.selectedPersonalityList.set(viewModel.selectedPersonalityList.get()!!.apply {
                    add(personalityType)
                })

                adapter.notifyItemChanged(position)

                if (viewModel.selectedPersonalityList.get()!!.size == getUserPrefs().personalityTypes?.size) {
                    viewModel.selectedPersonalityList.get()?.forEach {
                        if (getUserPrefs().personalityTypes?.contains(it) == false) {
                            isValid = true
                            return@forEach
                        }
                    }
                } else {
                    isValid = true
                }
            }
        }

        viewModel.personalityCount.set(viewModel.selectedPersonalityList.get()!!.size)
        viewModel.isButtonEnabled.set(isValid)
    }
}
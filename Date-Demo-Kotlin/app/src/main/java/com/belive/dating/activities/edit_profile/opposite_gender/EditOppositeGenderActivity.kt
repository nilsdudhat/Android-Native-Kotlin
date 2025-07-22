package com.belive.dating.activities.edit_profile.opposite_gender

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
import com.belive.dating.activities.edit_profile.height.EditHeightActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.constants.EventConstants
import com.belive.dating.constants.OPPOSITE_GENDER_OPTIONS
import com.belive.dating.databinding.ActivityEditOppositeGenderBinding
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Activity for editing the user's preferred opposite gender for matching.
 *
 * This activity allows users to select their preferred opposite gender from options like "men", "women", and "everyone".
 * It saves the selected preference and updates the user's profile.
 *
 * Key functionalities:
 *  - Displays the current preferred opposite gender using data binding.
 *  - Allows selecting a new opposite gender preference via radio button-like layouts.
 *  - Saves the updated preference to the user's profile through the `EditOppositeGenderViewModel`.
 *  - Handles back navigation using the `OnBackPressedDispatcher` and a custom animation (`swipeLeft`).
 *  - Customizes system UI with edge-to-edge support and setting system bar colors.
 *  - Manages network connectivity changes through inheritance from `NetworkReceiverActivity`.
 *  - Handles user authentication, including sign-out scenarios triggered by admin blocks or duplicate logins, redirecting to `SignInActivity`.
 *  - Tracks user interactions with Mixpanel analytics for activity lifecycle events (`onResume`, `onPause`).
 *  - Persists and restores the view model state using `onSaveInstanceState` and `onRestoreInstanceState`.
 *  - Displays loading dialogs during network operations and shows error messages via `Toast`.
 *  - Updates local user preferences (`getUserPrefs`) and posts an `UPDATE_OPPOSITE_GENDER` event upon successful save.
 *  - Utilizes Koin for dependency injection of the view model and authentication helper.
 *  - Implements toolbar with a back button and handles its click events.
 *
 * The activity uses `ActivityEditOppositeGenderBinding` for view binding and interacts with the `EditOppositeGenderViewModel`
 * to manage data and business logic.  It observes changes in the view model's `selectedOppositeGender` and `isButtonEnabled`
 * observable properties to update the UI and enable/disable the save button accordingly. The `validateOppositeGender` function
 * determines the button's enabled state based on whether the selected gender differs from the current preference.
 *
 * The `authOut` function handles sign-out triggered by errors or security concerns, leveraging an `AuthenticationHelper`
 * obtained via Koin.  It navigates the user to the `SignInActivity` and clears the activity stack.
 *
 */
class EditOppositeGenderActivity : NetworkReceiverActivity() {

    val binding by lazy {
        ActivityEditOppositeGenderBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditOppositeGenderViewModel

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditHeightActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditHeightActivity::class.java.simpleName)
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
                finish()
                swipeLeft()
            }
        })

        binding.layoutMen.setOnClickListener {
            viewModel.selectedOppositeGender.set(OPPOSITE_GENDER_OPTIONS.MEN)
            binding.executePendingBindings()

            validateOppositeGender()
        }

        binding.layoutWomen.setOnClickListener {
            viewModel.selectedOppositeGender.set(OPPOSITE_GENDER_OPTIONS.WOMEN)
            binding.executePendingBindings()

            validateOppositeGender()
        }

        binding.layoutEveryone.setOnClickListener {
            viewModel.selectedOppositeGender.set(OPPOSITE_GENDER_OPTIONS.BOTH)
            binding.executePendingBindings()

            validateOppositeGender()
        }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.saveOppositeGender().collectLatest {
                    launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {
                                LoadingDialog.show(this@EditOppositeGenderActivity)
                            }

                            Status.ADMIN_BLOCKED -> {
                                Toast.makeText(
                                    this@EditOppositeGenderActivity,
                                    "Admin has blocked you, because of security reasons.",
                                    Toast.LENGTH_SHORT,
                                ).show()

                                authOut()
                            }

                            Status.SIGN_OUT -> {
                                Toast.makeText(this@EditOppositeGenderActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                    .show()

                                authOut()
                            }

                            Status.ERROR -> {
                                LoadingDialog.hide()
                                Toast.makeText(this@EditOppositeGenderActivity, it.message, Toast.LENGTH_SHORT).show()
                            }

                            Status.SUCCESS -> {
                                LoadingDialog.hide()

                                if (it.data != null) {
                                    if (!it.data.user.oppositeGender.isNullOrEmpty()) {
                                        getUserPrefs().oppositeGender = it.data.user.oppositeGender
                                        getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                        EventManager.postEvent(Event(EventConstants.UPDATE_OPPOSITE_GENDER, null))
                                        EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                        onBackPressedDispatcher.onBackPressed()
                                    } else {
                                        Toast.makeText(
                                            this@EditOppositeGenderActivity,
                                            "Something went wrong, please try again...!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(this@EditOppositeGenderActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun validateOppositeGender() {
        when (getUserPrefs().oppositeGender) {
            "men" -> {
                viewModel.isButtonEnabled.set(viewModel.selectedOppositeGender.get() != OPPOSITE_GENDER_OPTIONS.MEN)
            }

            "women" -> {
                viewModel.isButtonEnabled.set(viewModel.selectedOppositeGender.get() != OPPOSITE_GENDER_OPTIONS.WOMEN)
            }

            "everyone" -> {
                viewModel.isButtonEnabled.set(viewModel.selectedOppositeGender.get() != OPPOSITE_GENDER_OPTIONS.BOTH)
            }
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        when (getUserPrefs().oppositeGender) {
            "men" -> {
                viewModel.selectedOppositeGender.set(OPPOSITE_GENDER_OPTIONS.MEN)
            }

            "women" -> {
                viewModel.selectedOppositeGender.set(OPPOSITE_GENDER_OPTIONS.WOMEN)
            }

            "everyone" -> {
                viewModel.selectedOppositeGender.set(OPPOSITE_GENDER_OPTIONS.BOTH)
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

                startActivity(Intent(this@EditOppositeGenderActivity, SignInActivity::class.java))
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
}
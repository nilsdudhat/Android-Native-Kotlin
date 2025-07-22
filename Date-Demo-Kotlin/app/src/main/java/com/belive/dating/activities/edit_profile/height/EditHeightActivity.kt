package com.belive.dating.activities.edit_profile.height

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
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
import com.belive.dating.databinding.ActivityEditHeightBinding
import com.belive.dating.di.profileViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Activity for editing the user's height.
 *
 * This activity allows users to update their height, either in centimeters or feet and inches.
 * It includes input validation, data persistence, and UI updates based on user interactions and network responses.
 *
 *  Key Features:
 *  - Height input in centimeters or feet/inches.
 *  - Input validation to ensure values are within acceptable ranges (90-241cm or 3-7ft and 0-11in).
 *  - Data binding with [EditHeightViewModel] for managing UI state and data.  This includes two-way data binding for input fields and updating UI elements like the "Save" button's enabled state.
 *  - Saving height data to the backend via the [EditHeightViewModel] and updating user preferences in shared preferences upon successful save.
 *  - Handling back navigation using the `onBackPressedDispatcher` to provide a smooth user experience and transition animation.
 *  - Tracking user interactions using Mixpanel for analytics to monitor feature usage and user behavior.
 *  - Handling network connectivity changes through inheritance from [NetworkReceiverActivity] and implementing appropriate UI or functional responses (though currently empty in this class).
 *  - Responding to authentication-related events, such as admin blocks or duplicate logins, triggering a sign-out and redirecting the user to the [SignInActivity].
 *  - Persisting and restoring UI state using `onSaveInstanceState` and `onRestoreInstanceState` to maintain user input across configuration changes.
 *
 * The activity uses a MVVM (Model-View-ViewModel) architecture pattern, separating UI logic from data handling.  It utilizes Kotlin's `Flow` for asynchronous operations like saving data, and `Data Binding` for efficient UI updates.
 */
class EditHeightActivity : NetworkReceiverActivity() {

    val binding: ActivityEditHeightBinding by lazy {
        ActivityEditHeightBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditHeightViewModel

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

            // Set top padding for status bar
            view.setPadding(
                view.paddingLeft,
                statusBarHeight,
                view.paddingRight,
                if (imeHeight > 0) imeHeight else navBarHeight,
            )

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

        observers()
    }

    private fun observers() {
        binding.edtCentimetres.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                validateCentimeters()
            }
        })

        binding.edtFeet.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                validateFeetInch(true)
            }
        })

        binding.edtInch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                validateFeetInch(false)
            }
        })
    }

    private fun validateCentimeters() {
        if (binding.edtCentimetres.text.isNullOrEmpty()) {
            viewModel.isButtonEnabled.set(false)
            binding.edtCentimetres.error = "Please enter height"
        } else if ((binding.edtCentimetres.text!!.toString().toInt() < 90) || (binding.edtCentimetres.text!!.toString().toInt() > 241)) {
            viewModel.isButtonEnabled.set(false)
            binding.edtCentimetres.error = "Please enter height between 90 to 241"
        } else {
            binding.edtCentimetres.error = null
            viewModel.isButtonEnabled.set(true)
        }
    }

    private fun validateFeetInch(isFeet: Boolean) {
        if (isFeet) {
            if (binding.edtFeet.text.isNullOrEmpty()) {
                viewModel.isButtonEnabled.set(false)
                binding.edtFeet.error = "Please enter feet between 3 to 7"
            } else if ((binding.edtFeet.text!!.toString().toInt() < 3) || (binding.edtFeet.text!!.toString().toInt() > 7)) {
                viewModel.isButtonEnabled.set(false)
                binding.edtFeet.error = "Please enter feet between 3 to 7"
            } else if (binding.edtInch.text.isNullOrEmpty()) {
                viewModel.isButtonEnabled.set(false)
                binding.edtInch.error = "Please enter inch between 0 to 11"
            } else if ((binding.edtInch.text!!.toString().toInt() < 0) || (binding.edtInch.text!!.toString().toInt() > 11)) {
                viewModel.isButtonEnabled.set(false)
                binding.edtInch.error = "Please enter inch between 0 to 11"
            } else {
                binding.edtFeet.error = null
                binding.edtInch.error = null
                viewModel.isButtonEnabled.set(true)
            }
        } else {
            if (binding.edtInch.text.isNullOrEmpty()) {
                viewModel.isButtonEnabled.set(false)
                binding.edtInch.error = "Please enter inch between 0 to 11"
            } else if ((binding.edtInch.text!!.toString().toInt() < 0) || (binding.edtInch.text!!.toString().toInt() > 11)) {
                viewModel.isButtonEnabled.set(false)
                binding.edtInch.error = "Please enter inch between 0 to 11"
            } else if (binding.edtFeet.text.isNullOrEmpty()) {
                viewModel.isButtonEnabled.set(false)
                binding.edtFeet.error = "Please enter feet between 3 to 7"
            } else if ((binding.edtFeet.text!!.toString().toInt() < 3) || (binding.edtFeet.text!!.toString().toInt() > 7)) {
                viewModel.isButtonEnabled.set(false)
                binding.edtFeet.error = "Please enter feet between 3 to 7"
            } else {
                binding.edtFeet.error = null
                binding.edtInch.error = null
                viewModel.isButtonEnabled.set(true)
            }
        }
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                swipeLeft()
            }
        })

        binding.txtCm.setOnClickListener {
            if (viewModel.isHeightInCentimetre.get() != true) {
                viewModel.isHeightInCentimetre.set(true)
                validateCentimeters()
            }
        }

        binding.txtFtIn.setOnClickListener {
            if (viewModel.isHeightInCentimetre.get() == true) {
                viewModel.isHeightInCentimetre.set(false)
                validateFeetInch(true)
            }
        }

        binding.btnSave.setOnClickListener {
            saveHeight()
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        if (getUserPrefs().height != null) {
            viewModel.isHeightInCentimetre.set(false)
            viewModel.feet.set(getUserPrefs().height?.feet.toString())
            viewModel.inch.set(getUserPrefs().height?.inch.toString())
            viewModel.centimeter.set(Pair<Int?, Int?>(getUserPrefs().height?.feet, getUserPrefs().height?.inch).toCentimeters().toString())
        }
    }

    fun Pair<Int?, Int?>.toCentimeters(): Int {
        val (feet, inches) = this

        // Handle overflow inches (e.g., 5ft 13in -> 6ft 1in)
        val totalInches = ((feet ?: 0) * 12) + (inches ?: 0).coerceAtLeast(0)

        // Convert to centimeters (1 inch = 2.54 cm)
        return (totalInches * 2.54).roundToInt()
    }

    private fun saveHeight() {
        lifecycleScope.launch(Dispatchers.IO) {
            val feet: Int
            val inch: Int

            if (viewModel.isHeightInCentimetre.get() == true) {
                val totalInches = viewModel.centimeter.get()!!.toInt() / 2.54
                feet = floor(totalInches / 12).toInt()
                inch = round(totalInches % 12).toInt()
            } else {
                feet = viewModel.feet.get()!!.toInt()
                inch = viewModel.inch.get()!!.toInt()
            }

            logger("--height--", "${feet}'${inch}''")

            viewModel.saveHeight(feet, inch).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditHeightActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditHeightActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(
                                this@EditHeightActivity,
                                "Your session has expired, Please log in again.",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditHeightActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                if (it.data.user.height != null) {
                                    getUserPrefs().height = it.data.user.height
                                    getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                    EventManager.postEvent(Event(EventConstants.UPDATE_HEIGHT, null))
                                    EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                    onBackPressedDispatcher.onBackPressed()
                                } else {
                                    Toast.makeText(
                                        this@EditHeightActivity,
                                        "Something went wrong, please try again...!",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@EditHeightActivity,
                                    "Something went wrong, please try again...!",
                                    Toast.LENGTH_SHORT,
                                ).show()
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

                startActivity(Intent(this@EditHeightActivity, SignInActivity::class.java))
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
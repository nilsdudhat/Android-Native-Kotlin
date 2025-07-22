package com.belive.dating.activities.edit_profile.school

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityEditSchoolBinding
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

class EditSchoolActivity : NetworkReceiverActivity() {

    val binding by lazy {
        ActivityEditSchoolBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditSchoolMeViewModel

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditSchoolActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditSchoolActivity::class.java.simpleName)
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
        binding.edtSchool.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                if (binding.edtSchool.text.isNullOrEmpty() || binding.edtSchool.text!!.length < 10) {
                    binding.edtSchool.error = StringBuilder().append(getString(R.string.error_school_minimum))
                    viewModel.isButtonEnabled.set(false)
                } else if (binding.edtSchool.text!!.length > 50) {
                    binding.edtSchool.error = StringBuilder().append(getString(R.string.error_school_maximum))
                    viewModel.isButtonEnabled.set(false)
                } else if (binding.edtSchool.text.toString() == getUserPrefs().school) {
                    binding.edtSchool.error = null
                    viewModel.isButtonEnabled.set(false)
                } else {
                    val string = binding.edtSchool.text.toString().replace(("[^\\d.]").toRegex(), "")
                    var consecutiveDigits = false
                    for (i in 0 until string.length - 6) {
                        consecutiveDigits =
                            string[i].isDigit() && string[i + 1].isDigit() && string[i + 2].isDigit() && string[i + 3].isDigit() && string[i + 4].isDigit() && string[i + 5].isDigit() && string[i + 6].isDigit()
                        if (consecutiveDigits) {
                            break
                        }
                    }
                    if (consecutiveDigits) {
                        binding.edtSchool.error = StringBuilder().append(getString(R.string.error_seven_digits))
                        viewModel.isButtonEnabled.set(false)
                    } else {
                        binding.edtSchool.error = null
                        viewModel.isButtonEnabled.set(true)
                    }
                }
            }
        })
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
            saveSchool()
        }
    }

    private fun initViews() {
        if (viewModel.school.get().isNullOrEmpty()) {
            viewModel.school.set(getUserPrefs().school)
        }
    }

    private fun saveSchool() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.validateName().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditSchoolActivity)
                        }

                        Status.ADMIN_BLOCKED -> {

                        }

                        Status.SIGN_OUT -> {

                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()
                            Toast.makeText(this@EditSchoolActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            if (it.data != null) {
                                val response = it.data

                                if (response.has("valid")) {
                                    val isValid = response.getAsJsonPrimitive("valid").asBoolean

                                    if (isValid) {
                                        launch(Dispatchers.IO) {
                                            viewModel.saveSchool().collectLatest {
                                                launch(Dispatchers.Main) {
                                                    when (it.status) {
                                                        Status.LOADING -> {
                                                            LoadingDialog.show(this@EditSchoolActivity)
                                                        }

                                                        Status.ADMIN_BLOCKED -> {
                                                            Toast.makeText(
                                                                this@EditSchoolActivity,
                                                                "Admin has blocked you, because of security reasons.",
                                                                Toast.LENGTH_SHORT,
                                                            ).show()

                                                            authOut()
                                                        }

                                                        Status.SIGN_OUT -> {
                                                            Toast.makeText(
                                                                this@EditSchoolActivity,
                                                                "Your session has expired, Please log in again.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            authOut()
                                                        }

                                                        Status.ERROR -> {
                                                            LoadingDialog.hide()
                                                            Toast.makeText(this@EditSchoolActivity, it.message, Toast.LENGTH_SHORT).show()
                                                        }

                                                        Status.SUCCESS -> {
                                                            LoadingDialog.hide()

                                                            if (it.data != null) {
                                                                getUserPrefs().school = it.data.user.school
                                                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                                                EventManager.postEvent(Event(EventConstants.UPDATE_SCHOOL, null))
                                                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                                                onBackPressedDispatcher.onBackPressed()
                                                            } else {
                                                                Toast.makeText(
                                                                    this@EditSchoolActivity,
                                                                    "Something went wrong, please try again...!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        LoadingDialog.hide()

                                        if (response.has("message")) {
                                            val message = response.getAsJsonPrimitive("message").asString
                                            Toast.makeText(this@EditSchoolActivity, message, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this@EditSchoolActivity, "Entered text contains inappropriate words", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                } else {
                                    LoadingDialog.hide()

                                    Toast.makeText(this@EditSchoolActivity, it.message ?: "Something went wrong...!!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                LoadingDialog.hide()

                                Toast.makeText(this@EditSchoolActivity, it.message ?: "Something went wrong...!!", Toast.LENGTH_SHORT).show()
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

                startActivity(Intent(this@EditSchoolActivity, SignInActivity::class.java))
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
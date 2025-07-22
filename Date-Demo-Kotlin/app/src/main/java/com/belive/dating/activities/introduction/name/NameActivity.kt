package com.belive.dating.activities.introduction.name

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.introduction.gender.GenderActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.databinding.ActivityNameBinding
import com.belive.dating.di.gistModule
import com.belive.dating.di.introductionModule
import com.belive.dating.di.introductionViewModels
import com.belive.dating.di.splashDataModule
import com.belive.dating.dialogs.AppDialog
import com.belive.dating.dialogs.InitFailedDialog
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getIntroductionPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.hideKeyboard
import com.belive.dating.extensions.isAppUpdateRequired
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.reOpenApp
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.showKeyboard
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.get_gist.getGistData
import com.belive.dating.helpers.helper_functions.splash_data.SplashData
import com.belive.dating.preferences.pref_utils.IntroductionPrefUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.context.unloadKoinModules

class NameActivity : NetworkReceiverActivity() {

    private val TAG = "--name--"

    private val binding: ActivityNameBinding by lazy {
        ActivityNameBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: NameViewModel

    override fun onResume() {
        super.onResume()

        logger("--introduction--", "onResume: $TAG")

        mixPanel?.timeEvent(NameActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        logger("--introduction--", "onPause: $TAG")

        if (isFinishing) {
            mixPanel?.track(NameActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", true)
            })
        } else {
            mixPanel?.track(NameActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", false)
            })
        }
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

        viewModel = tryKoinViewModel(listOf(introductionModule, introductionViewModels))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getKoinObject().get<IntroductionPrefUtils>().clear()

                hideKeyboard()

                val authenticationHelper = getKoinObject().get<AuthenticationHelper>()
                authenticationHelper.signOut(
                    lifecycleScope = lifecycleScope,
                    onSuccess = {
                        getKoinObject().get<IntroductionPrefUtils>().clear()

                        finish()
                        swipeLeft()
                    },
                )
            }
        })

        binding.footerButtons.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.footerButtons.btnNext.setOnClickListener {
            validateName()
        }
    }

    private fun validateName() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.validateName().collectLatest {

                if (it.status != Status.LOADING) {
                    mixPanel?.track(TAG, JSONObject().apply {
                        put("Error", it.message)
                        put("Status", it.status)
                        put("Data", it.data)
                        put("Name", viewModel.name.get()?.trim())
                    })
                }

                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@NameActivity)
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@NameActivity, it.message ?: "Something went wrong...!!", Toast.LENGTH_SHORT).show()
                        }

                        Status.SIGN_OUT -> {

                        }

                        Status.ADMIN_BLOCKED -> {

                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                val response = it.data

                                if (response.has("valid")) {
                                    val isValid = response.getAsJsonPrimitive("valid").asBoolean

                                    if (isValid) {
                                        getIntroductionPrefs().name = viewModel.name.get()?.trim()

                                        startActivity(Intent(this@NameActivity, GenderActivity::class.java))
                                        swipeRight()
                                    } else {
                                        if (response.has("message")) {
                                            val message = response.getAsJsonPrimitive("message").asString
                                            Toast.makeText(this@NameActivity, message, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this@NameActivity, "Entered text contains inappropriate words", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(this@NameActivity, it.message ?: "Something went wrong...!!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@NameActivity, it.message ?: "Something went wrong...!!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        viewModel.name.set(getIntroductionPrefs().name)
        getIntroductionPrefs().name?.length?.let {
            binding.edtFullName.post {
                binding.edtFullName.setSelection(it)
            }
        }
        viewModel.isNextEnabled.set(isNameValid())
        updateCounter()

        // Set the cursor to always be visible
        binding.edtFullName.setOnFocusChangeListener { _, _ ->
            binding.edtFullName.isCursorVisible = true
        }

        // If needed, ensure it's visible upon certain interactions
        binding.edtFullName.setOnClickListener {
            binding.edtFullName.isCursorVisible = true
        }

        binding.edtFullName.setOnEditorActionListener { v, actionId, event ->
            if ((actionId and EditorInfo.IME_MASK_ACTION) != 0) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.edtFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if ((binding.edtFullName.text?.isNotEmpty() == true) &&
                    (binding.edtFullName.text?.trim()?.isEmpty() == true)
                ) {
                    viewModel.name.set("")

                    updateCounter()
                    return
                }

                val isValid = isNameValid()

                viewModel.isNextEnabled.set(isValid)

                updateCounter()
            }
        })

        Handler(Looper.getMainLooper()).postDelayed({
            binding.edtFullName.showKeyboard()
        }, 500)
    }

    private fun updateCounter() {
        val name = viewModel.name.get()?.trim() ?: ""
        viewModel.nameLength.set(name.length)
    }

    private fun isNameValid(): Boolean {
        val name = viewModel.name.get()?.trim().toString()
        return if (name.isEmpty()) {
            viewModel.validationError.set("Please enter your name")
            false
        } else if (name.any { it.isDigit() }) {
            viewModel.validationError.set("Name cannot have number")
            false
        } else if (!Regex("^[a-zA-Z0-9 ]*$").matches(name)) {
            viewModel.validationError.set("Name cannot have special character")
            false
        } else if (name.length < 3) {
            viewModel.validationError.set("Minimum 3 characters required")
            false
        } else {
            viewModel.validationError.set("")
            true
        }
    }

    private fun uploadSplashData() {
        val isSplashDataAvailable = try {
            getKoinObject().get<SplashData>()
            true
        } catch (e: Exception) {
            false
        }
        if (isSplashDataAvailable) {
            val splashData = getKoinObject().get<SplashData>()
            splashData.referrers(
                onSuccess = {
                    splashData.getCountryDataApi(
                        onSuccess = {
                            splashData.sendUserData(onSuccess = {
                                unloadKoinModules(splashDataModule)
                            }, onError = {
                                unloadKoinModules(splashDataModule)
                            })
                        },
                        onError = {
                            unloadKoinModules(splashDataModule)
                        },
                    )
                },
                onError = {
                    unloadKoinModules(splashDataModule)
                },
            )
        }
    }

    private fun afterGist() {
        if (getGistPrefs().appRedirectOtherAppStatus) {
            AppDialog.showAppRedirectDialog(
                context = this,
                onManage = {
                    logger(TAG, "redirectPath: ${getGistPrefs().appNewPackageName}")

                    try {
                        val marketUri = getGistPrefs().appNewPackageName.toUri()
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (ignored: Exception) {
                        Toast.makeText(this, "Something want wrong", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        } else if (isAppUpdateRequired()) {
            val isFlexible = getGistPrefs().appUpdateAppDialogStatus

            AppDialog.showAppUpdateDialog(
                context = this,
                isFlexible = isFlexible,
                onClose = {

                },
                onManage = {
                    val packageName = getGistPrefs().appUpdatePackageName
                    logger(TAG, "packageName: $packageName")

                    try {
                        val marketUri = "https://play.google.com/store/apps/details?id=${packageName}".toUri()
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (ignored: Exception) {
                        Toast.makeText(this, "Something want wrong", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }

    private fun getGist() {
        getGistData(
            lifecycleScope,
            isLoading = {
                if (it) {
                    LoadingDialog.show(this)
                } else {
                    LoadingDialog.hide()
                }
            },
            gistNotAvailable = {
                uploadSplashData()

                afterGist()
            },
            onError = {
                InitFailedDialog.showAppUpdateDialog(onTryAgain = {
                    getGist()
                }, onReOpenApp = {
                    reOpenApp()
                })
            },
            onSuccess = {
                unloadKoinModules(gistModule)

                LoadingDialog.show(this)

                uploadSplashData()

                ManageAds.loadAds()

                AdmobAds.showAppOpenAdAfterSplash {
                    LoadingDialog.hide()

                    afterGist()
                }
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        if (intent.hasExtra("load_gist") && intent.getBooleanExtra("load_gist", true)) {
            getGist()
        } else {
            afterGist()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        if (isConnected) {
            if (intent.hasExtra("load_gist") && intent.getBooleanExtra("load_gist", true)) {
                getGist()
            } else {
                afterGist()
            }
        }
    }
}
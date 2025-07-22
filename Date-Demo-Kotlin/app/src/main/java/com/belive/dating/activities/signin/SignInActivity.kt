package com.belive.dating.activities.signin

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.activities.introduction.name.NameActivity
import com.belive.dating.activities.permission.PermissionsManagerActivity
import com.belive.dating.ads.InterstitialGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.constants.IntroductionConstants
import com.belive.dating.databinding.ActivitySignInBinding
import com.belive.dating.di.gistModule
import com.belive.dating.di.introductionModule
import com.belive.dating.di.introductionViewModels
import com.belive.dating.di.mainViewModel
import com.belive.dating.di.signInViewModel
import com.belive.dating.di.splashDataModule
import com.belive.dating.di.userModule
import com.belive.dating.dialogs.AppDialog
import com.belive.dating.dialogs.InitFailedDialog
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.checkPermissions
import com.belive.dating.extensions.getDeviceID
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getIntroductionPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.isAppUpdateRequired
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.openBrowser
import com.belive.dating.extensions.reOpenApp
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
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class SignInActivity : NetworkReceiverActivity(isToolbarAvailable = false) {

    private val TAG = "--sign_in--"

    private val binding: ActivitySignInBinding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: SignInViewModel

    private val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(SignInActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(SignInActivity::class.java.simpleName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { v, insets ->
            val paddingPixels = getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                paddingPixels,
                paddingPixels,
                paddingPixels,
                systemBars.bottom + paddingPixels,
            )
            insets
        }

        viewModel = tryKoinViewModel(listOf(introductionModule, signInViewModel))
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        binding.btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        authenticationHelper.signInWithGoogle(
            lifecycleScope = lifecycleScope,
            onLoading = {
                if (it) {
                    LoadingDialog.show(this)
                } else {
                    LoadingDialog.hide()
                }
            },
            onError = {
                LoadingDialog.hide()

                logger("--error--", "error: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()

                mixPanel?.track(TAG, JSONObject().apply {
                    put("error", it)
                })
            },
            onSuccess = { email, fcmToken ->
                getIntroductionPrefs().loginType = IntroductionConstants.GOOGLE
                getIntroductionPrefs().email = email

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.signIn(email, getDeviceID(), fcmToken).collectLatest {

                        if (it.data != null) {
                            mixPanel?.track(TAG, JSONObject().apply {
                                put("error", it.message)
                                put("code", it.data.first)
                                put("data", it.data.second)
                            })
                        }

                        launch(Dispatchers.Main) {
                            when (it.status) {
                                Status.LOADING -> {
                                    LoadingDialog.show(this@SignInActivity)
                                }

                                Status.SIGN_OUT -> {

                                }

                                Status.ADMIN_BLOCKED -> {

                                }

                                Status.ERROR -> {
                                    LoadingDialog.hide()

                                    logger("--sign_in--", "error: ${gsonString(it)}")
                                    Toast.makeText(this@SignInActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                }

                                Status.SUCCESS -> {
                                    LoadingDialog.hide()

                                    when (it.data?.first) {

                                        200 -> { // success
                                            getKoinObject().get<IntroductionPrefUtils>().clear()

                                            getUserPrefs().userToken = it.data.second
                                            getUserPrefs().fcmToken = fcmToken

                                            ManageAds.showInterstitialAd(InterstitialGroup.Intro) {
                                                if (checkPermissions()) {
                                                    loadKoinModules(userModule)
                                                    loadKoinModules(mainViewModel)

                                                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                                                    intent.putExtra("display_splash", false)
                                                    startActivity(intent)
                                                } else {
                                                    val intent = Intent(this@SignInActivity, PermissionsManagerActivity::class.java)
                                                    startActivity(intent)
                                                }
                                                finish()
                                                swipeRight()
                                            }
                                        }

                                        403 -> { // admin blocked
                                            LoadingDialog.show(this@SignInActivity)
                                            getIntroductionPrefs().loginType = -1

                                            authenticationHelper.signOut(
                                                lifecycleScope = lifecycleScope,
                                                onSuccess = {
                                                    LoadingDialog.hide()

                                                    Toast.makeText(
                                                        this@SignInActivity,
                                                        "User restricted. Please contact support.",
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                                },
                                            )
                                        }

                                        422 -> { // user not found
                                            ManageAds.showInterstitialAd(InterstitialGroup.Intro) {
                                                // Introduction Flow
                                                loadKoinModules(introductionViewModels)

                                                val intent = Intent(this@SignInActivity, NameActivity::class.java)
                                                startActivity(intent)
                                                swipeRight()
                                            }

                                            logger("--sign_in--", "signIn: ${gsonString(it)}")
                                        }

                                        498 -> { // token not found
                                            getIntroductionPrefs().loginType = -1

                                            Toast.makeText(this@SignInActivity, "Something went wrong, try again...!", Toast.LENGTH_SHORT).show()
                                        }

                                        else -> { // other error
                                            getIntroductionPrefs().loginType = -1

                                            Toast.makeText(this@SignInActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
        )
    }

    private fun initViews() {
        setTransparentStatusBarAndNavigationBar(window)

        // Get a custom drawable as the background
        val customBackground: Drawable? = ContextCompat.getDrawable(this, R.drawable.bg_rounded)

        // Create a ripple effect with the custom background drawable
        val rippleColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black_50)) // Ripple color (semi-transparent black)
        val rippleDrawable = RippleDrawable(rippleColor, customBackground, null)
        binding.btnGoogleLogin.background = rippleDrawable
    }

    private fun setPrivacyPolicy() {
        val privacyText = getString(R.string.message_policy_terms)
        binding.txtTerms.isClickable = true
        binding.txtTerms.linksClickable = true
        binding.txtTerms.movementMethod = LinkMovementMethod.getInstance()

        val spanText = SpannableString(privacyText)
        val clickableTerms = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openBrowser(getGistPrefs().termCondition)
            }
        }
        spanText.setSpan(
            clickableTerms,
            privacyText.lastIndexOf("Terms"),
            privacyText.lastIndexOf("Terms") + "Terms".length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE,
        )
        val clickablePrivacy = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openBrowser(getGistPrefs().privacyPolicy)
            }
        }
        spanText.setSpan(
            clickablePrivacy,
            privacyText.lastIndexOf("Privacy Policy"),
            privacyText.lastIndexOf("Privacy Policy") + "Privacy Policy".length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE,
        )
        binding.txtTerms.text = spanText
    }

    private fun setTransparentStatusBarAndNavigationBar(window: android.view.Window) {
        // Make the status bar and navigation bar backgrounds transparent
        window.apply {
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }

        // Handle edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API 30) and above
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // For Android 10 (API 29) and below
            @Suppress("DEPRECATION") window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }

        // Manage light/dark icons for status and navigation bars
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false  // Set to true for light status bar icons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            insetsController.isAppearanceLightNavigationBars = false // Set to true for light navigation bar icons
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
        setPrivacyPolicy()

        if (intent.hasExtra("signInAutomatically") && intent.getBooleanExtra("signInAutomatically", false)) {

        } else {
            signInWithGoogle()
        }

        if (getGistPrefs().appRedirectOtherAppStatus) {
            AppDialog.showAppRedirectDialog(
                context = this@SignInActivity,
                onManage = {
                    logger(TAG, "redirectPath: ${getGistPrefs().appNewPackageName}")

                    try {
                        val marketUri = getGistPrefs().appNewPackageName.toUri()
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (ignored: Exception) {
                        Toast.makeText(this@SignInActivity, "Something want wrong", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        } else if (isAppUpdateRequired()) {
            val isFlexible = getGistPrefs().appUpdateAppDialogStatus

            AppDialog.showAppUpdateDialog(
                context = this@SignInActivity,
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
                        Toast.makeText(this@SignInActivity, "Something want wrong", Toast.LENGTH_SHORT).show()
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
            uploadSplashData()

            setPrivacyPolicy()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        if (isConnected) {
            if (intent.hasExtra("load_gist") && intent.getBooleanExtra("load_gist", true)) {
                getGist()
            } else {
                uploadSplashData()
            }
        }
    }
}
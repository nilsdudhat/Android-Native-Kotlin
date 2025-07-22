package com.belive.dating.activities.introduction.gender

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.introduction.birthdate.BirthdateActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.constants.GENDER_OPTIONS
import com.belive.dating.databinding.ActivityGenderBinding
import com.belive.dating.di.gistModule
import com.belive.dating.di.introductionModule
import com.belive.dating.di.introductionViewModels
import com.belive.dating.di.splashDataModule
import com.belive.dating.dialogs.AppDialog
import com.belive.dating.dialogs.ConfirmBottomDialog
import com.belive.dating.dialogs.InitFailedDialog
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getIntroductionPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.isAppUpdateRequired
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.reOpenApp
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.get_gist.getGistData
import com.belive.dating.helpers.helper_functions.splash_data.SplashData
import org.json.JSONObject
import org.koin.core.context.unloadKoinModules

class GenderActivity : NetworkReceiverActivity(), ConfirmBottomDialog.OnConfirmListener {

    private val TAG = "--gender--"

    private val binding: ActivityGenderBinding by lazy {
        ActivityGenderBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: GenderViewModel

    private val genderConfirmDialog by lazy {
        ConfirmBottomDialog(
            this,
            "Confirm your Gender",
            "Yes, it’s Right.",
            null,
            this@GenderActivity,
        )
    }

    override fun onResume() {
        super.onResume()

        logger("--introduction--", "onResume: $TAG")

        mixPanel?.timeEvent(GenderActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        logger("--introduction--", "onPause: $TAG")

        if (isFinishing) {
            mixPanel?.track(GenderActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", true)
            })
        } else {
            mixPanel?.track(GenderActivity::class.java.simpleName, JSONObject().apply {
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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

    private fun initViews() {
        viewModel.selectedIndex.set(getIntroductionPrefs().gender)
        viewModel.isNextEnabled.set(viewModel.selectedIndex.get() != 0)
        binding.txtTitle.text = StringBuilder().append("${getIntroductionPrefs().name}, How do you identify your gender?")
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getIntroductionPrefs().gender = 0

                finish()
                swipeLeft()
            }
        })

        binding.footerButtons.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnMale.setOnClickListener {
            viewModel.selectedIndex.set(GENDER_OPTIONS.MALE.genderIndex)
            viewModel.isNextEnabled.set(true)
        }

        binding.btnFemale.setOnClickListener {
            viewModel.selectedIndex.set(GENDER_OPTIONS.FEMALE.genderIndex)
            viewModel.isNextEnabled.set(true)
        }

        binding.btnOther.setOnClickListener {
            viewModel.selectedIndex.set(GENDER_OPTIONS.OTHER.genderIndex)
            viewModel.isNextEnabled.set(true)
        }

        binding.footerButtons.btnNext.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (viewModel.selectedIndex.get() == GENDER_OPTIONS.MALE.genderIndex) {
                    genderConfirmDialog.setMessage(
                        Html.fromHtml(
                            "You said you are <font color='#e8ff83'><b>\"Male\"</b></font>",
                            Html.FROM_HTML_MODE_LEGACY,
                        )
                    )
                } else if (viewModel.selectedIndex.get() == GENDER_OPTIONS.FEMALE.genderIndex) {
                    genderConfirmDialog.setMessage(
                        Html.fromHtml(
                            "You said you are <font color='#e8ff83'><b>\"Female\"</b></font>",
                            Html.FROM_HTML_MODE_LEGACY,
                        )
                    )
                } else if (viewModel.selectedIndex.get() == GENDER_OPTIONS.OTHER.genderIndex) {
                    genderConfirmDialog.setMessage(
                        Html.fromHtml(
                            "You said you are <font color='#e8ff83'><b>\"Other\"</b></font>",
                            Html.FROM_HTML_MODE_LEGACY,
                        )
                    )
                }
            } else {
                if (viewModel.selectedIndex.get() == GENDER_OPTIONS.MALE.genderIndex) {
                    val spanned = SpannableString("You said you are Male")
                    genderConfirmDialog.setMessage(spanned)
                } else if (viewModel.selectedIndex.get() == GENDER_OPTIONS.FEMALE.genderIndex) {
                    val spanned = SpannableString("You said you are Female")
                    genderConfirmDialog.setMessage(spanned)
                } else if (viewModel.selectedIndex.get() == GENDER_OPTIONS.OTHER.genderIndex) {
                    val spanned = SpannableString("You said you are Other")
                    genderConfirmDialog.setMessage(spanned)
                }
            }

            genderConfirmDialog.show()
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
        ManageAds.showSmallNativeAd(SmallNativeGroup.Introduction, binding.adSmallNative)

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

    override fun onGenderConfirm() {
        if (viewModel.selectedIndex.get() == GENDER_OPTIONS.MALE.genderIndex) {
            mixPanel?.track(TAG, JSONObject().apply {
                put("Gender", GENDER_OPTIONS.MALE.name)
            })
        } else if (viewModel.selectedIndex.get() == GENDER_OPTIONS.FEMALE.genderIndex) {
            mixPanel?.track(TAG, JSONObject().apply {
                put("Gender", GENDER_OPTIONS.FEMALE.name)
            })
        } else if (viewModel.selectedIndex.get() == GENDER_OPTIONS.OTHER.genderIndex) {
            mixPanel?.track(TAG, JSONObject().apply {
                put("Gender", GENDER_OPTIONS.OTHER.name)
            })
        }

        getIntroductionPrefs().gender = GENDER_OPTIONS.entries.find { it.genderIndex == viewModel.selectedIndex.get() }?.genderIndex ?: -1

        // next activity
        startActivity(Intent(this, BirthdateActivity::class.java))
        swipeRight()
    }
}
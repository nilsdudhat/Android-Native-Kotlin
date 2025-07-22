package com.belive.dating.activities.introduction.sexual_orientation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.introduction.relationship_goal.RelationshipGoalActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.databinding.ActivitySexualOrientationBinding
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
import com.belive.dating.extensions.isAppUpdateRequired
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.reOpenApp
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.get_gist.getGistData
import com.belive.dating.helpers.helper_functions.splash_data.SplashData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.context.unloadKoinModules

class SexualOrientationActivity : NetworkReceiverActivity(), SexualOrientationAdapter.OnOrientationChangeListener {

    private val TAG = "--sexual_orientation--"

    val binding: ActivitySexualOrientationBinding by lazy {
        ActivitySexualOrientationBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: SexualOrientationViewModel

    private val adapter: SexualOrientationAdapter by lazy {
        SexualOrientationAdapter(callBack = this, viewModel = viewModel)
    }

    override fun onResume() {
        super.onResume()

        logger("--introduction--", "onResume: $TAG")

        mixPanel?.timeEvent(SexualOrientationActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        logger("--introduction--", "onPause: $TAG")

        if (isFinishing) {
            mixPanel?.track(SexualOrientationActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", true)
            })
        } else {
            mixPanel?.track(SexualOrientationActivity::class.java.simpleName, JSONObject().apply {
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

        binding.root.post {
            observeNetwork()
        }

        viewModel = tryKoinViewModel(listOf(introductionModule, introductionViewModels))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        clickListeners()

        observeData()
    }

    private fun setUpRecyclerView() {
        binding.rvOrientations.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvOrientations.adapter = adapter
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getIntroductionPrefs().sexualOrientation = -1

                finish()
                swipeLeft()
            }
        })

        binding.footerButtons.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.footerButtons.btnNext.setOnClickListener {
            mixPanel?.track(TAG, JSONObject().apply {
                put("Sexuality", adapter.asyncListDiffer.currentList.find { it.id == viewModel.selectedSexuality.get() }?.name)
            })

            if ((viewModel.selectedSexuality.get() == null) || (viewModel.selectedSexuality.get() == -1)) {
                Toast.makeText(this, "Please select your sexuality", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getIntroductionPrefs().sexualOrientation = viewModel.selectedSexuality.get()!!

            startActivity(Intent(this, RelationshipGoalActivity::class.java))
            swipeRight()
        }
    }

    private fun observeData() {
        viewModel.selectedSexuality.set(getIntroductionPrefs().sexualOrientation)

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.orientationsResource.collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            viewModel.isSkeleton.set(true)
                        }

                        Status.ADMIN_BLOCKED -> {

                        }

                        Status.SIGN_OUT -> {

                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            if (it.message != null) {
                                Toast.makeText(this@SexualOrientationActivity, "Exception: ${it.message}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SexualOrientationActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        Status.SUCCESS -> {
                            viewModel.isSkeleton.set(false)
                            setUpRecyclerView()

                            if (!it.data?.sexualOrientationList.isNullOrEmpty()) {
                                adapter.asyncListDiffer.submitList((it.data.sexualOrientationList).toMutableList())
                                viewModel.isNextEnabled.set(viewModel.selectedSexuality.get() != -1)
                            } else {
                                Toast.makeText(this@SexualOrientationActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
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

        if ((viewModel.orientationsResource.value.status != Status.SUCCESS) || (adapter.asyncListDiffer.currentList.isEmpty())) {
            viewModel.getAllSexualOrientations()
        }

        uploadSplashData()

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

    override fun onOrientationClick(position: Int) {
        val sexuality = adapter.asyncListDiffer.currentList[position]

        val previousSelectedPosition = adapter.asyncListDiffer.currentList.indexOfFirst { viewModel.selectedSexuality.get() == it.id }

        if (viewModel.selectedSexuality.get() == sexuality.id) {
            viewModel.selectedSexuality.set(null)
        } else {
            viewModel.selectedSexuality.set(sexuality.id)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        viewModel.isNextEnabled.set(true)
    }
}
package com.belive.dating.activities.introduction.choose_interest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.introduction.upload_photo.UploadPhotoActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.ActivityChooseInterestBinding
import com.belive.dating.di.gistModule
import com.belive.dating.di.googleVisionModule
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
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class ChooseInterestActivity : NetworkReceiverActivity(), InterestAdapter.OnInterestListener {

    private val TAG = "--choose_interest--"

    val binding: ActivityChooseInterestBinding by lazy {
        ActivityChooseInterestBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: ChooseInterestViewModel

    init {
        logger("--koin--", "init")
    }

    private val adapter: InterestAdapter by lazy { InterestAdapter(this) }

    override fun onResume() {
        super.onResume()

        logger("--introduction--", "onResume: $TAG")

        mixPanel?.timeEvent(ChooseInterestActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        logger("--introduction--", "onPause: $TAG")

        if (isFinishing) {
            mixPanel?.track(ChooseInterestActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", true)
            })
        } else {
            mixPanel?.track(ChooseInterestActivity::class.java.simpleName, JSONObject().apply {
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

        clickListeners()

        observeData()
    }

    private fun setUpRecyclerView() {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER

        binding.rvInterests.layoutManager = layoutManager
        binding.rvInterests.adapter = adapter
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getIntroductionPrefs().interestList = null
                finish()
                swipeLeft()
            }
        })

        binding.footerButtons.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.footerButtons.btnNext.setOnClickListener {
            val list = ArrayList<String?>()
            viewModel.selectedInterests.get()?.forEach { id ->
                list.add(adapter.asyncListDiffer.currentList.find { it.id == id }?.name)
            }

            mixPanel?.track(TAG, JSONObject().apply {
                put("Interests", list.joinToString(", ") { it.toString() })
            })

            getIntroductionPrefs().interestList = viewModel.selectedInterests.get()!!.toList().map { it.toString() }

            loadKoinModules(googleVisionModule)

            startActivity(Intent(this, UploadPhotoActivity::class.java))
            swipeRight()
        }
    }

    private fun observeData() {
        viewModel.selectedInterests.set(getIntroductionPrefs().interestList?.map { it.toInt() }?.let { ArrayList(it) })
        viewModel.interestsCount.set(viewModel.selectedInterests.get()?.size)
        viewModel.isNextEnabled.set(if (viewModel.selectedInterests.get() == null) false else viewModel.selectedInterests.get()!!.size >= 6)

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.interestsResource.collectLatest {
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
                                Toast.makeText(this@ChooseInterestActivity, "Exception: ${it.message}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@ChooseInterestActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        Status.SUCCESS -> {
                            viewModel.isSkeleton.set(false)
                            setUpRecyclerView()

                            if (!it.data?.data.isNullOrEmpty()) {
                                val list = it.data.data
                                list.forEach {
                                    it.apply {
                                        it.isChecked = viewModel.selectedInterests.get()?.contains(it.id) == true
                                    }
                                }
                                adapter.asyncListDiffer.submitList(list)
                            } else {
                                Toast.makeText(this@ChooseInterestActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
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
        ManageAds.loadAds()

        if (viewModel.interestsResource.value.status != Status.SUCCESS) {
            viewModel.getAllInterests()
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

                afterGist()
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

    override fun onInterestClick(list: ArrayList<Int>) {
        viewModel.interestsCount.set(list.size)
        viewModel.isNextEnabled.set(list.size >= 6)
        viewModel.selectedInterests.set(list)
    }
}
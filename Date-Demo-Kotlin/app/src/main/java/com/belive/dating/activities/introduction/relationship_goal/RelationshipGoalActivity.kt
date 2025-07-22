package com.belive.dating.activities.introduction.relationship_goal

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
import com.belive.dating.activities.introduction.choose_interest.ChooseInterestActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.databinding.ActivityRelationshipGoalBinding
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

class RelationshipGoalActivity : NetworkReceiverActivity(), RelationshipGoalAdapter.OnRelationshipGoalListener {

    private val TAG = "--relationship_goal--"

    val binding: ActivityRelationshipGoalBinding by lazy {
        ActivityRelationshipGoalBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: RelationshipGoalViewModel

    init {
        logger("--koin--", "init")
    }

    val adapter by lazy { RelationshipGoalAdapter(this, viewModel) }

    override fun onResume() {
        super.onResume()

        logger("--introduction--", "onResume: $TAG")

        mixPanel?.timeEvent(RelationshipGoalActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        logger("--introduction--", "onPause: $TAG")

        if (isFinishing) {
            mixPanel?.track(RelationshipGoalActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", true)
            })
        } else {
            mixPanel?.track(RelationshipGoalActivity::class.java.simpleName, JSONObject().apply {
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

        observeResources()
    }

    private fun setUpRecyclerView() {
        binding.rvRelationshipGoal.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvRelationshipGoal.adapter = adapter
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getIntroductionPrefs().relationshipGoal = -1

                finish()
                swipeLeft()
            }
        })

        binding.footerButtons.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.footerButtons.btnNext.setOnClickListener {
            mixPanel?.track(TAG, JSONObject().apply {
                put("Relationship Goal", adapter.asyncListDiffer.currentList.find { it.id == viewModel.selectedRelationshipGoal.get() }?.name)
            })

            getIntroductionPrefs().relationshipGoal = viewModel.selectedRelationshipGoal.get()!!

            startActivity(Intent(this, ChooseInterestActivity::class.java))
            swipeRight()
        }
    }

    private fun observeResources() {
        viewModel.selectedRelationshipGoal.set(getIntroductionPrefs().relationshipGoal)

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.relationshipGoalResource.collectLatest {
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
                                Toast.makeText(this@RelationshipGoalActivity, "Exception: ${it.message}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@RelationshipGoalActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        Status.SUCCESS -> {
                            viewModel.isSkeleton.set(false)
                            setUpRecyclerView()

                            if (!it.data?.data.isNullOrEmpty()) {
                                adapter.asyncListDiffer.submitList((it.data.data).toMutableList())

                                if (viewModel.selectedRelationshipGoal.get() != -1) {
                                    viewModel.isNextEnabled.set(true)
                                } else {
                                    viewModel.isNextEnabled.set(false)
                                }
                            } else {
                                Toast.makeText(this@RelationshipGoalActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
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

        if ((viewModel.relationshipGoalResource.value.status != Status.SUCCESS) || adapter.asyncListDiffer.currentList.isEmpty()) {
            viewModel.getAllRelationshipGoals()
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

    override fun onRelationshipGoalClick(position: Int) {
        val relationshipGoal = adapter.asyncListDiffer.currentList[position]

        val previousSelectedPosition = adapter.asyncListDiffer.currentList.indexOfFirst { viewModel.selectedRelationshipGoal.get() == it.id }

        if (viewModel.selectedRelationshipGoal.get() == relationshipGoal.id) {
            viewModel.selectedRelationshipGoal.set(null)
        } else {
            viewModel.selectedRelationshipGoal.set(relationshipGoal.id)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        viewModel.isNextEnabled.set(true)
    }
}
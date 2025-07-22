package com.belive.dating.activities.permission

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.databinding.ActivityPermissionsManagerBinding
import com.belive.dating.di.gistModule
import com.belive.dating.di.mainViewModel
import com.belive.dating.di.splashDataModule
import com.belive.dating.di.userModule
import com.belive.dating.dialogs.AppDialog
import com.belive.dating.dialogs.InitFailedDialog
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.askPermissions
import com.belive.dating.extensions.checkPermissions
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.isAppUpdateRequired
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.reOpenApp
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.helpers.helper_functions.get_gist.getGistData
import com.belive.dating.helpers.helper_functions.splash_data.SplashData
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class PermissionsManagerActivity : NetworkReceiverActivity(isToolbarAvailable = false) {

    private val TAG = "--permission_manager--"

    val binding: ActivityPermissionsManagerBinding by lazy {
        ActivityPermissionsManagerBinding.inflate(layoutInflater)
    }

    private var isPermissionAllowed = false

    override fun onPause() {
        super.onPause()

        mixPanel?.track(PermissionsManagerActivity::class.java.simpleName)
    }

    private val permissionDialog: Dialog by lazy {
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.dialog_permission_required)
            setCanceledOnTouchOutside(true)
            window?.setDimAmount(0.75f)
            window?.apply {
                setLayout(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(PermissionsManagerActivity::class.java.simpleName)

        if (!permissionDialog.isShowing) {
            if (checkPermissions()) {
                isPermissionAllowed = true
                binding.btnNext.text = getString(R.string.next)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val paddingPixels = getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
            v.setPadding(
                systemBars.left + paddingPixels,
                systemBars.top + paddingPixels,
                systemBars.right + paddingPixels,
                systemBars.bottom + paddingPixels,
            )
            insets
        }

        binding.root.post {
            observeNetwork()
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        initViews()

        clickListeners()

        binding.root.post {
            observeNetwork()
        }
    }

    private fun clickListeners() {
        var doubleBackToExitPressedOnce = false

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    swipeLeft()
                    return
                }

                doubleBackToExitPressedOnce = true

                Toast.makeText(
                    this@PermissionsManagerActivity, "Press back again to exit", Toast.LENGTH_SHORT
                ).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })

        binding.btnNext.setOnClickListener {
            if (isPermissionAllowed) {
                loadKoinModules(userModule)
                loadKoinModules(mainViewModel)

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("display_splash", false)
                startActivity(intent)
                finishAffinity()
            } else {
                askPermissions()
            }
        }
    }

    private fun initViews() {
        binding.btnNotification.isVisible = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 103 && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            binding.btnNext.text = getString(R.string.next)
            isPermissionAllowed = true
        } else {
            var openSettings = false

            for (permission in permissions) {
                if (!shouldShowRequestPermissionRationale(permission)) {
                    openSettings = true
                    break
                }
            }

            if (openSettings) {
                permissionDialog.show()

                val btnContinue = permissionDialog.findViewById<TextView>(R.id.btn_open_settings)
                btnContinue.setOnClickListener {
                    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                    with(intent) {
                        data = Uri.fromParts("package", packageName, null)
                        addCategory(CATEGORY_DEFAULT)
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                        addFlags(FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }
                    startActivity(intent)

                    permissionDialog.dismiss()
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
        if (getGistPrefs().appRedirectOtherAppStatus) {
            AppDialog.showAppRedirectDialog(
                context = this,
                onManage = {
                    logger(
                        TAG, "redirectPath: ${getGistPrefs().appNewPackageName}"
                    )

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
            uploadSplashData()
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
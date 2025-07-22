package com.belive.dating.helpers.helper_functions.app_update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class AppUpdateHelper(private val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {

    private var isFlexible: Boolean? = null

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(getKoinActivity())
    }

    fun checkInAppUpdate() {
        logger("--install--", "checkInAppUpdate: ")
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            logger("--install--", "addOnSuccessListener: ${appUpdateInfo.updateAvailability()}")

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                logger("--install--", "UpdateAvailability.UPDATE_AVAILABLE")

                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    logger("--install--", "AppUpdateType.IMMEDIATE")

                    appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // an activity result launcher registered via registerForActivityResult
                        activityResultLauncher,
                        // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                    isFlexible = false
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    logger("--install--", "AppUpdateType.FLEXIBLE")

                    appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // an activity result launcher registered via registerForActivityResult
                        activityResultLauncher,
                        // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                    isFlexible = true
                }
                appUpdateManager.registerListener(updateAppListener)
            } else {
                logger("--install--", "UpdateAvailability.UPDATE_NOT_AVAILABLE}")
            }
        }.addOnFailureListener {
            logger("--install--", "addOnFailureListener: ${gsonString(it)}")
        }
    }

    // Create a listener to track request state updates.
    private val updateAppListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()

                logger("--install--", "DOWNLOADING: $bytesDownloaded out of $totalBytesToDownload")
            }

            InstallStatus.DOWNLOADED -> {
                logger("--install--", "DOWNLOADED:")

                if (isFlexible != null && isFlexible!!) {
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackBarForCompleteUpdate()
                } else {
                    appUpdateManager.completeUpdate()
                }
            }

            InstallStatus.CANCELED -> {
                logger("--install--", "CANCELED:")
            }

            InstallStatus.FAILED -> {
                logger("--install--", "FAILED:")
            }

            InstallStatus.INSTALLED -> {
                logger("--install--", "INSTALLED:")
            }

            InstallStatus.INSTALLING -> {
                logger("--install--", "INSTALLING:")
            }

            InstallStatus.PENDING -> {
                logger("--install--", "PENDING:")
            }

            InstallStatus.UNKNOWN -> {
                logger("--install--", "UNKNOWN:")
            }

            InstallStatus.REQUIRES_UI_INTENT -> {
                logger("--install--", "REQUIRES_UI_INTENT:")
            }
        }
    }

    // Displays the snack bar notification and call to action.
    private fun popupSnackBarForCompleteUpdate() {
        try {
            Snackbar.make(
                getKoinActivity().window.decorView.rootView,
                "An update has just been downloaded.",
                Snackbar.LENGTH_INDEFINITE,
            ).apply {
                setAction("RESTART") {
                    appUpdateManager.completeUpdate()
                }
                show()
            }
        } catch (e: Exception) {
            catchLog("popupSnackBarForCompleteUpdate: ${gsonString(e)}")
        }
    }
}
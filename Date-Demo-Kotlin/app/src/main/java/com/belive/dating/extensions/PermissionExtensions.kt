package com.belive.dating.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.belive.dating.R

private const val PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED

val allPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.POST_NOTIFICATIONS,
    )
else
    arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
    )

fun Context.checkPermissions(): Boolean {
    var isPermissionAvailable = true

    for (permission in allPermissions) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isPermissionAvailable = false
            break
        }
    }

    return isPermissionAvailable
}

fun Activity.askPermissions(permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(
        this,
        permissions,
        requestCode,
    )
}

fun Activity.askPermissions() {
    ActivityCompat.requestPermissions(
        this,
        allPermissions,
        103,
    )
}

fun Context.hasPermission(vararg permissions: String): Boolean {
    for (permission in permissions) {
        if (!hasPermission(permission)) {
            return false
        }
    }
    return true
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
}

fun Context.showRationaleDialog(@StringRes title: Int, @StringRes message: Int) {
    buildDialog {
        setTitle(title)
        setMessage(message)
        setPositiveButton(R.string.ok) { dialog, _ ->
            openSettings()
            dialog.dismiss()
        }
    }.show()
}

fun Context.showRationaleDialog(settingLauncher: ActivityResultLauncher<Intent>) {
    buildDialog {
        setTitle(this.context.getString(R.string.permission_denied))
        setMessage(this.context.getString(R.string.missing_permissions_message))
        setPositiveButton(R.string.ok) { dialog, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            settingLauncher.launch(intent)
            dialog.dismiss()
        }
    }.show()
}

fun Context.buildDialog(
    init: (AlertDialog.Builder.() -> Unit)
): AlertDialog.Builder {
    return AlertDialog.Builder(this).apply {
        init()
    }
}

fun Context.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}
package com.belive.dating.activities

import android.content.IntentFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.belive.dating.R
import com.belive.dating.ads.ManageAds
import com.belive.dating.extensions.getActionBarHeight
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getStatusBarHeight
import com.belive.dating.extensions.pxToDp
import com.belive.dating.helpers.helper_views.internet_connection.InternetConnectionManager
import com.belive.dating.receiver.NetworkChangeCallBack
import com.belive.dating.receiver.NetworkChangeReceiver
import com.google.android.material.snackbar.Snackbar

/**
 * An abstract activity that monitors network connectivity changes and displays corresponding
 * SnackBar messages to the user.
 *
 * This class extends [MixPanelActivity] and implements [NetworkChangeCallBack] to handle
 * network status changes. It provides visual feedback to the user through SnackBar messages
 * indicating poor internet connection, no internet connection, and internet restoration.
 *
 * Subclasses should call `observeNetwork()` in their `onResume()` or `onCreate()` methods to
 * start monitoring network changes. The network monitoring will be automatically stopped
 * in `onDestroy()`.
 *
 * Subclasses can optionally override `onInternetAvailableForFirstTime()` and
 * `onInternetConfigurationChanged()` to implement custom logic based on network availability.
 * For example, `onInternetAvailableForFirstTime()` can be used to retry failed network requests
 * when the internet becomes available, and `onInternetConfigurationChanged()` can be used to
 * enable or disable features based on network connectivity.
 *
 * The SnackBar messages are displayed at the top of the screen, below the toolbar (if present)
 * and status bar, with custom styling including icons and text formatting.  The styling includes
 * a colored background, contrasting text color, a left-aligned icon, bold text, and adjusted padding
 * and text size for visual clarity.
 *
 * @property isToolbarAvailable Optional boolean indicating if a toolbar is present in the activity's layout.
 *                             This affects the top margin of the displayed SnackBars, ensuring they are
 *                             positioned correctly below the toolbar. Defaults to `true`.
 * @property networkChangeReceiver The [NetworkChangeReceiver] instance used to listen for network changes.
 */
open class NetworkReceiverActivity(
    private val isToolbarAvailable: Boolean? = true,
) : MixPanelActivity(), NetworkChangeCallBack {

    private val networkChangeReceiver by lazy {
        NetworkChangeReceiver(this)
    }

    var isNetworkAvailable = false

    private val poorInternetSnackBar by lazy {
        Snackbar.make(window.decorView.rootView, "", Snackbar.LENGTH_INDEFINITE).apply {
            val layoutParams = this.view.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.TOP
            layoutParams.topMargin = calculateTopMargin(isToolbarAvailable!!) ?: 0
            layoutParams.leftMargin = getDimensionPixelOffset(com.intuit.sdp.R.dimen._8sdp)
            layoutParams.rightMargin = getDimensionPixelOffset(com.intuit.sdp.R.dimen._8sdp)
            view.layoutParams = layoutParams

            setBackgroundTint(ContextCompat.getColor(this@NetworkReceiverActivity, R.color.colorError))
            setTextColor(ContextCompat.getColor(this@NetworkReceiverActivity, R.color.colorOnError))

            val messageView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

            val spannableString = createSpannableString("Poor internet connection", R.drawable.ic_poor_internet, R.color.colorOnPrimary)

            // Set SpannableString to TextView
            messageView.text = spannableString

            messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, pxToDp(getDimensionPixelOffset(com.intuit.sdp.R.dimen._15sdp)).toFloat())
            messageView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            messageView.includeFontPadding = false

            view.setPadding(view.paddingLeft, 0, view.paddingRight, 0) // Set custom padding
        }
    }

    private val noInternetSnackBar by lazy {
        Snackbar.make(window.decorView.rootView, "", Snackbar.LENGTH_INDEFINITE).apply {
            val layoutParams = this.view.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.TOP
            layoutParams.topMargin = calculateTopMargin(isToolbarAvailable!!) ?: 0
            layoutParams.leftMargin = getDimensionPixelOffset(com.intuit.sdp.R.dimen._8sdp)
            layoutParams.rightMargin = getDimensionPixelOffset(com.intuit.sdp.R.dimen._8sdp)
            view.layoutParams = layoutParams

            setBackgroundTint(ContextCompat.getColor(this@NetworkReceiverActivity, R.color.colorError))
            setTextColor(ContextCompat.getColor(this@NetworkReceiverActivity, R.color.colorOnError))

            val messageView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

            val spannableString = createSpannableString("You are Offline", R.drawable.ic_no_internet, R.color.colorOnPrimary)

            // Set SpannableString to TextView
            messageView.text = spannableString

            messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, pxToDp(getDimensionPixelOffset(com.intuit.sdp.R.dimen._15sdp)).toFloat())
            messageView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            messageView.includeFontPadding = false

            view.setPadding(view.paddingLeft, 0, view.paddingRight, 0) // Set custom padding
        }
    }

    private val internetRestoredSnackBar by lazy {
        Snackbar.make(window.decorView.rootView, "", Snackbar.LENGTH_SHORT).apply {
            val layoutParams = this.view.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.TOP
            layoutParams.topMargin = calculateTopMargin(isToolbarAvailable!!) ?: 0
            layoutParams.leftMargin = getDimensionPixelOffset(com.intuit.sdp.R.dimen._8sdp)
            layoutParams.rightMargin = getDimensionPixelOffset(com.intuit.sdp.R.dimen._8sdp)
            view.layoutParams = layoutParams

            setBackgroundTint(ContextCompat.getColor(this@NetworkReceiverActivity, R.color.colorPositive))
            setTextColor(ContextCompat.getColor(this@NetworkReceiverActivity, R.color.colorOnPrimary))

            val messageView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

            val spannableString = createSpannableString("Back to Online", R.drawable.ic_internet, R.color.colorOnPrimary)

            // Set SpannableString to TextView
            messageView.text = spannableString

            messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, pxToDp(getDimensionPixelOffset(com.intuit.sdp.R.dimen._15sdp)).toFloat())
            messageView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            messageView.includeFontPadding = false

            view.setPadding(view.paddingLeft, 0, view.paddingRight, 0) // Set custom padding
        }
    }

    // Create the InternetConnectionManager instance
    private val internetConnectionManager by lazy {
        InternetConnectionManager(
            noInternetSnackBar,
            poorInternetSnackBar,
            internetRestoredSnackBar,
            onInternetAvailableForFirstTime = {
                // Handle the first time internet is available
                isNetworkAvailable = true
                ManageAds.loadAds()
                onInternetAvailableForFirstTime()
            },
            onInternetConfigurationChanged = { isAvailable ->
                // Handle internet configuration changes
                isNetworkAvailable = isAvailable
                if (isAvailable) {
                    ManageAds.loadAds()
                }
                onInternetConfigurationChanged(isAvailable)
            }
        )
    }

    private fun createSpannableString(message: String, @DrawableRes iconResId: Int, @ColorRes color: Int): SpannableString {
        val text = "   $message" // Add spaces for icon padding
        val spannableString = SpannableString(text)

        // Apply Bold to the message
        val boldSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(boldSpan, 1, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Attach ImageSpan
        getSnackBarIcon(iconResId)?.let { icon ->
            icon.setTint(ContextCompat.getColor(this, color))
            val imageSpan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ImageSpan(icon, ImageSpan.ALIGN_CENTER)
            } else {
                ImageSpan(icon)
            }
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        return spannableString
    }

    private fun calculateTopMargin(isToolbarAvailable: Boolean): Int? {
        val statusBarHeight = getStatusBarHeight()
        val margin = getDimensionPixelOffset(com.intuit.sdp.R.dimen._8sdp)
        return if (isToolbarAvailable) {
            val actionBarHeight = getActionBarHeight()
            statusBarHeight?.plus(actionBarHeight)?.plus(margin)
        } else {
            statusBarHeight?.plus(margin)
        }
    }

    private fun getSnackBarIcon(@DrawableRes icon: Int): Drawable? {
        // Get the drawable
        val drawable = ContextCompat.getDrawable(this, icon)

        // Resize the drawable
        val width = getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
        val height = getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
        drawable?.setBounds(0, 0, width, height)

        return drawable
    }

    open fun observeNetwork() {
        registerReceiver(networkChangeReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(networkChangeReceiver)
        } catch (e: Exception) {

        }
        super.onDestroy()
    }

    override fun isConnected(isConnected: Boolean, isPoorSignal: Boolean) {
        internetConnectionManager.onConnectionStatusChanged(isConnected, isPoorSignal)
    }

    open fun onInternetAvailableForFirstTime() {

    }

    open fun onInternetConfigurationChanged(isConnected: Boolean) {

    }
}
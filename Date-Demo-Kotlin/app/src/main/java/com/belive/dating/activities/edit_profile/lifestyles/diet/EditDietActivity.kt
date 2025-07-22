package com.belive.dating.activities.edit_profile.lifestyles.diet

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityEditDietBinding
import com.belive.dating.di.profileViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Activity for editing the user's preferred diet.
 *
 * This activity allows the user to select their preferred diet from a list of options.
 * It handles saving the selected diet to the user's preferences and updating the UI accordingly.
 *
 *  Key features:
 *  - Displays a list of available diets using [DietAdapter] and a [FlexboxLayoutManager].
 *  - Allows the user to select a diet by clicking on it.
 *  - Saves the preferred diet to the user's preferences when the "Save" button is clicked.
 *  - Handles network connectivity changes and updates the UI accordingly.
 *  - Tracks user activity using Mixpanel for analytics.
 *  - Provides back navigation and handles the back button press.
 *  - Manages user authentication and handles cases where the user is blocked or signed out.
 *
 *  @see NetworkReceiverActivity
 *  @see DietAdapter.DietClickListener
 */
class EditDietActivity : NetworkReceiverActivity(), DietAdapter.DietClickListener {

    val binding by lazy {
        ActivityEditDietBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditDietViewModel

    val adapter by lazy {
        DietAdapter(viewModel, this)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditDietActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditDietActivity::class.java.simpleName)
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

        viewModel = tryKoinViewModel(listOf(profileViewModel))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                swipeLeft()
            }
        })

        binding.btnSave.setOnClickListener {
            savePreferredDiet()
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefDiet = if (getUserPrefs().preferredDiet.isNullOrEmpty()) {
            null
        } else {
            getUserPrefs().preferredDiet
        }
        viewModel.selectedDiet.set(prefDiet)

        if (binding.rvDiet.layoutManager == null) {
            val layoutManager = FlexboxLayoutManager(this)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            binding.rvDiet.layoutManager = layoutManager
        }
        if (binding.rvDiet.adapter == null) {
            binding.rvDiet.adapter = adapter
        }
    }

    private fun savePreferredDiet() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveDiet().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditDietActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditDietActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditDietActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditDietActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().preferredDiet = if (it.data.user.preferredDiet.isNullOrEmpty()) {
                                    null
                                } else {
                                    it.data.user.preferredDiet
                                }
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_PREFERRED_DIET, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditDietActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // Handles back action
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun authOut() {
        LoadingDialog.show(this)

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@EditDietActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)
        }
    }

    override fun onDietClick(position: Int) {
        val diet = viewModel.dietList[position]

        val previousSelectedPosition = viewModel.dietList.indexOf(viewModel.selectedDiet.get())

        if (viewModel.selectedDiet.get() == diet) {
            viewModel.selectedDiet.set(null)
        } else {
            viewModel.selectedDiet.set(diet)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        val prefDiet = if (getUserPrefs().preferredDiet.isNullOrEmpty()) {
            null
        } else {
            getUserPrefs().preferredDiet
        }

        if (viewModel.selectedDiet.get() != prefDiet) {
            viewModel.isButtonEnabled.set(true)
        } else {
            viewModel.isButtonEnabled.set(false)
        }
    }
}
package com.belive.dating.activities.edit_profile.lifestyles.drinking

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.SmallNativeGroup
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityEditDrinkingBinding
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Activity for editing the user's drinking habits.
 *
 * This activity allows the user to view and select their drinking preference from a list.
 * It interacts with the [EditDrinkingViewModel] to manage data and network operations,
 * and uses an [EditDrinkingAdapter] to display the list of options in a RecyclerView.
 *
 *  Key Functionality:
 *  - Displays a list of drinking habit options.
 *  - Allows the user to select a drinking habit.
 *  - Saves the selected drinking habit to the server.
 *  - Handles user interactions, including back navigation and saving changes.
 */
class EditDrinkingActivity : NetworkReceiverActivity(), EditDrinkingAdapter.OnDrinkingChangeListener {

    val binding by lazy {
        ActivityEditDrinkingBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditDrinkingViewModel

    val adapter by lazy {
        EditDrinkingAdapter(this, viewModel)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditDrinkingActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditDrinkingActivity::class.java.simpleName)
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
            saveDrinkingHabit()
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        val prefDrinking = if (getUserPrefs().drinking.isNullOrEmpty()) {
            null
        } else {
            getUserPrefs().drinking
        }
        viewModel.selectedDrinking.set(prefDrinking)

        if (binding.rvDrinking.layoutManager == null) {
            binding.rvDrinking.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvDrinking.adapter == null) {
            binding.rvDrinking.adapter = adapter
        }
    }

    private fun saveDrinkingHabit() {
        lifecycleScope.launch(Dispatchers.IO) {

            viewModel.saveDrinkingHabit().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditDrinkingActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditDrinkingActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditDrinkingActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditDrinkingActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().drinking = it.data.user.drinking
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_DRINKING, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditDrinkingActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
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

                startActivity(Intent(this@EditDrinkingActivity, SignInActivity::class.java))
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

    override fun onDrinkingClick(position: Int) {
        val drinking = viewModel.drinkingList[position]

        val previousSelectedPosition = viewModel.drinkingList.indexOf(viewModel.selectedDrinking.get())

        if (viewModel.selectedDrinking.get() == drinking) {
            viewModel.selectedDrinking.set(null)
        } else {
            viewModel.selectedDrinking.set(drinking)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        val prefDrinking = if (getUserPrefs().drinking.isNullOrEmpty()) {
            null
        } else {
            getUserPrefs().drinking
        }

        if (viewModel.selectedDrinking.get() != prefDrinking) {
            viewModel.isButtonEnabled.set(true)
        } else {
            viewModel.isButtonEnabled.set(false)
        }
    }
}
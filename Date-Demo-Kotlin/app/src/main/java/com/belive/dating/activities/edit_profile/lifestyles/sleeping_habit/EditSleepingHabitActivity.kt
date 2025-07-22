package com.belive.dating.activities.edit_profile.lifestyles.sleeping_habit

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
import com.belive.dating.databinding.ActivityEditSleepingHabitBinding
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
 * Activity for editing the user's sleeping habit.
 *
 * This activity allows the user to select their sleeping habit from a list and save the changes.
 * It interacts with the [EditSleepingHabitViewModel] to manage the data and uses [EditSleepingHabitAdapter]
 * to display the list of sleeping habits.
 *
 * Implements [NetworkReceiverActivity] to handle network connectivity changes and
 * [EditSleepingHabitAdapter.OnSleepingHabitChangeListener] to listen for item clicks in the RecyclerView.
 */
class EditSleepingHabitActivity : NetworkReceiverActivity(), EditSleepingHabitAdapter.OnSleepingHabitChangeListener {

    val binding by lazy {
        ActivityEditSleepingHabitBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditSleepingHabitViewModel

    val adapter by lazy {
        EditSleepingHabitAdapter(this, viewModel)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditSleepingHabitActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditSleepingHabitActivity::class.java.simpleName)
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
            saveSleepingHabit()
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        val prefSleepingHabit = if (getUserPrefs().sleepingHabit.isNullOrEmpty()) {
            null
        } else {
            getUserPrefs().sleepingHabit
        }
        viewModel.selectedSleepingHabit.set(prefSleepingHabit)

        if (binding.rvSleepingHabit.layoutManager == null) {
            binding.rvSleepingHabit.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvSleepingHabit.adapter == null) {
            binding.rvSleepingHabit.adapter = adapter
        }
    }

    private fun saveSleepingHabit() {
        lifecycleScope.launch(Dispatchers.IO) {

            viewModel.saveSleepingHabit().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditSleepingHabitActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditSleepingHabitActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditSleepingHabitActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditSleepingHabitActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().sleepingHabit = it.data.user.sleepHabit
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_SLEEPING_HABIT, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditSleepingHabitActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT)
                                    .show()
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

                startActivity(Intent(this@EditSleepingHabitActivity, SignInActivity::class.java))
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

    override fun onSleepingHabitClick(position: Int) {
        val sleepingHabit = viewModel.sleepingHabitList[position]

        val previousSelectedPosition = viewModel.sleepingHabitList.indexOf(viewModel.selectedSleepingHabit.get())

        if (viewModel.selectedSleepingHabit.get() == sleepingHabit) {
            viewModel.selectedSleepingHabit.set(null)
        } else {
            viewModel.selectedSleepingHabit.set(sleepingHabit)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        val prefSleepingHabit = if (getUserPrefs().sleepingHabit.isNullOrEmpty()) {
            null
        } else {
            getUserPrefs().sleepingHabit
        }

        if (viewModel.selectedSleepingHabit.get() != prefSleepingHabit) {
            viewModel.isButtonEnabled.set(true)
        } else {
            viewModel.isButtonEnabled.set(false)
        }
    }
}
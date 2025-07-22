package com.belive.dating.activities.edit_profile.sexual_orientation

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
import com.belive.dating.api.user.models.user.Orientation
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityEditSexualOrientationBinding
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
 * Activity for editing the user's sexual orientation.
 *
 * This activity allows the user to view and select their sexual orientation from a list.
 * It handles fetching orientations, saving the selected orientation, and managing user authentication.
 *
 * Implements [NetworkReceiverActivity] to handle network connectivity changes and
 * [EditSexualOrientationAdapter.OnOrientationChangeListener] to listen for orientation selection changes in the adapter.
 */
class EditSexualOrientationActivity : NetworkReceiverActivity(), EditSexualOrientationAdapter.OnOrientationChangeListener {

    val binding by lazy {
        ActivityEditSexualOrientationBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditSexualOrientationViewModel

    val adapter by lazy {
        EditSexualOrientationAdapter(this, viewModel)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditSexualOrientationActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditSexualOrientationActivity::class.java.simpleName)
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
            saveSexualOrientation()
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.rvSexualOrientations.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvSexualOrientations.adapter = adapter
    }

    private fun saveSexualOrientation() {
        lifecycleScope.launch(Dispatchers.IO) {

            val orientation = viewModel.sexualOrientationList.get()!![viewModel.selectedSexualOrientation.get()!!]

            viewModel.saveSexualOrientation(orientation.id).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditSexualOrientationActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditSexualOrientationActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(
                                this@EditSexualOrientationActivity,
                                "Your session has expired, Please log in again.",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditSexualOrientationActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                if (it.data.user.orientation != null) {
                                    getUserPrefs().sexualOrientation = Orientation(it.data.user.orientation.id, it.data.user.orientation.name)
                                    getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                    EventManager.postEvent(Event(EventConstants.UPDATE_SEXUAL_ORIENTATION, null))
                                    EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                    onBackPressedDispatcher.onBackPressed()
                                } else {
                                    Toast.makeText(
                                        this@EditSexualOrientationActivity,
                                        "Something went wrong, please try again...!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@EditSexualOrientationActivity,
                                    "Something went wrong, please try again...!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getAllSexualOrientations() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllSexualOrientations().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditSexualOrientationActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditSexualOrientationActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditSexualOrientationActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditSexualOrientationActivity, it.message, Toast.LENGTH_SHORT).show()

                            onBackPressedDispatcher.onBackPressed()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data?.sexualOrientationList != null) {
                                viewModel.sexualOrientationList.set(ArrayList(it.data.sexualOrientationList))

                                val selectedPosition =
                                    viewModel.sexualOrientationList.get()?.indexOfFirst { it.id == getUserPrefs().sexualOrientation?.id }

                                if (selectedPosition != -1) {
                                    viewModel.selectedSexualOrientation.set(selectedPosition)
                                }

                                adapter.asyncListDiffer.submitList((it.data.sexualOrientationList).toMutableList())
                            } else {
                                Toast.makeText(this@EditSexualOrientationActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()

                                onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    }
                }
            }
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

                startActivity(Intent(this@EditSexualOrientationActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
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

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

        if (viewModel.sexualOrientationList.get() == null) {
            getAllSexualOrientations()
        } else {
            adapter.asyncListDiffer.submitList(viewModel.sexualOrientationList.get())
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

            if (viewModel.sexualOrientationList.get() == null) {
                getAllSexualOrientations()
            } else {
                adapter.asyncListDiffer.submitList(viewModel.sexualOrientationList.get())
            }
        }
    }

    override fun onOrientationClick(position: Int) {
        if (getUserPrefs().sexualOrientation?.id == viewModel.sexualOrientationList.get()?.get(position)?.id) {
            viewModel.isButtonEnabled.set(false)
        } else {
            viewModel.isButtonEnabled.set(true)
        }
    }
}
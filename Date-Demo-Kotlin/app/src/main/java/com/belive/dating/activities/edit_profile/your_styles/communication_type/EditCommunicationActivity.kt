package com.belive.dating.activities.edit_profile.your_styles.communication_type

import android.R
import android.content.Intent
import android.os.Bundle
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
import com.belive.dating.databinding.ActivityEditCommunicationBinding
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
 * Activity for editing user's preferred communication types.
 *
 * This activity allows the user to select up to three communication types from a list.
 * The selected types are saved to the user's preferences and the server.
 *
 * Key functionalities:
 *  - Displays a list of communication types using [CommunicationAdapter].
 *  - Allows the user to select/deselect communication types by clicking on them.
 *  - Enforces a limit of three selected communication types.
 *  - Saves the selected communication types to the server and updates local preferences.
 *  - Handles back navigation and swipe gestures.
 *  - Integrates with Mixpanel for activity tracking.
 *  - Handles network availability changes.
 *  - Manages user authentication and sign-out.
 *  - Restores and saves state using the ViewModel.
 *
 *  @property binding The view binding for this activity.
 *  @property viewModel The ViewModel instance for managing communication data and logic.
 *  @property adapter The adapter for displaying the list of communication types.
 */
class EditCommunicationActivity : NetworkReceiverActivity(), CommunicationAdapter.CommunicationClickListener {

    val binding by lazy {
        ActivityEditCommunicationBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditCommunicationViewModel

    val adapter by lazy {
        CommunicationAdapter(viewModel, this)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditCommunicationActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditCommunicationActivity::class.java.simpleName)
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

        setSystemBarColors(getColorFromAttr(R.attr.windowBackground))

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

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            saveCommunicationTypes()
        }
    }

    private fun initViews() {
        val communicationList = arrayListOf<String>()
        getUserPrefs().communicationTypes?.let { communicationList.addAll(it) }

        viewModel.selectedCommunicationList.set(communicationList)
        viewModel.communicationCount.set(communicationList.size)

        if (binding.rvCommunicationType.layoutManager == null) {
            binding.rvCommunicationType.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvCommunicationType.adapter == null) {
            binding.rvCommunicationType.adapter = adapter
        }
    }

    private fun saveCommunicationTypes() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveCommunicationTypes().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditCommunicationActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditCommunicationActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditCommunicationActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditCommunicationActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().communicationTypes = if (it.data.user.communicationType.isNullOrEmpty()) {
                                    null
                                } else if (it.data.user.communicationType.contains(",")) {
                                    it.data.user.communicationType.split(",").toList()
                                } else {
                                    listOf(it.data.user.communicationType)
                                }
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_COMMUNICATIONS, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditCommunicationActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT)
                                    .show()
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

                startActivity(Intent(this@EditCommunicationActivity, SignInActivity::class.java))
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

    override fun onCommunicationClick(position: Int) {
        val communicationType = viewModel.communicationList[position]

        var isValid = false

        if (viewModel.selectedCommunicationList.get()?.contains(communicationType) == true) {
            viewModel.selectedCommunicationList.set(viewModel.selectedCommunicationList.get()!!.apply {
                remove(communicationType)
            })

            adapter.notifyItemChanged(position)

            if (viewModel.selectedCommunicationList.get()!!.size == getUserPrefs().communicationTypes?.size) {
                viewModel.selectedCommunicationList.get()?.forEach {
                    if (getUserPrefs().communicationTypes?.contains(it) == false) {
                        isValid = true
                    }
                }
            } else {
                isValid = true
            }
        } else {
            if (viewModel.selectedCommunicationList.get()!!.size >= 3) {
                Toast.makeText(this, "Maximum 3 communication types can be selected", Toast.LENGTH_SHORT).show()

                if (viewModel.selectedCommunicationList.get()!!.size == getUserPrefs().communicationTypes?.size) {
                    viewModel.selectedCommunicationList.get()?.forEach {
                        if (getUserPrefs().communicationTypes?.contains(it) == false) {
                            isValid = true
                            return@forEach
                        }
                    }
                } else {
                    isValid = true
                }
            } else {
                viewModel.selectedCommunicationList.set(viewModel.selectedCommunicationList.get()!!.apply {
                    add(communicationType)
                })

                adapter.notifyItemChanged(position)

                if (viewModel.selectedCommunicationList.get()!!.size == getUserPrefs().communicationTypes?.size) {
                    viewModel.selectedCommunicationList.get()?.forEach {
                        if (getUserPrefs().communicationTypes?.contains(it) == false) {
                            isValid = true
                            return@forEach
                        }
                    }
                } else {
                    isValid = true
                }
            }
        }

        viewModel.communicationCount.set(viewModel.selectedCommunicationList.get()!!.size)
        viewModel.isButtonEnabled.set(isValid)
    }
}
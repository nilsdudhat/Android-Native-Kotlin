package com.belive.dating.activities.edit_profile.your_styles.love_type

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
import com.belive.dating.databinding.ActivityEditLoveTypeBinding
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
 * Activity for editing the user's love types.
 *
 * This activity allows users to select and save their preferred love types. It interacts with
 * the [EditLoveTypeViewModel] to manage data and updates the user's preferences accordingly.
 * It also handles network connectivity changes, user authentication, and UI interactions.
 *
 *  Key Features:
 * - Displays a list of love types for the user to choose from.
 * - Allows the user to select up to 3 love types.
 * - Saves the selected love types to the user's preferences.
 * - Handles back navigation and saves/restores state.
 */
class EditLoveTypeActivity : NetworkReceiverActivity(), LoveTypeAdapter.LoveTypeClickListener {

    val binding by lazy {
        ActivityEditLoveTypeBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditLoveTypeViewModel

    val adapter by lazy {
        LoveTypeAdapter(viewModel, this)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditLoveTypeActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditLoveTypeActivity::class.java.simpleName)
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
            saveLoveTypes()
        }
    }

    private fun initViews() {
        val loveTypeList = arrayListOf<String>()
        getUserPrefs().loveTypes?.let { loveTypeList.addAll(it) }

        viewModel.selectedLoveTypeList.set(loveTypeList)
        viewModel.loveTypeCount.set(loveTypeList.size)

        if (binding.rvLoveType.layoutManager == null) {
            binding.rvLoveType.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvLoveType.adapter == null) {
            binding.rvLoveType.adapter = adapter
        }
    }

    private fun saveLoveTypes() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveLoveTypes().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditLoveTypeActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditLoveTypeActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditLoveTypeActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditLoveTypeActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().loveTypes = if (it.data.user.loveType.isNullOrEmpty()) {
                                    null
                                } else if (it.data.user.loveType.contains(",")) {
                                    it.data.user.loveType.split(",").toList()
                                } else {
                                    listOf(it.data.user.loveType)
                                }
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_LOVE_TYPES, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditLoveTypeActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
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

                startActivity(Intent(this@EditLoveTypeActivity, SignInActivity::class.java))
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

    override fun onLoveTypeClick(position: Int) {
        val loveType = viewModel.loveTypeList[position]

        var isValid = false

        if (viewModel.selectedLoveTypeList.get()?.contains(loveType) == true) {
            viewModel.selectedLoveTypeList.set(viewModel.selectedLoveTypeList.get()!!.apply {
                remove(loveType)
            })

            adapter.notifyItemChanged(position)

            if (viewModel.selectedLoveTypeList.get()!!.size == getUserPrefs().loveTypes?.size) {
                viewModel.selectedLoveTypeList.get()?.forEach {
                    if (getUserPrefs().loveTypes?.contains(it) == false) {
                        isValid = true
                    }
                }
            } else {
                isValid = true
            }
        } else {
            if (viewModel.selectedLoveTypeList.get()!!.size >= 3) {
                Toast.makeText(this, "Maximum 3 love types can be selected", Toast.LENGTH_SHORT).show()

                if (viewModel.selectedLoveTypeList.get()!!.size == getUserPrefs().loveTypes?.size) {
                    viewModel.selectedLoveTypeList.get()?.forEach {
                        if (getUserPrefs().loveTypes?.contains(it) == false) {
                            isValid = true
                            return@forEach
                        }
                    }
                } else {
                    isValid = true
                }
            } else {
                viewModel.selectedLoveTypeList.set(viewModel.selectedLoveTypeList.get()!!.apply {
                    add(loveType)
                })

                adapter.notifyItemChanged(position)

                if (viewModel.selectedLoveTypeList.get()!!.size == getUserPrefs().loveTypes?.size) {
                    viewModel.selectedLoveTypeList.get()?.forEach {
                        if (getUserPrefs().loveTypes?.contains(it) == false) {
                            isValid = true
                            return@forEach
                        }
                    }
                } else {
                    isValid = true
                }
            }
        }

        viewModel.loveTypeCount.set(viewModel.selectedLoveTypeList.get()!!.size)
        viewModel.isButtonEnabled.set(isValid)
    }
}
package com.belive.dating.activities.edit_profile.lifestyles.pets

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
import com.belive.dating.databinding.ActivityEditPetsBinding
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
 * Activity for editing the user's pet selection.
 *
 * This activity allows users to select their pet from a list and save the selection.
 * It handles network connectivity changes, user preferences, and authentication.
 *
 * Key Features:
 * - Displays a list of available pets using a [FlexboxLayoutManager] for flexible arrangement.
 * - Allows users to select a pet via a [PetAdapter] and highlights the selected pet.
 * - Saves the selected pet to user preferences using [getUserPrefs] and updates the backend service through [EditPetViewModel.savePets].
 * - Handles network connectivity changes using [NetworkReceiverActivity], ensuring a smooth user experience.
 * - Implements [PetAdapter.PetClickListener] to handle pet selection events.
 * - Uses Koin for dependency injection of the [EditPetViewModel] and [AuthenticationHelper].
 * - Utilizes coroutines and Flows for asynchronous operations like saving pets and handling authentication.
 * - Tracks user activity using MixPanel for analytics.
 *
 * The activity flow includes:
 * 1. Displaying a list of pets retrieved from the [EditPetViewModel].
 * 2. Allowing the user to select a pet from the list.
 * 3. Highlighting the selected pet in the UI.
 * 4. Saving the selected pet to user preferences and updating the backend when the "Save" button is clicked.
 */
class EditPetActivity : NetworkReceiverActivity(), PetAdapter.PetClickListener {

    val binding by lazy {
        ActivityEditPetsBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditPetViewModel

    val adapter by lazy {
        PetAdapter(viewModel, this)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditPetActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditPetActivity::class.java.simpleName)
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
            savePets()
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefPet = if (getUserPrefs().pet.isNullOrEmpty()) {
            null
        } else {
            getUserPrefs().pet
        }
        viewModel.selectedPet.set(prefPet)

        if (binding.rvPets.layoutManager == null) {
            val layoutManager = FlexboxLayoutManager(this)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            binding.rvPets.layoutManager = layoutManager
        }
        if (binding.rvPets.adapter == null) {
            binding.rvPets.adapter = adapter
        }
    }

    private fun savePets() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.savePets().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditPetActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditPetActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditPetActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditPetActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().pet = if (it.data.user.pet.isNullOrEmpty()) {
                                    null
                                } else {
                                    it.data.user.pet
                                }
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_PET, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditPetActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
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

                startActivity(Intent(this@EditPetActivity, SignInActivity::class.java))
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

    override fun onPetClick(position: Int) {
        val pet = viewModel.petList[position]

        val previousSelectedPosition = viewModel.petList.indexOf(viewModel.selectedPet.get())

        if (viewModel.selectedPet.get() == pet) {
            viewModel.selectedPet.set(null)
        } else {
            viewModel.selectedPet.set(pet)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        val prefPet = if (getUserPrefs().pet.isNullOrEmpty()) {
            null
        } else {
            getUserPrefs().pet
        }

        if (viewModel.selectedPet.get() != prefPet) {
            viewModel.isButtonEnabled.set(true)
        } else {
            viewModel.isButtonEnabled.set(false)
        }
    }
}
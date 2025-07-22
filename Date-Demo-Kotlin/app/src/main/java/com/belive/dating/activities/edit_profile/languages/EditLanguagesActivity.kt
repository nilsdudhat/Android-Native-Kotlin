package com.belive.dating.activities.edit_profile.languages

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
import com.belive.dating.databinding.ActivityEditLanguagesBinding
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
 * Activity for editing the user's known languages.
 *
 * This activity allows the user to select and save their known languages. It handles
 * fetching the list of available languages, displaying them in a RecyclerView, managing
 * user selections, and saving the updated language preferences. It also includes
 * error handling for network issues, authentication failures, and admin blocks.
 *
 * Key features:
 * - Displays a list of languages with checkboxes for selection.
 * - Pre-selects languages the user has already chosen.
 * - Allows users to update their language preferences.
 * - Handles network connectivity changes and refreshes the language list accordingly.
 * - Provides feedback to the user via Toasts for success, errors, and authentication issues.
 * - Uses a ViewModel (EditLanguagesViewModel) to manage UI-related data and operations.
 * - Implements an adapter (EditLanguageAdapter) for the RecyclerView to display languages.
 * - Utilizes Kotlin coroutines for asynchronous operations like fetching and saving data.
 * - Includes a custom loading dialog (LoadingDialog) to indicate ongoing operations.
 * - Handles back button presses to navigate back and apply a transition.
 * - Integrates with dependency injection (Koin) for ViewModel, AuthenticationHelper, and other dependencies.
 *
 * Functionality breakdown:
 * - **onCreate**: Initializes the activity, sets up data binding, observes network state, initializes views, and sets click listeners.
 * - **clickListeners**:  Handles clicks for back navigation and saving language preferences.
 * - **saveLanguages**: Asynchronously saves the user's selected languages using the ViewModel. Displays loading dialog, handles success, error, and authentication states (admin block, sign out), updates user preferences upon success, and navigates back.
 * - **initViews**: Placeholder for any initial view setup logic.
 * - **setUpRecyclerView**: Configures the RecyclerView with a LinearLayoutManager and the custom EditLanguageAdapter.
 * - **getAllInterests**: Asynchronously fetches the list of all languages using the ViewModel.  Displays loading dialog, handles success, error, and authentication states.  Pre-selects user's existing languages in the list, updates the ViewModel's live data, and submits the list to the adapter.
 */
class EditLanguagesActivity : NetworkReceiverActivity(), EditLanguageAdapter.OnLanguageListener {

    val binding by lazy {
        ActivityEditLanguagesBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditLanguagesViewModel

    val adapter by lazy {
        EditLanguageAdapter(this)
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
            listenEvents()
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
            finish()
            swipeLeft()
        }

        binding.btnSave.setOnClickListener {
            saveLanguages()
        }
    }

    private fun saveLanguages() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveKnownLanguages(adapter.getSelected()).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditLanguagesActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditLanguagesActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditLanguagesActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditLanguagesActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().knownLanguages = if (it.data.user.knownLanguage.isNullOrEmpty()) {
                                    null
                                } else if (it.data.user.knownLanguage.contains(",")) {
                                    it.data.user.knownLanguage.split(",").toList()
                                } else {
                                    listOf(it.data.user.knownLanguage)
                                }
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_KNOWN_LANGUAGES, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditLanguagesActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        if (binding.rvInterests.layoutManager == null) {
            binding.rvInterests.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvInterests.adapter == null) {
            binding.rvInterests.adapter = adapter
        }
    }

    private fun getAllLanguages() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllLanguages().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditLanguagesActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditLanguagesActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditLanguagesActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditLanguagesActivity, it.message, Toast.LENGTH_SHORT).show()

                            onBackPressedDispatcher.onBackPressed()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data?.languageList != null) {
                                getUserPrefs().knownLanguages?.forEach { language ->
                                    val selectedInterestPosition = it.data.languageList.indexOfFirst { it.name == language }
                                    it.data.languageList[selectedInterestPosition].isChecked = true
                                }

                                viewModel.languagesList.set(ArrayList(it.data.languageList))
                                viewModel.languagesCount.set(getUserPrefs().knownLanguages?.size)
                                adapter.asyncListDiffer.submitList((it.data.languageList).toMutableList())
                            } else {
                                Toast.makeText(this@EditLanguagesActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()

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

                startActivity(Intent(this@EditLanguagesActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

        if (viewModel.languagesList.get() == null) {
            getAllLanguages()
        } else {
            adapter.asyncListDiffer.submitList(viewModel.languagesList.get())
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

            if (viewModel.languagesList.get() == null) {
                getAllLanguages()
            } else {
                adapter.asyncListDiffer.submitList(viewModel.languagesList.get())
            }
        }
    }

    override fun onLanguageClick(list: ArrayList<String>) {
        viewModel.languagesCount.set(list.size)
        viewModel.selectedLanguages.set(list)

        val myLanguages = getUserPrefs().knownLanguages

        if (list.size == myLanguages?.size) {
            var isMismatchFound = false

            list.forEach { id ->
                if (!myLanguages.contains(id)) {
                    isMismatchFound = true
                    return@forEach
                }
            }

            viewModel.isButtonEnabled.set(isMismatchFound)
        } else {
            viewModel.isButtonEnabled.set(true)
        }
    }
}
package com.belive.dating.activities.edit_profile.basics.education

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
import com.belive.dating.api.user.models.user.Basic
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityEditEducationBinding
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
 * Activity for editing the user's education.
 *
 * This activity allows users to view a list of available education options and select one to update their profile.
 * It uses a RecyclerView to display the options and interacts with a ViewModel to manage data and network operations.
 *
 * Key Features:
 * - Displays a list of education options fetched from the server.
 * - Allows the user to select an education option.
 * - Saves the selected education to the user's profile.
 * - Handles network connectivity changes and automatically re-fetches data when the internet becomes available.
 * - Implements back navigation and custom animations.
 * - Tracks user interactions with MixPanel for analytics.
 * - Handles user authentication and sign-out scenarios.
 *
 * @see NetworkReceiverActivity Base class for activities that need to monitor network connectivity.
 * @see EditEducationAdapter Adapter for the RecyclerView displaying the education options.
 * @see EditEducationViewModel ViewModel responsible for managing education data and network calls.
 */
class EditEducationActivity : NetworkReceiverActivity(), EditEducationAdapter.OnEducationChangeListener {

    val binding by lazy {
        ActivityEditEducationBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditEducationViewModel

    val adapter by lazy {
        EditEducationAdapter(this, viewModel)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditEducationActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditEducationActivity::class.java.simpleName)
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
            saveEducation()
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.rvEducation.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvEducation.adapter = adapter
    }

    private fun saveEducation() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveEducation(viewModel.selectedEducation.get()).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditEducationActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditEducationActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditEducationActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditEducationActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().education =
                                    if (it.data.user.education == null) null else Basic(
                                        it.data.user.education.id,
                                        it.data.user.education.name,
                                        it.data.user.education.icon,
                                    )
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_EDUCATION, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditEducationActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getAllEducations() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllEducation().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditEducationActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditEducationActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditEducationActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditEducationActivity, it.message, Toast.LENGTH_SHORT).show()

                            onBackPressedDispatcher.onBackPressed()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (!it.data?.educationList.isNullOrEmpty()) {
                                viewModel.educationList.set(it.data.educationList.let { it1 -> ArrayList(it1) })

                                viewModel.selectedEducation.set(getUserPrefs().education?.id)

                                adapter.asyncListDiffer.submitList((it.data.educationList).toMutableList())
                            } else {
                                Toast.makeText(this@EditEducationActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()

                                onBackPressedDispatcher.onBackPressed()
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

                startActivity(Intent(this@EditEducationActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

        if (viewModel.educationList.get() == null) {
            getAllEducations()
        } else {
            adapter.asyncListDiffer.submitList(viewModel.educationList.get())
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

            if (viewModel.educationList.get() == null) {
                getAllEducations()
            } else {
                adapter.asyncListDiffer.submitList(viewModel.educationList.get())
            }
        }
    }

    override fun onEducationClick(position: Int) {
        val educationData = viewModel.educationList.get()!![position]

        val previousSelectedPosition = viewModel.educationList.get()!!.indexOfFirst { viewModel.selectedEducation.get() == it.id }

        if (viewModel.selectedEducation.get() == educationData.id) {
            viewModel.selectedEducation.set(null)
        } else {
            viewModel.selectedEducation.set(educationData.id)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        val prefEducation = if (getUserPrefs().education == null) {
            null
        } else {
            getUserPrefs().education
        }

        if (viewModel.selectedEducation.get() != prefEducation?.id) {
            viewModel.isButtonEnabled.set(true)
        } else {
            viewModel.isButtonEnabled.set(false)
        }
    }
}
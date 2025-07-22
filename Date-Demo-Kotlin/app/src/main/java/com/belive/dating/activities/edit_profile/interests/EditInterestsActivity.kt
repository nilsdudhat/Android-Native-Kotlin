package com.belive.dating.activities.edit_profile.interests

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityEditInterestsBinding
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
 * Activity for editing user interests.
 *
 * This activity allows users to select their interests from a list and save them.
 * It extends [NetworkReceiverActivity] to handle network connectivity changes and
 * implements [EditInterestAdapter.OnInterestListener] to listen for interest selection events.
 *
 * Key functionalities include:
 *  - Displaying a list of interests fetched from the server.
 *  - Allowing users to select multiple interests with a minimum requirement of 6.
 *  - Saving the selected interests to the user's profile on the server.
 *  - Handling network connectivity changes to ensure data availability.
 *  - Implementing user authentication and sign-out functionality in case of admin block or duplicate login.
 *  - Tracking user interactions using Mixpanel analytics.
 *
 * The activity uses a [FlexboxLayoutManager] to display interests in a flexible, wrapping grid layout.
 * It interacts with an [EditInterestsViewModel] to manage data and business logic, and uses an [EditInterestAdapter]
 * to display the list of interests and handle user interactions.  The selected interests are stored as a comma separated string
 * in the viewModel.  The activity observes changes to the network connectivity and will fetch interests on initial connection or when connectivity changes.
 *
 *  @property binding The view binding for the activity.  Initialized lazily.
 *  @property viewModel The view model associated with the activity, providing data and functionality. Injected via Koin.
 *  @property adapter The adapter used to display the list of interests in the RecyclerView. Initialized lazily.
 */
class EditInterestsActivity : NetworkReceiverActivity(), EditInterestAdapter.OnInterestListener {

    val binding by lazy {
        ActivityEditInterestsBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditInterestsViewModel

    val adapter by lazy {
        EditInterestAdapter(this)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditInterestsActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditInterestsActivity::class.java.simpleName)
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

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            saveMyInterests()
        }
    }

    private fun saveMyInterests() {
        lifecycleScope.launch(Dispatchers.IO) {

            val selectedInterests = TextUtils.join(",", viewModel.selectedInterests.get()!!)

            viewModel.saveInterests(selectedInterests).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditInterestsActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditInterestsActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(
                                this@EditInterestsActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT
                            ).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditInterestsActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                if (!it.data.user.myInterests.isNullOrEmpty()) {
                                    getUserPrefs().myInterests = if (it.data.user.myInterests.isEmpty()) {
                                        null
                                    } else if (it.data.user.myInterests.contains(",")) {
                                        it.data.user.myInterests.split(",").toList()
                                    } else {
                                        listOf(it.data.user.myInterests)
                                    }
                                    getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                    EventManager.postEvent(Event(EventConstants.UPDATE_INTERESTS, null))
                                    EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                    onBackPressedDispatcher.onBackPressed()
                                } else {
                                    Toast.makeText(
                                        this@EditInterestsActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(this@EditInterestsActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
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
            val layoutManager = FlexboxLayoutManager(this)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER

            binding.rvInterests.layoutManager = layoutManager
        }

        if (binding.rvInterests.adapter == null) {
            binding.rvInterests.adapter = adapter
        }
    }

    private fun getAllInterests() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllInterests().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditInterestsActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditInterestsActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditInterestsActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditInterestsActivity, it.message, Toast.LENGTH_SHORT).show()

                            onBackPressedDispatcher.onBackPressed()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data?.data != null) {
                                val selectedInterests = getUserPrefs().myInterests
                                selectedInterests?.forEach { id ->
                                    val selectedInterestPosition = it.data.data.indexOfFirst { it.id == id.trim().toInt() }
                                    it.data.data[selectedInterestPosition].isChecked = true
                                }

                                viewModel.interestsList.set(ArrayList(it.data.data))
                                viewModel.interestsCount.set(selectedInterests?.size)
                                adapter.asyncListDiffer.submitList((it.data.data).toMutableList())
                            } else {
                                Toast.makeText(this@EditInterestsActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()

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

                startActivity(Intent(this@EditInterestsActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        if (viewModel.interestsList.get() == null) {
            getAllInterests()
        } else {
            adapter.asyncListDiffer.submitList(viewModel.interestsList.get())
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            if (viewModel.interestsList.get() == null) {
                getAllInterests()
            } else {
                adapter.asyncListDiffer.submitList(viewModel.interestsList.get())
            }
        }
    }

    override fun onInterestClick(list: ArrayList<Int>) {
        viewModel.interestsCount.set(list.size)
        viewModel.selectedInterests.set(list)

        if (list.size < 6) {
            viewModel.isButtonEnabled.set(false)
        } else {
            var isMismatchFound = false

            val myInterests = getUserPrefs().myInterests

            if (list.size == myInterests?.size) {
                list.forEach { id ->
                    if (!myInterests.contains(id.toString())) {
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
}
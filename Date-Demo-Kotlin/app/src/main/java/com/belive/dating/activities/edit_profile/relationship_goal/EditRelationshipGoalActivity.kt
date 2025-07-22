package com.belive.dating.activities.edit_profile.relationship_goal

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
import com.belive.dating.api.user.models.user.RelationshipGoal
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityEditRelationshipGoalBinding
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
 * Activity for editing the user's relationship goal.
 *
 * This activity allows the user to view a list of available relationship goals and select a new one.
 * It interacts with the [EditRelationshipGoalViewModel] to manage data and network operations.
 * The activity also handles user authentication, internet connectivity changes, and UI interactions.
 *
 * Key features:
 *  - Displays a list of relationship goals fetched from the server.
 *  - Allows the user to select a relationship goal.
 *  - Saves the selected relationship goal to the user's profile.
 *  - Handles network connectivity changes and automatically retries fetching data.
 */
class EditRelationshipGoalActivity : NetworkReceiverActivity(), EditRelationshipGoalAdapter.OnRelationshipGoalListener {

    val binding by lazy {
        ActivityEditRelationshipGoalBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditRelationshipGoalViewModel

    val adapter by lazy {
        EditRelationshipGoalAdapter(this, viewModel)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditRelationshipGoalActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditRelationshipGoalActivity::class.java.simpleName)
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
            if (viewModel.selectedRelationshipGoal.get() == null) {
                Toast.makeText(this, "Please select a relationship goal", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.saveRelationshipGoal(viewModel.selectedRelationshipGoal.get()!!).collectLatest {
                        when (it.status) {
                            Status.LOADING -> {
                                LoadingDialog.show(this@EditRelationshipGoalActivity)
                            }

                            Status.ADMIN_BLOCKED -> {
                                Toast.makeText(
                                    this@EditRelationshipGoalActivity,
                                    "Admin has blocked you, because of security reasons.",
                                    Toast.LENGTH_SHORT,
                                ).show()

                                authOut()
                            }

                            Status.SIGN_OUT -> {
                                Toast.makeText(
                                    this@EditRelationshipGoalActivity,
                                    "Your session has expired, Please log in again.",
                                    Toast.LENGTH_SHORT,
                                )
                                    .show()

                                authOut()
                            }

                            Status.ERROR -> {
                                LoadingDialog.hide()

                                Toast.makeText(this@EditRelationshipGoalActivity, it.message, Toast.LENGTH_SHORT).show()
                            }

                            Status.SUCCESS -> {
                                LoadingDialog.hide()

                                if (it.data != null) {
                                    if (it.data.user.relationshipGoal != null) {
                                        getUserPrefs().relationshipGoal = RelationshipGoal(
                                            it.data.user.relationshipGoal.icon,
                                            it.data.user.relationshipGoal.id,
                                            it.data.user.relationshipGoal.name
                                        )
                                        getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                        EventManager.postEvent(Event(EventConstants.UPDATE_RELATIONSHIP_GOAL, null))
                                        EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                        onBackPressedDispatcher.onBackPressed()
                                    } else {
                                        Toast.makeText(
                                            this@EditRelationshipGoalActivity,
                                            "Something went wrong, please try again...!",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this@EditRelationshipGoalActivity,
                                        "Something went wrong, please try again...!",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.rvRelationshipGoal.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvRelationshipGoal.adapter = adapter
    }

    private fun getAllRelationshipGoal() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllRelationshipGoals().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditRelationshipGoalActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditRelationshipGoalActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditRelationshipGoalActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditRelationshipGoalActivity, it.message, Toast.LENGTH_SHORT).show()

                            onBackPressedDispatcher.onBackPressed()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data?.data != null) {
                                viewModel.relationshipGoalList.set(ArrayList(it.data.data))

                                val selectedPosition =
                                    viewModel.relationshipGoalList.get()?.indexOfFirst { it.id == getUserPrefs().relationshipGoal?.id }

                                if (selectedPosition != -1) {
                                    viewModel.selectedRelationshipGoal.set(getUserPrefs().relationshipGoal?.id)
                                }

                                adapter.asyncListDiffer.submitList((it.data.data).toMutableList())
                            } else {
                                Toast.makeText(this@EditRelationshipGoalActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()

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

                startActivity(Intent(this@EditRelationshipGoalActivity, SignInActivity::class.java))
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

        if (viewModel.relationshipGoalList.get() == null) {
            getAllRelationshipGoal()
        } else {
            adapter.asyncListDiffer.submitList(viewModel.relationshipGoalList.get())
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

            if (viewModel.relationshipGoalList.get() == null) {
                getAllRelationshipGoal()
            } else {
                adapter.asyncListDiffer.submitList(viewModel.relationshipGoalList.get())
            }
        } else {
            adapter.asyncListDiffer.submitList(viewModel.relationshipGoalList.get())
        }
    }

    override fun onRelationshipGoalClick(position: Int) {
        val relationshipGoal = viewModel.relationshipGoalList.get()!![position]

        val previousSelectedPosition = viewModel.relationshipGoalList.get()!!.indexOfFirst { viewModel.selectedRelationshipGoal.get() == it.id }

        if (viewModel.selectedRelationshipGoal.get() == relationshipGoal.id) {
            viewModel.selectedRelationshipGoal.set(null)
        } else {
            viewModel.selectedRelationshipGoal.set(relationshipGoal.id)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        val prefRelationshipGoal = if (getUserPrefs().relationshipGoal == null) {
            null
        } else {
            getUserPrefs().relationshipGoal
        }

        if (viewModel.selectedRelationshipGoal.get() != prefRelationshipGoal?.id) {
            viewModel.isButtonEnabled.set(true)
        } else {
            viewModel.isButtonEnabled.set(false)
        }
    }
}
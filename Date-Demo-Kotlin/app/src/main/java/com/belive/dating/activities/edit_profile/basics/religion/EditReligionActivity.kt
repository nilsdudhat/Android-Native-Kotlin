package com.belive.dating.activities.edit_profile.basics.religion

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
import com.belive.dating.databinding.ActivityEditReligionBinding
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

class EditReligionActivity : NetworkReceiverActivity(), EditReligionAdapter.OnReligionChangeListener {

    val binding by lazy {
        ActivityEditReligionBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditReligionViewModel

    val adapter by lazy {
        EditReligionAdapter(this, viewModel)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditReligionActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditReligionActivity::class.java.simpleName)
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
            saveReligion()
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        if (binding.rvReligion.layoutManager == null) {
            binding.rvReligion.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvReligion.adapter == null) {
            binding.rvReligion.adapter = adapter
        }
    }

    private fun saveReligion() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveReligion(viewModel.selectedReligion.get()).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditReligionActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditReligionActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(
                                this@EditReligionActivity,
                                "Your session has expired, Please log in again.",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditReligionActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().religion = if (it.data.user.religion == null) null else Basic(
                                    it.data.user.religion.id,
                                    it.data.user.religion.name,
                                    it.data.user.religion.icon,
                                )
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_RELIGION, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditReligionActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getAllReligions() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllReligions().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditReligionActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditReligionActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditReligionActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditReligionActivity, it.message, Toast.LENGTH_SHORT).show()

                            onBackPressedDispatcher.onBackPressed()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data?.religionList != null) {
                                viewModel.religionList.set(ArrayList(it.data.religionList))

                                viewModel.selectedReligion.set(getUserPrefs().religion?.id)

                                adapter.asyncListDiffer.submitList((it.data.religionList).toMutableList())
                            } else {
                                Toast.makeText(this@EditReligionActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()

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

                startActivity(Intent(this@EditReligionActivity, SignInActivity::class.java))
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

        if (viewModel.religionList.get() == null) {
            getAllReligions()
        } else {
            adapter.asyncListDiffer.submitList(viewModel.religionList.get())
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

            if (viewModel.religionList.get() == null) {
                getAllReligions()
            } else {
                adapter.asyncListDiffer.submitList(viewModel.religionList.get())
            }
        }
    }

    override fun onReligionClick(position: Int) {
        val religion = viewModel.religionList.get()!![position]

        val previousSelectedPosition = viewModel.religionList.get()!!.indexOfFirst { viewModel.selectedReligion.get() == it.id }

        if (viewModel.selectedReligion.get() == religion.id) {
            viewModel.selectedReligion.set(null)
        } else {
            viewModel.selectedReligion.set(religion.id)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        val prefReligion = if (getUserPrefs().religion == null) {
            null
        } else {
            getUserPrefs().religion
        }

        if (viewModel.selectedReligion.get() != prefReligion?.id) {
            viewModel.isButtonEnabled.set(true)
        } else {
            viewModel.isButtonEnabled.set(false)
        }
    }
}
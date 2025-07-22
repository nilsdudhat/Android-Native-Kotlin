package com.belive.dating.activities.edit_profile.basics.zodiac

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
import com.belive.dating.api.user.models.user.Basic
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityEditZodiacBinding
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
 * Activity for editing the user's zodiac sign.
 *
 * This activity allows the user to view a list of zodiac signs and select a new one.
 * It interacts with the [EditZodiacViewModel] to manage data and network operations.
 * It also handles user authentication and network connectivity changes.
 */
class EditZodiacActivity : NetworkReceiverActivity(), EditZodiacAdapter.OnZodiacChangeListener {

    val binding by lazy {
        ActivityEditZodiacBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditZodiacViewModel

    val adapter by lazy {
        EditZodiacAdapter(this, viewModel)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(EditZodiacActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(EditZodiacActivity::class.java.simpleName)
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
            saveZodiac()
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER

        binding.rvZodiac.layoutManager = layoutManager
        binding.rvZodiac.adapter = adapter
    }

    private fun saveZodiac() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveZodiac(viewModel.selectedZodiac.get()).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditZodiacActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditZodiacActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(
                                this@EditZodiacActivity,
                                "Your session has expired, Please log in again.",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditZodiacActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().zodiac = if (it.data.user.zodiac == null) null else Basic(
                                    it.data.user.zodiac.id,
                                    it.data.user.zodiac.name,
                                    it.data.user.zodiac.icon,
                                )
                                getUserPrefs().completeProfilePercentage = it.data.user.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_ZODIAC, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(
                                    this@EditZodiacActivity,
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

    private fun getAllZodiacs() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllZodiac().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditZodiacActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(
                                this@EditZodiacActivity,
                                "Admin has blocked you, because of security reasons.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditZodiacActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@EditZodiacActivity, it.message, Toast.LENGTH_SHORT).show()

                            onBackPressedDispatcher.onBackPressed()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (!it.data?.zodiacList.isNullOrEmpty()) {
                                viewModel.zodiacList.set(it.data.zodiacList.let { it1 -> ArrayList(it1) })

                                viewModel.selectedZodiac.set(getUserPrefs().zodiac?.id)

                                adapter.asyncListDiffer.submitList((it.data.zodiacList).toMutableList())
                            } else {
                                Toast.makeText(this@EditZodiacActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()

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

                startActivity(Intent(this@EditZodiacActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

        if (viewModel.zodiacList.get() == null) {
            getAllZodiacs()
        } else {
            adapter.asyncListDiffer.submitList(viewModel.zodiacList.get())
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            ManageAds.showSmallNativeAd(SmallNativeGroup.Profile, binding.adSmallNative)

            if (viewModel.zodiacList.get() == null) {
                getAllZodiacs()
            } else {
                adapter.asyncListDiffer.submitList(viewModel.zodiacList.get())
            }
        }
    }

    override fun onZodiacClick(position: Int) {
        val zodiac = viewModel.zodiacList.get()!![position].id

        val previousSelectedPosition = viewModel.zodiacList.get()!!.indexOfFirst { viewModel.selectedZodiac.get() == it.id }

        if (viewModel.selectedZodiac.get() == zodiac) {
            viewModel.selectedZodiac.set(null)
        } else {
            viewModel.selectedZodiac.set(zodiac)
        }

        adapter.notifyItemChanged(position)
        if (previousSelectedPosition != -1) {
            adapter.notifyItemChanged(previousSelectedPosition)
        }

        val prefPet = if (getUserPrefs().zodiac == null) {
            null
        } else {
            getUserPrefs().zodiac
        }

        if (viewModel.selectedZodiac.get() != prefPet?.id) {
            viewModel.isButtonEnabled.set(true)
        } else {
            viewModel.isButtonEnabled.set(false)
        }
    }
}
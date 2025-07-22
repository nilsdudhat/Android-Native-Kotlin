package com.belive.dating.activities.filter.location

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.Observable
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.juky.squircleview.views.SquircleConstraintLayout
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.filter.location.search_location.SearchLocationActivity
import com.belive.dating.activities.paywalls.subscriptions.subscription.SubscriptionActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.BigNativeGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.api.user.models.my_locations.MyLocation
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityChangeLocationBinding
import com.belive.dating.di.filtersViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.dpToPx
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.swipeUp
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.current_location.getAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.context.unloadKoinModules

class ChangeLocationActivity : NetworkReceiverActivity(), LocationAdapter.OnLocationClickListener {

    val binding: ActivityChangeLocationBinding by lazy {
        ActivityChangeLocationBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: ChangeLocationViewModel

    val adapter: LocationAdapter by lazy {
        LocationAdapter(viewModel, this)
    }

    private val addLocationActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if ((result.data?.hasExtra("is_location_added") == true) && (result.data?.getBooleanExtra(
                    "is_location_added", false
                ) == true)
            ) {
                getCustomLocations(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(ChangeLocationActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(ChangeLocationActivity::class.java.simpleName)
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

        viewModel = tryKoinViewModel(listOf(filtersViewModel))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            listenEvents()

            observeNetwork()
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        initViews()

        clickListeners()

        observeData()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        ManageAds.showNativeSquareAd(BigNativeGroup.Filter, binding.adNative)

        viewModel.selectedLocation.set(
            Triple(
                intent.getDoubleExtra("latitude", 0.0),
                intent.getDoubleExtra("longitude", 0.0),
                intent.getStringExtra("address"),
            )
        )

        setCurrentLocation()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isDeleteView.get() == true) {
                    viewModel.isDeleteView.set(false)
                    viewModel.isDeleteEnabled.set(false)

                    binding.rvCustomLocations.post {
                        updateCustomLocations(false)
                        invalidateOptionsMenu()
                    }
                    return
                } else {
                    if (viewModel.customLocationList.get()?.any { viewModel.selectedLocation.get()?.third == it.name } == true) {
                        binding.btnApply.performClick()
                    } else {
                        if (!viewModel.deletedList.get().isNullOrEmpty()) {
                            if (viewModel.deletedList.get()?.any { it.name == intent.getStringExtra("address") } == true) {
                                binding.btnApply.performClick()
                            } else {
                                finish()
                                swipeLeft()
                            }
                        } else {
                            finish()
                            swipeLeft()
                        }
                    }
                }
            }
        })

        binding.layoutAddLocation.setOnClickListener {
            if (getUserPrefs().isLocationFilter) {
                addLocationActivityLauncher.launch(Intent(this, SearchLocationActivity::class.java))
                swipeRight()
            } else {
                startActivity(Intent(getKoinActivity(), SubscriptionActivity::class.java).apply {
                    putExtra("is_gold_available", false)
                    putExtra("restriction_message", "Location Filter is not available with Gold Subscriptions")
                })
                getKoinActivity().swipeUp()
            }
        }

        binding.layoutCurrentLocation.setOnClickListener {
            if (viewModel.isCurrentLocationSelected.get() == false) {
                viewModel.selectedLocation.set(
                    Triple(
                        getUserPrefs().currentLatitude?.toDouble(),
                        getUserPrefs().currentLongitude?.toDouble(),
                        viewModel.currentLocation.get(),
                    )
                )
                viewModel.isCurrentLocationSelected.set(true)
                viewModel.isLocationChanged.set(true)
                adapter.notifyItemRangeChanged(0, viewModel.customLocationList.get()!!.size)
            }
        }

        binding.btnApply.setOnClickListener {
            if (viewModel.isLocationChanged.get() == true) {
                updateLocation()
            } else {
                finish()
                swipeLeft()
            }
        }

        binding.btnDelete.setOnClickListener {
            deleteLocations()
        }
    }

    private fun observeData() {
        viewModel.customLocationList.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                adapter.asyncListDiffer.submitList((viewModel.customLocationList.get())?.toMutableList())
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_change_location, menu)
        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // Handles back action
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)

        binding.toolbar.post {
            try {
                // Loop through toolbar children to find overflow menu button
                for (i in 0 until binding.toolbar.childCount) {
                    val view = binding.toolbar.getChildAt(i)
                    if (view is androidx.appcompat.widget.ActionMenuView) {
                        for (j in 0 until view.childCount) {
                            val child = view.getChildAt(j)
                            if (child.contentDescription != null && child.contentDescription.contains("More options")) {
                                child.setOnClickListener {
                                    showDeletePopup(child)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Control menu item visibility based on the fragment
        if (viewModel.isDeleteAvailable.get() == false) {
            menu?.findItem(R.id.delete_location)?.isVisible = false
        } else if ((viewModel.isDeleteAvailable.get() == true) && (viewModel.isDeleteView.get() == false)) {
            menu?.findItem(R.id.delete_location)?.isVisible = true
        } else {
            menu?.findItem(R.id.delete_location)?.isVisible = false
        }
        return true
    }

    private fun showDeletePopup(view: View) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_delete_locations, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true // Focusable
        )

        // Set actions for menu items
        val actionOne: SquircleConstraintLayout = popupView.findViewById(R.id.layout_delete)

        actionOne.setOnClickListener {
            popupWindow.dismiss()

            viewModel.isDeleteView.set(true)
            invalidateOptionsMenu()

            viewModel.selectedLocation.set(
                Triple(
                    getUserPrefs().currentLatitude?.toDouble(), getUserPrefs().currentLongitude?.toDouble(), viewModel.currentLocation.get()
                )
            )

            viewModel.isCurrentLocationSelected.set(true)
            viewModel.isLocationChanged.set(true)
        }

        popupWindow.setOnDismissListener {
            binding.main.alpha = 1f
        }

        // Show the popup menu
        popupWindow.elevation = 10f

        // Show the popup at the specified position
        popupWindow.showAsDropDown(
            view,
            -dpToPx(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._32sdp)),
            0,
        )

        binding.main.alpha = 0.5f
    }

    private fun updateLocation() {
        LoadingDialog.show(this@ChangeLocationActivity)

        getAddress(
            this@ChangeLocationActivity,
            viewModel.selectedLocation.get()?.first ?: 0.0,
            viewModel.selectedLocation.get()?.second ?: 0.0,
        ) { location ->
            val map = mutableMapOf<String, Any?>()
            map["cust_latitude"] = viewModel.selectedLocation.get()?.first
            map["cust_longitude"] = viewModel.selectedLocation.get()?.second
            map["cust_country"] = location?.countryName

            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.updateFilters(map).collectLatest {
                    launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {
                                LoadingDialog.show(this@ChangeLocationActivity)
                            }

                            Status.SIGN_OUT -> {
                                Toast.makeText(this@ChangeLocationActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                                authOut()
                            }

                            Status.ADMIN_BLOCKED -> {
                                Toast.makeText(this@ChangeLocationActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                    .show()

                                authOut()
                            }

                            Status.ERROR -> {
                                LoadingDialog.hide()

                                Toast.makeText(this@ChangeLocationActivity, it.message.toString(), Toast.LENGTH_SHORT).show()

                                unloadKoinModules(filtersViewModel)

                                finish()
                                swipeLeft()
                            }

                            Status.SUCCESS -> {
                                getUserPrefs().customLatitude = viewModel.selectedLocation.get()?.first.toString()
                                getUserPrefs().customLongitude = viewModel.selectedLocation.get()?.second.toString()
                                getUserPrefs().customLocationName = viewModel.selectedLocation.get()?.third.toString()
                                getUserPrefs().customCountry = location?.countryName

                                getUserPrefs().countryCode = location?.countryCode ?: ""

                                LoadingDialog.hide()

                                unloadKoinModules(filtersViewModel)

                                val intent = Intent()
                                intent.putExtra("latitude", viewModel.selectedLocation.get()!!.first)
                                intent.putExtra("longitude", viewModel.selectedLocation.get()!!.second)
                                intent.putExtra("address", viewModel.selectedLocation.get()!!.third)
                                setResult(RESULT_OK, intent)
                                finish()
                                swipeLeft()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun deleteLocations() {
        val deleteList = adapter.getDeleteList()
        val delete = gsonString(deleteList).replace("[", "").replace("]", "").replace(" ", "")

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteLocations(delete).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@ChangeLocationActivity)
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@ChangeLocationActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@ChangeLocationActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@ChangeLocationActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data == null) {
                                Toast.makeText(this@ChangeLocationActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.customLocationList.get()?.forEach {
                                    if (deleteList.contains(it.id)) {
                                        val list = viewModel.deletedList.get()
                                        list?.add(it)
                                        viewModel.deletedList.set(list)
                                    }
                                }

                                viewModel.customLocationList.get()?.clear()
                                viewModel.customLocationList.set(ArrayList(it.data.locationList))

                                viewModel.isDeleteView.set(false)
                                viewModel.isDeleteEnabled.set(false)
                                viewModel.isDeleteAvailable.set(!viewModel.customLocationList.get().isNullOrEmpty())

                                invalidateOptionsMenu()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setCurrentLocation() {
        val currentLocation =
            if (!getUserPrefs().currentCity.isNullOrEmpty() && !getUserPrefs().currentState.isNullOrEmpty() && !getUserPrefs().currentCountry.isNullOrEmpty()) {
                "${getUserPrefs().currentCity},${getUserPrefs().currentState},${getUserPrefs().currentCountry}"
            } else if (!getUserPrefs().currentState.isNullOrEmpty() && !getUserPrefs().currentCountry.isNullOrEmpty()) {
                "${getUserPrefs().currentState},${getUserPrefs().currentCountry}"
            } else if (!getUserPrefs().currentCountry.isNullOrEmpty()) {
                getUserPrefs().currentCountry
            } else {
                null
            }

        if (currentLocation == null) {
            viewModel.currentLocation.set("-")
            viewModel.selectedLocation.set(Triple(getUserPrefs().currentLatitude?.toDouble(), getUserPrefs().currentLongitude?.toDouble(), null))
        } else if (!currentLocation.contains(",")) {
            viewModel.currentLocation.set(currentLocation)
        } else {
            val split = currentLocation.split(",")
            val displayLocation = StringBuilder()
            split.forEach {
                if (displayLocation.isEmpty()) {
                    displayLocation.append(it.trim())
                } else {
                    displayLocation.append(", ").append(it.trim())
                }
            }
            viewModel.currentLocation.set(displayLocation.toString())
        }

        if (viewModel.selectedLocation.get()?.third == viewModel.currentLocation.get()) {
            viewModel.isCurrentLocationSelected.set(true)
        } else {
            viewModel.isCurrentLocationSelected.set(false)
        }
    }

    private fun getCustomLocations(isSelectFirstPosition: Boolean = false) {
        if (getUserPrefs().isLocationFilter) {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getMyLocation().collectLatest {
                    lifecycleScope.launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {
                                LoadingDialog.show(this@ChangeLocationActivity)
                            }

                            Status.SIGN_OUT -> {
                                LoadingDialog.hide()

                                Toast.makeText(this@ChangeLocationActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                    .show()

                                authOut()
                            }

                            Status.ADMIN_BLOCKED -> {
                                LoadingDialog.hide()

                                Toast.makeText(this@ChangeLocationActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                    .show()

                                authOut()
                            }

                            Status.ERROR -> {
                                LoadingDialog.hide()

                                Toast.makeText(this@ChangeLocationActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }

                            Status.SUCCESS -> {
                                LoadingDialog.hide()
                                viewModel.isDataAvailable.set(true)
                                binding.executePendingBindings()

                                if (it.data == null) {
                                    Toast.makeText(this@ChangeLocationActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                    onBackPressedDispatcher.onBackPressed()
                                } else {
                                    val list = arrayListOf<MyLocation>()
                                    list.addAll(it.data.locationList)
                                    list.reverse()
                                    viewModel.customLocationList.set(list)
                                    updateCustomLocations(isSelectFirstPosition)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateCustomLocations(isSelectFirstPosition: Boolean) {
        binding.txtLocationCount.isVisible = true
        viewModel.isDeleteAvailable.set(!viewModel.customLocationList.get().isNullOrEmpty())
        invalidateOptionsMenu()

        if (binding.rvCustomLocations.layoutManager == null) {
            binding.rvCustomLocations.layoutManager = LinearLayoutManager(this@ChangeLocationActivity, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvCustomLocations.adapter == null) {
            binding.rvCustomLocations.adapter = adapter
        }

        binding.txtLocationCount.text = StringBuilder().append("(").append(viewModel.customLocationList.get()!!.size).append("/5)")

        if (isSelectFirstPosition) {
            viewModel.isLocationChanged.set(true)
            viewModel.isCurrentLocationSelected.set(false)

            binding.imgCurrentLocation.visibility = View.GONE
            binding.txtCurrentLocation.setTextColor(ContextCompat.getColor(this, R.color.white))

            viewModel.selectedLocation.set(
                Triple(
                    viewModel.customLocationList.get()!![0].latitude.toDouble(),
                    viewModel.customLocationList.get()!![0].longitude.toDouble(),
                    viewModel.customLocationList.get()!![0].name,
                )
            )
        }
        adapter.notifyItemRangeChanged(0, viewModel.customLocationList.get()!!.size)
    }

    private fun authOut() {
        LoadingDialog.show(this)

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@ChangeLocationActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        if (viewModel.isDataAvailable.get() == false) {
            getCustomLocations(false)
        } else {
            updateCustomLocations(false)
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            if (viewModel.isDataAvailable.get() == false) {
                getCustomLocations(false)
            } else {
                updateCustomLocations(false)
            }
        }
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.UPDATE_PURCHASE -> {
                logger("--event--", "UPDATE_PURCHASE")

                recreate()
            }
        }
    }

    override fun onLocationClick(location: MyLocation) {
        if (viewModel.isDeleteView.get() == true) {
            val deleteList = adapter.getDeleteList()
            viewModel.isDeleteEnabled.set(deleteList.isNotEmpty())
        } else {
            viewModel.isLocationChanged.set(true)
            viewModel.isCurrentLocationSelected.set(false)

            viewModel.selectedLocation.set(Triple(location.latitude.toDouble(), location.longitude.toDouble(), location.name))
            adapter.notifyItemRangeChanged(0, viewModel.customLocationList.get()!!.size)
        }
    }
}
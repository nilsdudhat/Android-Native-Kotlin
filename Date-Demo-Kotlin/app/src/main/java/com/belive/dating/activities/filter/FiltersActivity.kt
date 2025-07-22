package com.belive.dating.activities.filter

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.filter.location.ChangeLocationActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.InterstitialGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.api.user.models.filters.Filters
import com.belive.dating.databinding.ActivityFiltersBinding
import com.belive.dating.di.filtersViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.current_location.getAddress
import com.belive.dating.helpers.helper_views.rangeseekbar.DoubleValueSeekBarView
import com.belive.dating.helpers.helper_views.rangeseekbar.OnDoubleValueSeekBarChangeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.context.unloadKoinModules

class FiltersActivity : NetworkReceiverActivity() {

    val binding: ActivityFiltersBinding by lazy {
        ActivityFiltersBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: FiltersViewModel

    private val changeLocationActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if ((result.data?.hasExtra("latitude") == true) && (result.data?.hasExtra("longitude") == true) && (result.data?.hasExtra("address") == true)) {
                viewModel.location.set(
                    Triple(
                        result.data?.getDoubleExtra("latitude", 0.0),
                        result.data?.getDoubleExtra("longitude", 0.0),
                        result.data?.getStringExtra("address"),
                    )
                )

                viewModel.isFilterUpdated.set(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(FiltersActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(FiltersActivity::class.java.simpleName)
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

        viewModel = tryKoinViewModel(listOf(filtersViewModel))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        initViews()

        clickListeners()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        viewModel.isDistanceInKms.set(getUserPrefs().countryCode.equals("IN", false))

        setSpinner()

        setSpinnerSelection(0)
    }

    private fun updateViews(filters: Filters) {
        getUserPrefs().customLatitude = filters.custLatitude
        getUserPrefs().customLongitude = filters.custLongitude

        setActiveLocation()

        viewModel.filters.set(filters)

        viewModel.ageMin.set(filters.ageMin)
        viewModel.ageMax.set(filters.ageMax)
        viewModel.distance.set(filters.distance)

        binding.txtDistance.text = StringBuilder().append(filters.distance)
        binding.txtAge.text = StringBuilder().append(filters.ageMin).append("-").append(filters.ageMax)

        binding.doubleRangeSeekbar.currentMinValue = filters.ageMin
        binding.doubleRangeSeekbar.currentMaxValue = filters.ageMax
        binding.seekDistance.progress = filters.distance

        when (filters.seeingInterest) {
            1 -> {
                viewModel.oppositeGender.set(1)
                setSpinnerSelection(0)
            }

            2 -> {
                viewModel.oppositeGender.set(2)
                setSpinnerSelection(1)
            }

            3 -> {
                viewModel.oppositeGender.set(3)
                setSpinnerSelection(2)
            }
        }
    }

    private fun setActiveLocation() {
        val customLocation = getUserPrefs().customLocationName
        if (customLocation == null) {
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

            getUserPrefs().customLocationName = currentLocation

            if (currentLocation == null) {
                viewModel.location.set(Triple(getUserPrefs().currentLatitude?.toDouble(), getUserPrefs().currentLongitude?.toDouble(), "-"))
            } else if (!currentLocation.contains(",")) {
                viewModel.location.set(
                    Triple(
                        getUserPrefs().currentLatitude?.toDouble(),
                        getUserPrefs().currentLongitude?.toDouble(),
                        currentLocation,
                    )
                )
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
                viewModel.location.set(
                    Triple(
                        getUserPrefs().currentLatitude?.toDouble(),
                        getUserPrefs().currentLongitude?.toDouble(),
                        displayLocation.toString(),
                    )
                )
            }
        } else if (customLocation.contains(",")) {
            val split = customLocation.split(",")
            val displayLocation = StringBuilder()
            split.forEach {
                if (displayLocation.isEmpty()) {
                    displayLocation.append(it.trim())
                } else {
                    displayLocation.append(", ").append(it.trim())
                }
            }
            viewModel.location.set(
                Triple(
                    getUserPrefs().customLatitude?.toDouble(),
                    getUserPrefs().customLongitude?.toDouble(),
                    displayLocation.toString(),
                )
            )
        } else {
            viewModel.location.set(Triple(getUserPrefs().customLatitude?.toDouble(), getUserPrefs().customLongitude?.toDouble(), customLocation))
        }
    }

    private fun setSpinnerSelection(position: Int) {
        binding.spinnerInterest.setSelection(position)

        binding.spinnerInterest.postDelayed({
            try {
                val v: View = binding.spinnerInterest.selectedView
                (v as TextView).setTextColor(ContextCompat.getColor(this, R.color.primary_color))
                v.setTypeface(v.typeface, Typeface.BOLD)
            } catch (e: Exception) {
                catchLog("setSpinnerSelection: ${gsonString(e)}")
            }
        }, 500)
    }

    private fun setSpinner() {
        binding.spinnerInterest.viewTreeObserver.addOnPreDrawListener {
            binding.spinnerInterest.dropDownWidth = binding.spinnerInterest.measuredWidth
            true
        }

        // Data for the Spinner
        val items = listOf("Men", "Women", "Both")

        val adapter = ArrayAdapter(this, R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.item_spinner_drop_down)
        binding.spinnerInterest.setAdapter(adapter)
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                applyFilter()
            }
        })

        binding.doubleRangeSeekbar.setOnRangeSeekBarViewChangeListener(object : OnDoubleValueSeekBarChangeListener {

            override fun onValueChanged(
                seekBar: DoubleValueSeekBarView?,
                min: Int,
                max: Int,
                fromUser: Boolean,
            ) {
                // Ensure the minimum thumb range is at least 5
                if (fromUser) {
                    if (max - min < 5) {
                        // Reset thumb positions
                        if (min + 5 <= 60) {
                            binding.doubleRangeSeekbar.currentMinValue = min
                            binding.doubleRangeSeekbar.currentMaxValue = min + 5

                            viewModel.ageMin.set(min)
                            viewModel.ageMax.set(min + 5)
                        } else {
                            binding.doubleRangeSeekbar.currentMinValue = max - 5
                            binding.doubleRangeSeekbar.currentMaxValue = max

                            viewModel.ageMin.set(max - 5)
                            viewModel.ageMax.set(max)
                        }
                    } else {
                        viewModel.ageMin.set(min)
                        viewModel.ageMax.set(max)
                    }
                    if ((viewModel.filters.get() != null) && ((viewModel.filters.get()?.ageMax != viewModel.ageMax.get()) || (viewModel.filters.get()?.ageMin != viewModel.ageMin.get()))) {
                        viewModel.isFilterUpdated.set(true)
                    }
                }
            }

            override fun onStartTrackingTouch(
                seekBar: DoubleValueSeekBarView?,
                min: Int,
                max: Int,
            ) {

            }

            override fun onStopTrackingTouch(seekBar: DoubleValueSeekBarView?, min: Int, max: Int) {

            }
        })

        binding.seekDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.distance.set(progress)

                if ((viewModel.filters.get() != null) && (viewModel.filters.get()?.distance != viewModel.distance.get())) {
                    viewModel.isFilterUpdated.set(true)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        binding.spinnerInterest.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                // Change the selected item's text color
                (view as TextView).setTextColor(ContextCompat.getColor(this@FiltersActivity, R.color.primary_color))
                view.gravity = Gravity.END

                if ((viewModel.filters.get() != null) && (viewModel.filters.get()?.seeingInterest != position + 1)) {
                    viewModel.isFilterUpdated.set(true)
                }

                viewModel.oppositeGender.set(position + 1)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.btnChangeLocation.setOnClickListener {
            changeLocationActivityLauncher.launch(Intent(this@FiltersActivity, ChangeLocationActivity::class.java).apply {
                putExtra("latitude", viewModel.location.get()?.first)
                putExtra("longitude", viewModel.location.get()?.second)
                putExtra("address", viewModel.location.get()?.third)
            })
            swipeRight()
        }

        binding.btnApply.setOnClickListener {
            applyFilter()
        }
    }

    private fun applyFilter() {
        if (viewModel.isFilterUpdated.get() == false) {
            ManageAds.showInterstitialAd(InterstitialGroup.Filter) {
                unloadKoinModules(filtersViewModel)

                finish()
                swipeLeft()
            }
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val map = mutableMapOf<String, Any?>()
            map["age_min"] = viewModel.ageMin.get()
            map["age_max"] = viewModel.ageMax.get()
            map["distance"] = viewModel.distance.get()
            map["seeing_interest"] = viewModel.oppositeGender.get()
            map["is_show_seeing_interest"] = 1
            map["is_show_age"] = 1
            map["is_show_distance"] = 1
            map["cust_latitude"] = viewModel.location.get()?.first
            map["cust_longitude"] = viewModel.location.get()?.second
            map["cust_country"] = getUserPrefs().customCountry ?: getUserPrefs().currentCountry

            viewModel.updateFilters(map).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@FiltersActivity)
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@FiltersActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@FiltersActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@FiltersActivity, it.message.toString(), Toast.LENGTH_SHORT).show()

                            unloadKoinModules(filtersViewModel)

                            finish()
                            swipeLeft()
                        }

                        Status.SUCCESS -> {
                            getUserPrefs().customLatitude = viewModel.location.get()?.first.toString()
                            getUserPrefs().customLongitude = viewModel.location.get()?.second.toString()
                            getUserPrefs().customLocationName = viewModel.location.get()?.third.toString()

                            getAddress(this@FiltersActivity, viewModel.location.get()?.first ?: 0.0, viewModel.location.get()?.second ?: 0.0) {
                                getUserPrefs().countryCode = it?.countryCode ?: ""

                                LoadingDialog.hide()

                                lifecycleScope.launch(Dispatchers.Main) {
                                    ManageAds.showInterstitialAd(InterstitialGroup.Filter) {
                                        unloadKoinModules(filtersViewModel)

                                        val intent = Intent()
                                        intent.putExtra("is_filter", true)
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

    private fun getFiltersData() {
        if (viewModel.isDataLoaded.get() == false) {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getFiltersData().collectLatest {
                    launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {
                                LoadingDialog.show(this@FiltersActivity)
                            }

                            Status.SIGN_OUT -> {
                                Toast.makeText(this@FiltersActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                                authOut()
                            }

                            Status.ADMIN_BLOCKED -> {
                                Toast.makeText(this@FiltersActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                                authOut()
                            }

                            Status.ERROR -> {
                                Toast.makeText(this@FiltersActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                            }

                            Status.SUCCESS -> {
                                LoadingDialog.hide()

                                if (it.data == null) {
                                    Toast.makeText(this@FiltersActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()

                                    onBackPressedDispatcher.onBackPressed()
                                } else {
                                    viewModel.isDataLoaded.set(true)

                                    updateViews(it.data.filters)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            viewModel.filters.get()?.let { updateViews(it) }
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

                startActivity(Intent(this@FiltersActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        if (viewModel.isDataLoaded.get() == false) {
            getFiltersData()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected && (viewModel.isDataLoaded.get() == false)) {
            getFiltersData()
        }
    }
}
package com.belive.dating.activities.filter.location.search_location

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.Observable
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.databinding.ActivitySearchLocationBinding
import com.belive.dating.di.filtersViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.hideKeyboard
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchLocationActivity : NetworkReceiverActivity() {

    val binding: ActivitySearchLocationBinding by lazy {
        ActivitySearchLocationBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: SearchLocationViewModel

    val adapter by lazy {
        SearchLocationAdapter(viewModel)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(SearchLocationActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(SearchLocationActivity::class.java.simpleName)
    }

    override fun onDestroy() {
        Places.deinitialize()
        super.onDestroy()
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

        viewModel.searchedValue.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                viewModel.selectedPlace.postValue(null)
                viewModel.fetchPlaces(binding.editText.text.toString())
            }
        })

        initRecyclerView()
    }

    private fun initRecyclerView() {
        if (binding.rvPlaces.layoutManager == null) {
            binding.rvPlaces.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvPlaces.adapter == null) {
            binding.rvPlaces.adapter = adapter
        }
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                hideKeyboard()

                finish()
                swipeLeft()
            }
        })

        binding.btnAdd.setOnClickListener {
            hideKeyboard()

            if (viewModel.selectedPlace.value == null) {
                Toast.makeText(this, "Please select a location to add", Toast.LENGTH_SHORT).show()
            } else {
                addLocation()
            }
        }
    }

    private fun addLocation() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.selectedPlace.value?.let {
                viewModel.addLocation(it).collect {
                    launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.LOADING -> {
                                LoadingDialog.show(this@SearchLocationActivity)
                            }

                            Status.SIGN_OUT -> {
                                LoadingDialog.hide()

                                Toast.makeText(this@SearchLocationActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                    .show()

                                authOut()
                            }

                            Status.ADMIN_BLOCKED -> {
                                LoadingDialog.hide()

                                Toast.makeText(
                                    this@SearchLocationActivity,
                                    "Admin has blocked you, because of security reasons.",
                                    Toast.LENGTH_SHORT,
                                ).show()

                                authOut()
                            }

                            Status.ERROR -> {
                                LoadingDialog.hide()

                                Toast.makeText(this@SearchLocationActivity, it.message, Toast.LENGTH_SHORT).show()
                            }

                            Status.SUCCESS -> {
                                LoadingDialog.hide()

                                if (it.data != null) {
                                    val intent = Intent()
                                    intent.putExtra("is_location_added", true)
                                    setResult(RESULT_OK, intent)
                                    finish()
                                    swipeLeft()
                                } else {
                                    Toast.makeText(this@SearchLocationActivity, "Something went to wrong...!", Toast.LENGTH_SHORT).show()
                                }
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
                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@SearchLocationActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
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
}
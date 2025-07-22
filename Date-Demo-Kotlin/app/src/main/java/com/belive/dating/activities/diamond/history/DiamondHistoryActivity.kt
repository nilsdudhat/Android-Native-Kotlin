package com.belive.dating.activities.diamond.history

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
import com.belive.dating.databinding.ActivityDiamondHistoryBinding
import com.belive.dating.di.diamondViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DiamondHistoryActivity : NetworkReceiverActivity(), DiamondHistoryAdapter.OnDiamondLastItemListener {

    val binding by lazy {
        ActivityDiamondHistoryBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: DiamondHistoryViewModel

    val adapter by lazy {
        DiamondHistoryAdapter(activity = this, viewModel = viewModel, lastItemCallback = this)
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

        viewModel = tryKoinViewModel(listOf(diamondViewModel))
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
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button
    }

    private fun setUpRecyclerView() {
        if (binding.rvDiamondHistory.layoutManager == null) {
            binding.rvDiamondHistory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        if (binding.rvDiamondHistory.adapter == null) {
            binding.rvDiamondHistory.adapter = adapter
        }

        binding.executePendingBindings()
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

    private fun getDiamondHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getDiamondHistory().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            viewModel.isLoading.set(true)
                            if (viewModel.diamondHistoryList.get().isNullOrEmpty()) {
                                LoadingDialog.show(this@DiamondHistoryActivity)
                            }
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@DiamondHistoryActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@DiamondHistoryActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()

                            Toast.makeText(this@DiamondHistoryActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            viewModel.isLoading.set(false)
                            LoadingDialog.hide()

                            if (it.data != null) {
                                val diamondHistoryList = viewModel.diamondHistoryList.get() ?: arrayListOf()
                                diamondHistoryList.addAll(it.data.diamondTransactionList)
                                viewModel.diamondHistoryList.set(diamondHistoryList)

                                setUpRecyclerView()
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

                startActivity(Intent(this@DiamondHistoryActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        if (viewModel.diamondHistoryList.get()?.isEmpty() == true) {
            getDiamondHistory()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected && viewModel.diamondHistoryList.get()?.isEmpty() == true) {
            getDiamondHistory()
        }
    }

    override fun onDiamondLastItem() {
        getDiamondHistory()
    }
}
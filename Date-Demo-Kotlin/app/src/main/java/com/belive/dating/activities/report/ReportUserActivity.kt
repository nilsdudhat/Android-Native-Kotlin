package com.belive.dating.activities.report

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.databinding.ActivityReportUserBinding
import com.belive.dating.di.reportViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.hideKeyboard
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeDown
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.unloadKoinModules

class ReportUserActivity : NetworkReceiverActivity(), ReportReasonAdapter.OnReasonClickListener, ReportDetailsAdapter.DetailsClickListener {

    private val binding: ActivityReportUserBinding by lazy {
        ActivityReportUserBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: ReportViewModel

    private val reasonAdapter = ReportReasonAdapter(this@ReportUserActivity)
    private val detailsAdapter = ReportDetailsAdapter(this@ReportUserActivity)

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(ReportUserActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(ReportUserActivity::class.java.simpleName)
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

        viewModel = tryKoinViewModel(listOf(reportViewModel))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        binding.root.post {
            observeNetwork()
        }

        initViews()
    }

    private fun initViews() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                hideKeyboard()

                if (viewModel.currentIndex.get()!! > 0) {
                    viewModel.currentIndex.set(viewModel.currentIndex.get()!! - 1)

                    if ((viewModel.currentIndex.get()!! + 1) == 1) {
                        binding.rvDetails.invalidate()
                        detailsAdapter.selectedPosition = "-1"
                    } else if ((viewModel.currentIndex.get()!! + 1) == 2) {
                        binding.edtReport.text.clear()
                    }

                    enableDisableButton(true)

                    if (viewModel.currentIndex.get()!! == 0) {
                        binding.btnBack.visibility = View.GONE
                    } else {
                        binding.btnBack.visibility = View.VISIBLE
                    }
                } else {
                    unloadKoinModules(reportViewModel)

                    finish()
                    swipeDown()
                }
            }
        })

        binding.btnClose.setOnClickListener {
            unloadKoinModules(reportViewModel)

            finish()
            swipeDown()
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnReport.setOnClickListener {
            hideKeyboard()

            if (viewModel.currentIndex.get()!! < 2) {
                viewModel.currentIndex.set(viewModel.currentIndex.get()!! + 1)

                if (viewModel.currentIndex.get()!! == 1) {
                    initDetailsRecyclerView()
                } else if (viewModel.currentIndex.get()!! == 2) {
                    binding.edtReport.text.clear()
                }

                enableDisableButton(false)

                if (viewModel.currentIndex.get()!! == 0) {
                    binding.btnBack.visibility = View.GONE
                } else {
                    binding.btnBack.visibility = View.VISIBLE
                }
            } else {
                val reason = reasonAdapter.reasonList[reasonAdapter.selectedPosition]

                val question = detailsAdapter.sequenceList[detailsAdapter.selectedPosition[0].digitToInt()]
                val subDetails = detailsAdapter.detailsList[question]
                val detail = subDetails?.get(detailsAdapter.selectedPosition[1].digitToInt())

                val comment = binding.edtReport.text.trim().toString()
                val profileId = intent.getIntExtra("profile_id", -1)

                val jsonBody = JsonObject()
                jsonBody.addProperty("profile_id", profileId)
                jsonBody.addProperty("reason", reason)
                jsonBody.addProperty("detail", detail)
                jsonBody.addProperty("comment", comment)

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.reportUser(jsonBody).collect {
                        launch(Dispatchers.Main) {
                            when (it.status) {
                                Status.LOADING -> {
                                    LoadingDialog.show(this@ReportUserActivity)
                                }

                                Status.SIGN_OUT -> {
                                    Toast.makeText(this@ReportUserActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                        .show()

                                    authOut()
                                }

                                Status.ADMIN_BLOCKED -> {
                                    Toast.makeText(
                                        this@ReportUserActivity,
                                        "Admin has blocked you, because of security reasons.",
                                        Toast.LENGTH_SHORT,
                                    ).show()

                                    authOut()
                                }

                                Status.ERROR -> {
                                    LoadingDialog.hide()

                                    Toast.makeText(this@ReportUserActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
                                }

                                Status.SUCCESS -> {
                                    LoadingDialog.hide()

                                    Toast.makeText(this@ReportUserActivity, "User reported successfully.", Toast.LENGTH_SHORT).show()

                                    val intent = Intent()
                                    intent.putExtra("resultData", "Report")
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.edtReport.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding.txtAboutLength.text = StringBuilder().append("(").append(binding.edtReport.text.trim().length).append("/500 Words)")

                if (validateComment(binding.edtReport.text.toString().trim())) {
                    enableDisableButton(true)
                } else {
                    enableDisableButton(false)
                }
            }
        })

        initReasonRecyclerView()

        enableDisableButton(false)

        if (viewModel.currentIndex.get()!! == 0) {
            binding.btnBack.visibility = View.GONE
        } else {
            binding.btnBack.visibility = View.VISIBLE
        }
    }

    private fun validateComment(text: String): Boolean {
        if (text.isEmpty() || text.length < 20) {
            binding.txtAboutError.visibility = View.VISIBLE
            binding.txtAboutError.text = StringBuilder().append(getString(R.string.error_report_minimum))
            return false
        } else if (binding.edtReport.text.length > 500) {
            binding.txtAboutError.visibility = View.VISIBLE
            binding.txtAboutError.text = StringBuilder().append(getString(R.string.error_report_maximum))
            return false
        } else {
            binding.txtAboutError.visibility = View.GONE
            return true
        }
    }

    private fun initDetailsRecyclerView() {
        binding.rvDetails.apply {
            layoutManager = LinearLayoutManager(this@ReportUserActivity, LinearLayoutManager.VERTICAL, false)
            adapter = this@ReportUserActivity.detailsAdapter.apply {
                sequenceList = viewModel.getSequenceList()
                detailsList = viewModel.getDetails()
            }
        }
    }

    private fun initReasonRecyclerView() {
        binding.rvReasons.apply {
            layoutManager = LinearLayoutManager(this@ReportUserActivity, LinearLayoutManager.VERTICAL, false)
            adapter = this@ReportUserActivity.reasonAdapter.apply {
                reasonList = viewModel.getReasons()
            }
        }
    }

    private fun enableDisableButton(isEnable: Boolean) {
        if (isEnable) {
            binding.btnReport.isEnabled = true
            binding.btnReport.isClickable = true
        } else {
            binding.btnReport.isEnabled = false
            binding.btnReport.isClickable = false
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

                startActivity(Intent(this@ReportUserActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onReasonClicked(reasonPosition: Int) {
        reasonAdapter.setSelection(reasonPosition)
        enableDisableButton(true)
    }

    override fun onDetailsClicked() {
        enableDisableButton(true)
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)
    }
}
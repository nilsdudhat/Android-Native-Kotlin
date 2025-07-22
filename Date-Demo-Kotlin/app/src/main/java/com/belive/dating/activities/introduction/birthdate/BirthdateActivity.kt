package com.belive.dating.activities.introduction.birthdate

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.introduction.opposite_gender.OppositeGenderActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.databinding.ActivityBirthdateBinding
import com.belive.dating.di.gistModule
import com.belive.dating.di.introductionModule
import com.belive.dating.di.introductionViewModels
import com.belive.dating.di.splashDataModule
import com.belive.dating.dialogs.AppDialog
import com.belive.dating.dialogs.InitFailedDialog
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.calculateAge
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.focusField
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getIntroductionPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.hideKeyboard
import com.belive.dating.extensions.isAppUpdateRequired
import com.belive.dating.extensions.isValidDate
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.reOpenApp
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.showKeyboard
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.get_gist.getGistData
import com.belive.dating.helpers.helper_functions.splash_data.SplashData
import org.json.JSONObject
import org.koin.core.context.unloadKoinModules
import java.util.Calendar

class BirthdateActivity : NetworkReceiverActivity() {

    private val TAG = "--birth_date--"

    val binding: ActivityBirthdateBinding by lazy {
        ActivityBirthdateBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: BirthDateViewModel

    private val minAge: Int = 18
    private val maxAge: Int = 60

    val maxYear by lazy {
        // Example date (18 years ago from today)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -minAge)
        calendar.get(Calendar.YEAR)
    }

    val minYear by lazy {
        // Example date (60 years ago from today)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -maxAge)
        calendar.get(Calendar.YEAR)
    }

    override fun onResume() {
        super.onResume()

        logger("--introduction--", "onResume: $TAG")

        mixPanel?.timeEvent(BirthdateActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        logger("--introduction--", "onPause: $TAG")

        if (isFinishing) {
            mixPanel?.track(BirthdateActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", true)
            })
        } else {
            mixPanel?.track(BirthdateActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", false)
            })
        }
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

            // Set top padding for status bar
            view.setPadding(
                view.paddingLeft,
                statusBarHeight,
                view.paddingRight,
                if (imeHeight > 0) imeHeight else navBarHeight,
            )

            insets
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        viewModel = tryKoinViewModel(listOf(introductionModule, introductionViewModels))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getIntroductionPrefs().birthDate = null

                finish()
                swipeLeft()
            }
        })

        binding.footerButtons.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.footerButtons.btnNext.setOnClickListener {
            mixPanel?.track(TAG, JSONObject().apply {
                put("Birth Date", viewModel.day.get() + "/" + viewModel.month.get() + "/" + viewModel.year.get())
            })

            getIntroductionPrefs().birthDate = viewModel.year.get() + "-" + viewModel.month.get() + "-" + viewModel.day.get()

            startActivity(Intent(this@BirthdateActivity, OppositeGenderActivity::class.java))
            swipeRight()
        }
    }

    private fun initViews() {
        binding.txtTitle.text = StringBuilder().append("Hey ${getIntroductionPrefs().name}, when you born?")
        val split = getIntroductionPrefs().birthDate?.split("-")
        if (split?.isEmpty() != true) {
            try {
                viewModel.year.set(split?.get(0))
                viewModel.month.set(split?.get(1))
                viewModel.day.set(split?.get(2))

                val year = viewModel.year.get()
                year?.length?.let {
                    binding.edtYear.post {
                        binding.edtYear.setSelection(it)
                    }
                }
                if ((year == null) || (year.length != 4)) {
                    viewModel.validationError.set("Invalid Year")
                    binding.edtYear.post {
                        binding.edtYear.focusField()
                    }
                    viewModel.isNextEnabled.set(false)
                } else {
                    if (year.toInt() < minYear) {
                        viewModel.validationError.set("Invalid Year")
                        binding.edtYear.post {
                            binding.edtYear.focusField()
                        }
                        viewModel.isNextEnabled.set(false)
                    } else if (year.toInt() > maxYear) {
                        viewModel.validationError.set("Year above $maxYear is not allowed")
                        binding.edtYear.post {
                            binding.edtYear.focusField()
                        }
                        viewModel.isNextEnabled.set(false)
                    } else if (binding.edtDay.text?.length != 2) {
                        viewModel.validationError.set("Year above $maxYear is not allowed")
                        binding.edtYear.post {
                            binding.edtDay.focusField()
                        }
                        viewModel.isNextEnabled.set(false)
                    } else if ((viewModel.day.get()!!.toInt() == 0) || (viewModel.day.get()!!.toInt() >= 31)) {
                        viewModel.validationError.set("Invalid Day")
                        binding.edtYear.post {
                            binding.edtDay.focusField()
                        }
                        viewModel.isNextEnabled.set(false)
                        return
                    } else if (viewModel.month.get()?.length != 2) {
                        viewModel.validationError.set("Invalid Day")
                        binding.edtYear.post {
                            binding.edtMonth.focusField()
                        }
                        viewModel.isNextEnabled.set(false)
                    } else if ((0 == viewModel.month.get()!!.toInt()) || (viewModel.month.get()!!.toInt() > 12)) {
                        viewModel.validationError.set("Invalid Month")
                        binding.edtYear.post {
                            binding.edtMonth.focusField()
                        }
                        viewModel.isNextEnabled.set(false)
                    } else if (!isValidDate("${viewModel.day.get()}/${viewModel.month.get()}/${viewModel.year.get()}")) {
                        viewModel.validationError.set("Please enter a valid date")
                    } else if (eraseDate()) {
                        viewModel.isNextEnabled.set(false)
                    } else {
                        viewModel.validationError.set("")
                        hideKeyboard()
                        viewModel.isNextEnabled.set(true)
                    }
                }
            } catch (e: Exception) {
                catchLog("initViews: ${gsonString(e)}")
            }
        }

        binding.root.postDelayed({
            if (viewModel.day.get()?.length != 2) {
                binding.edtDay.showKeyboard()
                binding.edtDay.focusField()
            } else if (viewModel.day.get()!!.toInt() == 0 || (viewModel.day.get()!!.toInt() > 31)) {
                binding.edtDay.showKeyboard()
                binding.edtDay.focusField()
            } else if (viewModel.month.get()?.length != 2) {
                binding.edtDay.showKeyboard()
                binding.edtDay.focusField()
            } else if ((viewModel.month.get()!!.toInt() == 0) || (viewModel.month.get()!!.toInt() > 12)) {
                binding.edtMonth.showKeyboard()
                binding.edtMonth.focusField()
            } else if (viewModel.year.get()?.length != 4) {
                binding.edtYear.showKeyboard()
                binding.edtYear.focusField()
            } else if (viewModel.year.get()!!.toInt() < minYear) {
                binding.edtYear.showKeyboard()
                binding.edtYear.focusField()
            } else if (viewModel.year.get()!!.toInt() > maxYear) {
                binding.edtYear.showKeyboard()
                binding.edtYear.focusField()
            }
        }, 500)

        binding.edtDay.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DEL) && (viewModel.day.get()?.isEmpty() == true)) {
                binding.edtMonth.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                // Move focus to the second EditText on delete
                binding.edtDay.focusField()
                true // Indicate that the event was handled
            } else if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DEL) && (viewModel.day.get()?.length == 1)) {
                binding.edtMonth.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                viewModel.day.set("")
                true // Indicate that the event was handled
            } else {
                false // Allow default behavior
            }
        }

        binding.edtMonth.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DEL) && (viewModel.month.get()?.isEmpty() == true)) {
                binding.edtMonth.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                // Move focus to the second EditText on delete
                binding.edtDay.focusField()
                true // Indicate that the event was handled
            } else if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DEL) && (viewModel.month.get()?.length == 1)) {
                binding.edtMonth.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                viewModel.month.set("")
                true // Indicate that the event was handled
            } else {
                false // Allow default behavior
            }
        }

        binding.edtYear.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DEL) && (viewModel.year.get()?.isEmpty() == true)) {
                binding.edtYear.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                // Move focus to the second EditText on delete
                binding.edtMonth.focusField()
                true // Indicate that the event was handled
            } else if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DEL) && (viewModel.year.get()?.length == 1)) {
                binding.edtYear.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                viewModel.year.set("")
                true // Indicate that the event was handled
            } else {
                false // Allow default behavior
            }
        }

        binding.edtDay.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                // Move to next EditText
                binding.edtMonth.focusField()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.edtMonth.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                // Move to next EditText
                binding.edtYear.focusField()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.edtYear.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Move to next EditText
                binding.edtYear.hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.edtDay.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex("^[0-9]*$"))) {
                source // Allow input
            } else {
                "" // Reject input
            }
        }, InputFilter.LengthFilter(2))

        binding.edtMonth.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex("^[0-9]*$"))) {
                source // Allow input
            } else {
                "" // Reject input
            }
        }, InputFilter.LengthFilter(2))

        binding.edtYear.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex("^[0-9]*$"))) {
                source // Allow input
            } else {
                "" // Reject input
            }
        }, InputFilter.LengthFilter(4))

        binding.edtDay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                s?.length?.let { binding.edtDay.setSelection(it) }

                val day = s.toString()
                if (day.length != 2) {
                    viewModel.validationError.set("Invalid Day")
                    binding.edtDay.focusField()
                    viewModel.isNextEnabled.set(false)

                    if (day.isNotEmpty() && day.toInt() > 3) {
                        viewModel.day.set(StringBuilder().append("0").append(day).toString())
                        binding.executePendingBindings()
                        binding.edtMonth.focusField()
                    }
                } else {
                    if (day.toInt() == 0 || (day.toInt() > 31)) {
                        viewModel.validationError.set("Invalid Day")
                        binding.edtDay.focusField()
                        viewModel.isNextEnabled.set(false)
                        return
                    } else if (viewModel.month.get()?.length != 2) {
                        viewModel.validationError.set("Please enter Month")
                        binding.edtMonth.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if ((viewModel.month.get()!!.toInt() == 0) || (viewModel.month.get()!!.toInt() > 12)) {
                        viewModel.validationError.set("Invalid Month")
                        binding.edtMonth.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (viewModel.year.get()?.length != 4) {
                        viewModel.validationError.set("Please enter Year")
                        binding.edtYear.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (viewModel.year.get()!!.toInt() < minYear) {
                        viewModel.validationError.set("Year below $minYear is not allowed")
                        binding.edtYear.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (viewModel.year.get()!!.toInt() > maxYear) {
                        viewModel.validationError.set("Year above $maxYear is not allowed")
                        binding.edtYear.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (!isValidDate("${viewModel.day.get()}/${viewModel.month.get()}/${viewModel.year.get()}")) {
                        viewModel.validationError.set("Please enter a valid date")
                        viewModel.isNextEnabled.set(false)
                    } else if (eraseDate()) {
                        viewModel.isNextEnabled.set(false)
                    } else {
                        viewModel.validationError.set("")
                        hideKeyboard()
                        viewModel.isNextEnabled.set(true)
                    }
                }
            }
        })

        binding.edtMonth.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if ((count > 0) && (s.toString().length == 1)) {
                    binding.edtDay.focusField()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (!binding.edtMonth.hasFocus()) {
                    return
                }

                s?.length?.let { binding.edtMonth.setSelection(it) }

                val month = s.toString()
                if (month.length != 2) {
                    viewModel.validationError.set("Invalid Month")
                    binding.edtMonth.focusField()
                    viewModel.isNextEnabled.set(false)

                    if (month.isNotEmpty() && month.toInt() > 1) {
                        viewModel.month.set(StringBuilder().append("0").append(month).toString())
                        binding.executePendingBindings()
                        binding.edtYear.focusField()
                    }
                } else {
                    if ((month.toInt() == 0) || (month.toInt() > 12)) {
                        viewModel.validationError.set("Invalid Month")
                        binding.edtMonth.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (viewModel.day.get()?.length != 2) {
                        viewModel.validationError.set("Please enter Day")
                        binding.edtDay.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if ((viewModel.day.get()!!.toInt() == 0) || (viewModel.day.get()!!.toInt() >= 31)) {
                        viewModel.validationError.set("Invalid Day")
                        binding.edtDay.focusField()
                        viewModel.isNextEnabled.set(false)
                        return
                    } else if (viewModel.year.get()?.length != 4) {
                        viewModel.validationError.set("Please enter Year")
                        binding.edtYear.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (viewModel.year.get()!!.toInt() < minYear) {
                        viewModel.validationError.set("Year below $minYear is not allowed")
                        binding.edtYear.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (viewModel.year.get()!!.toInt() > maxYear) {
                        viewModel.validationError.set("Year above $maxYear is not allowed")
                        binding.edtYear.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (!isValidDate("${viewModel.day.get()}/${viewModel.month.get()}/${viewModel.year.get()}")) {
                        viewModel.validationError.set("Please enter a valid date")
                        viewModel.isNextEnabled.set(false)
                    } else if (eraseDate()) {
                        viewModel.isNextEnabled.set(false)
                    } else {
                        viewModel.validationError.set(null)
                        hideKeyboard()
                        viewModel.isNextEnabled.set(true)
                    }
                }
            }
        })

        binding.edtYear.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if ((count > 0) && (s.toString().length == 1)) {
                    binding.edtMonth.focusField()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (!binding.edtYear.hasFocus()) {
                    return
                }

                s?.length?.let { binding.edtYear.setSelection(it) }

                val year = s.toString()
                if (year.length != 4) {
                    viewModel.validationError.set("Invalid Year")
                    binding.edtYear.focusField()
                    viewModel.isNextEnabled.set(false)
                } else {
                    if (year.toInt() < minYear) {
                        viewModel.validationError.set("Invalid Year")
                        binding.edtYear.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (year.toInt() > maxYear) {
                        viewModel.validationError.set("Year above $maxYear is not allowed")
                        binding.edtYear.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (binding.edtDay.text?.length != 2) {
                        viewModel.validationError.set("Year above $maxYear is not allowed")
                        binding.edtDay.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if ((viewModel.day.get()!!.toInt() == 0) || (viewModel.day.get()!!.toInt() >= 31)) {
                        viewModel.validationError.set("Invalid Day")
                        binding.edtDay.focusField()
                        viewModel.isNextEnabled.set(false)
                        return
                    } else if (viewModel.month.get()?.length != 2) {
                        viewModel.validationError.set("Invalid Day")
                        binding.edtMonth.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if ((0 == viewModel.month.get()!!.toInt()) || (viewModel.month.get()!!.toInt() > 12)) {
                        viewModel.validationError.set("Invalid Month")
                        binding.edtMonth.focusField()
                        viewModel.isNextEnabled.set(false)
                    } else if (!isValidDate("${viewModel.day.get()}/${viewModel.month.get()}/${viewModel.year.get()}")) {
                        viewModel.validationError.set("Please enter a valid date")
                    } else if (eraseDate()) {
                        viewModel.isNextEnabled.set(false)
                    } else {
                        viewModel.validationError.set("")
                        hideKeyboard()
                        viewModel.isNextEnabled.set(true)
                    }
                }
            }
        })
    }

    fun eraseDate(): Boolean {
        val day = viewModel.day.get()!!
        val month = viewModel.month.get()!!
        val year = viewModel.year.get()!!

        val age = calculateAge(year.toInt(), month.toInt(), day.toInt())

        if (age > 60) {
            viewModel.validationError.set("Age must be below 60, You are not eligible to use this app")
            return true
        } else if (age < 18) {
            viewModel.validationError.set("Age must be below 60, You are not eligible to use this app")
            return true
        } else {
            viewModel.validationError.set("")
            return false
        }
    }

    private fun uploadSplashData() {
        val isSplashDataAvailable = try {
            getKoinObject().get<SplashData>()
            true
        } catch (e: Exception) {
            false
        }
        if (isSplashDataAvailable) {
            val splashData = getKoinObject().get<SplashData>()
            splashData.referrers(
                onSuccess = {
                    splashData.getCountryDataApi(
                        onSuccess = {
                            splashData.sendUserData(onSuccess = {
                                unloadKoinModules(splashDataModule)
                            }, onError = {
                                unloadKoinModules(splashDataModule)
                            })
                        },
                        onError = {
                            unloadKoinModules(splashDataModule)
                        },
                    )
                },
                onError = {
                    unloadKoinModules(splashDataModule)
                },
            )
        }
    }

    private fun afterGist() {
        if (getGistPrefs().appRedirectOtherAppStatus) {
            AppDialog.showAppRedirectDialog(
                context = this,
                onManage = {
                    logger(TAG, "redirectPath: ${getGistPrefs().appNewPackageName}")

                    try {
                        val marketUri = getGistPrefs().appNewPackageName.toUri()
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (ignored: Exception) {
                        Toast.makeText(this, "Something want wrong", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        } else if (isAppUpdateRequired()) {
            val isFlexible = getGistPrefs().appUpdateAppDialogStatus

            AppDialog.showAppUpdateDialog(
                context = this,
                isFlexible = isFlexible,
                onClose = {

                },
                onManage = {
                    val packageName = getGistPrefs().appUpdatePackageName
                    logger(TAG, "packageName: $packageName")

                    try {
                        val marketUri = "https://play.google.com/store/apps/details?id=${packageName}".toUri()
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (ignored: Exception) {
                        Toast.makeText(this, "Something want wrong", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }

    private fun getGist() {
        getGistData(
            lifecycleScope,
            isLoading = {
                if (it) {
                    LoadingDialog.show(this)
                } else {
                    LoadingDialog.hide()
                }
            },
            gistNotAvailable = {
                uploadSplashData()

                afterGist()
            },
            onError = {
                InitFailedDialog.showAppUpdateDialog(onTryAgain = {
                    getGist()
                }, onReOpenApp = {
                    reOpenApp()
                })
            },
            onSuccess = {
                unloadKoinModules(gistModule)

                LoadingDialog.show(this)

                uploadSplashData()

                ManageAds.loadAds()

                AdmobAds.showAppOpenAdAfterSplash {
                    LoadingDialog.hide()

                    afterGist()
                }
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        if (intent.hasExtra("load_gist") && intent.getBooleanExtra("load_gist", true)) {
            getGist()
        } else {
            afterGist()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        if (isConnected) {
            if (intent.hasExtra("load_gist") && intent.getBooleanExtra("load_gist", true)) {
                getGist()
            } else {
                afterGist()
            }
        }
    }
}
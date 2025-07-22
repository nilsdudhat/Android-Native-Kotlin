package com.belive.dating.activities.search_user

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.activities.user_details.UserDetailsActivity
import com.belive.dating.databinding.ActivitySearchUserBinding
import com.belive.dating.di.searchUserViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.unloadKoinModules

class SearchUserActivity : AppCompatActivity(), SearchUserAdapter.OnItemClickListener {

    private val binding: ActivitySearchUserBinding by lazy {
        ActivitySearchUserBinding.inflate(layoutInflater)
    }

    private val adapter = SearchUserAdapter(this)

    lateinit var viewModel: SearchUserViewModel

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

        viewModel = tryKoinViewModel(listOf(searchUserViewModel))

        observer()

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                unloadKoinModules(searchUserViewModel)

                finish()
                swipeLeft()
            }
        })
    }

    private fun observer() {
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.fetchUser(binding.editText.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
        })

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.searchData.collect {
                lifecycleScope.launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {

                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@SearchUserActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@SearchUserActivity, "Your session has expired. Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            logger("--users--", "error: ${it.message}")

                            Toast.makeText(this@SearchUserActivity, it.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        Status.SUCCESS -> {
                            logger("--users--", "size: ${it.data?.size}")

                            if (it.data != null) {
                                adapter.userList = ArrayList(it.data)
                            } else {
                                adapter.userList = arrayListOf()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvUsers.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvUsers.adapter = adapter
        binding.rvUsers.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL,
            )
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

    private fun authOut() {
        LoadingDialog.show(this)

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@SearchUserActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onItemClick(profileId: Int) {
        val intent = Intent(this, UserDetailsActivity::class.java)
        intent.putExtra("userId", profileId)
        startActivity(intent)
    }
}
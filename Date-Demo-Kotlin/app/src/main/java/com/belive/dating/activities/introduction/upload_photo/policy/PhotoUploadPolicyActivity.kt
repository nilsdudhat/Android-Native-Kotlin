package com.belive.dating.activities.introduction.upload_photo.policy

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.databinding.ActivityPhotoUploadPolicyBinding
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getGlide
import com.belive.dating.extensions.openBrowser
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import org.json.JSONObject

class PhotoUploadPolicyActivity : NetworkReceiverActivity() {

    val binding: ActivityPhotoUploadPolicyBinding by lazy {
        ActivityPhotoUploadPolicyBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(PhotoUploadPolicyActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        if (isFinishing) {
            mixPanel?.track(PhotoUploadPolicyActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", true)
            })
        } else {
            mixPanel?.track(PhotoUploadPolicyActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", false)
            })
        }
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

        binding.root.post {
            observeNetwork()
        }

        initViews()

        clickListeners()
    }

    private fun initViews() {
        binding.footerButtons.btnNext.text = StringBuilder().append("Read More")
        binding.isNextEnabled = true

        getGlide().load("https://www.love.belivedating.com/uploads/policy/1.jfif").centerCrop()
            .placeholder(R.drawable.logo_belive).into(binding.imgSample1)
        getGlide().load("https://www.love.belivedating.com/uploads/policy/2.jfif").centerCrop()
            .placeholder(R.drawable.logo_belive).into(binding.imgSample2)
        getGlide().load("https://www.love.belivedating.com/uploads/policy/3.jfif").centerCrop()
            .placeholder(R.drawable.logo_belive).into(binding.imgSample3)

        val guideLineList = arrayListOf<String>()
        guideLineList.add("Photos must <b>not</b> include <b>offensive, inappropriate, or explicit content</b>.")
        guideLineList.add("Avoid group photos, cartoons, or objects (e.g., logos) as profile pictures.")
        guideLineList.add("The photo should clearly show your <b>face</b> for easy identification.")
        guideLineList.add("Use a <b>high-resolution photo</b> with good lighting.")
        guideLineList.add("<b>Blurry, pixelated, or overly dark</b> photos are not allowed.")
        guideLineList.add("The photo must be <br>your own</b> or one you have permission to use.")
        guideLineList.add("Do not include <b>offensive gestures</b>, filters, or overlays.")
        guideLineList.add("Some sample allow photos are as above.")

        val guideLineAdapter = ImageGuidelineAdapter()
        guideLineAdapter.guidelineList = guideLineList

        binding.rvGuidelines.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvGuidelines.adapter = guideLineAdapter
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                swipeLeft()
            }
        })

        binding.footerButtons.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.footerButtons.btnNext.setOnClickListener {
            openBrowser("https://sites.google.com/view/profilephotopolicy-belive/profile-photo-policy")
        }
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        getGlide().load("https://www.love.belivedating.com/uploads/policy/1.jfif").centerCrop()
            .placeholder(R.drawable.logo_belive).into(binding.imgSample1)
        getGlide().load("https://www.love.belivedating.com/uploads/policy/2.jfif").centerCrop()
            .placeholder(R.drawable.logo_belive).into(binding.imgSample2)
        getGlide().load("https://www.love.belivedating.com/uploads/policy/3.jfif").centerCrop()
            .placeholder(R.drawable.logo_belive).into(binding.imgSample3)
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected) {
            getGlide().load("https://www.love.belivedating.com/uploads/policy/1.jfif").centerCrop()
                .placeholder(R.drawable.logo_belive).into(binding.imgSample1)
            getGlide().load("https://www.love.belivedating.com/uploads/policy/2.jfif").centerCrop()
                .placeholder(R.drawable.logo_belive).into(binding.imgSample2)
            getGlide().load("https://www.love.belivedating.com/uploads/policy/3.jfif").centerCrop()
                .placeholder(R.drawable.logo_belive).into(binding.imgSample3)
        }
    }
}
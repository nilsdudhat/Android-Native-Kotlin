package com.belive.dating.activities.dashboard.fragments.ai

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.belive.dating.R
import com.belive.dating.activities.EventBusFragment
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.activities.paywalls.subscriptions.subscription.SubscriptionActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.api.user.models.ai.AIProfilesResponse
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.FragmentAiBinding
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.navGraphViewModel
import com.belive.dating.extensions.setBackgroundAnimation
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeUp
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AIFragment : EventBusFragment(), AIAdapter.LastItemListener, AIAdapter.AIItemClickListener {

    val binding: FragmentAiBinding by lazy {
        FragmentAiBinding.inflate(layoutInflater)
    }

    val viewModel: AIViewModel by navGraphViewModel(R.id.navigation_ai)

    val adapter: AIAdapter by lazy {
        AIAdapter(viewModel, this@AIFragment, this@AIFragment)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.updateState()
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        viewModel.getState()
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()

        logger("--fragment--", "onDestroy: AIFragment")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.getState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logger("--fragment--", "onViewCreated: AIFragment")

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initViews()

        clickListeners()

        listenEvents()

        observeData()

        getAIProfiles()
    }

    private fun clickListeners() {
        binding.layoutPremium.setOnClickListener {
            startActivity(Intent(requireActivity(), SubscriptionActivity::class.java).apply {
                putExtra("is_gold_available", false)
                putExtra("restriction_message", "AI Match Maker is not available with Gold Subscriptions")
            })
            requireActivity().swipeUp()
        }
    }

    private fun getAIProfiles() {
        if (((requireActivity() as MainActivity).viewModel.isInitLoading.get() == false) && (viewModel.isDataLoaded.get() == false)) {
            lifecycleScope.launch(Dispatchers.IO) {
                getUserPrefs().countryCode?.let { countryCode ->
                    viewModel.getAIProfiles(countryCode).collectLatest {
                        appendProfiles(it)
                    }
                }
            }
        }
    }

    private fun initViews() {
        viewModel.isAIMatchAvailable.set(getUserPrefs().isAiMatchMaker)

        aiPremiumMessage()

        premiumBannerUI()
    }

    private fun aiPremiumMessage() {
        if (viewModel.isAIMatchAvailable.get() == true) {
            val message = "Discover over 10,000 AI recommended matches in one place."
            val spannableMessage = SpannableString(message)

            // Styling for "10,000 AI recommended matches"
            val boldPart = "10,000 AI recommended matches"
            val boldStart = message.indexOf(boldPart)
            val boldEnd = boldStart + boldPart.length
            spannableMessage.setSpan(StyleSpan(Typeface.BOLD), boldStart, boldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableMessage.setSpan(
                ForegroundColorSpan(requireContext().getColorFromAttr(android.R.attr.colorPrimary)),
                boldStart,
                boldEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.message = spannableMessage
        } else {
            val message = "Discover over 10,000 AI recommended matches in one place. Upgrade to premium today!"
            val spannableMessage = SpannableString(message)

            // Styling for "10,000 AI recommended matches"
            val boldPart = "10,000 AI recommended matches"
            val boldStart = message.indexOf(boldPart)
            val boldEnd = boldStart + boldPart.length
            spannableMessage.setSpan(StyleSpan(Typeface.BOLD), boldStart, boldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableMessage.setSpan(
                ForegroundColorSpan(requireContext().getColorFromAttr(android.R.attr.colorPrimary)),
                boldStart,
                boldEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Styling for "Upgrade to premium today!"
            val messageItalicPart = "Upgrade to premium today!"
            val messageItalicStart = message.indexOf(messageItalicPart)
            val messageItalicEnd = messageItalicStart + messageItalicPart.length
            spannableMessage.setSpan(StyleSpan(Typeface.ITALIC), messageItalicStart, messageItalicEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            binding.message = spannableMessage
        }
    }

    var currentIndex = 0

    val sentences = listOf(
        "Unlock Messaging",
        "Get Unlimited Likes",
        "Enjoy Unlimited Rewinds",
        "Receive More Gems Every Month",
        "Get More Super Likes Per Week",
        "Enjoy More Boost Per Month",
        "Earn an Exclusive Gold Badge",
        "Browse Without Ads",
        "See Who Likes You",
        "Use Location Filters",
        "Access the AI Matchmaker",
    )

    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {

            binding.txtFeature.post {

                val slideOut = ObjectAnimator.ofFloat(binding.txtFeature, "translationX", 0f, -binding.txtFeature.width.toFloat()).apply {
                    duration = 200
                }
                val slideIn = ObjectAnimator.ofFloat(binding.txtFeature, "translationX", binding.txtFeature.width.toFloat(), 0f).apply {
                    duration = 200
                }

                slideOut.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentIndex = (currentIndex + 1) % sentences.size
                        binding.feature = sentences[currentIndex] // Update text
                        binding.txtFeature.translationX = binding.txtFeature.width.toFloat() // Reset position
                        slideIn.start()
                    }
                })

                slideOut.start()
            }

            handler.postDelayed(this, 2000) // Next text update after 2s
        }
    }

    private fun premiumBannerUI() {
        viewModel.isAIMatchAvailable.set(getUserPrefs().isAiMatchMaker)

        val displayPremiumBanner: Boolean = if (viewModel.isAIMatchAvailable.get() == true) {
            false
        } else if (viewModel.isDataLoaded.get() == false) {
            true
        } else if ((viewModel.isDataLoaded.get() == true) && !viewModel.aiProfileList.value.isNullOrEmpty()) {
            true
        } else {
            false
        }

        binding.layoutPremium.clearAnimation()
        binding.txtFeature.clearAnimation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler.hasCallbacks(runnable)) {
                handler.removeCallbacks(runnable)
            }
        } else {
            handler.removeCallbacksAndMessages(null)
        }

        if (displayPremiumBanner) {
            val premiumTitle = "Enjoy Exclusive Features with Premium"
            val spannablePremiumTitle = SpannableString(premiumTitle)

            // Styling for "Enjoy Exclusive Features with Premium"
            val premiumTitleItalicStart = premiumTitle.indexOf(premiumTitle)
            val premiumTitleItalicEnd = premiumTitleItalicStart + premiumTitle.length
            spannablePremiumTitle.setSpan(
                StyleSpan(Typeface.ITALIC), premiumTitleItalicStart, premiumTitleItalicEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.premiumTitle = spannablePremiumTitle

            binding.feature = sentences[currentIndex]

            handler.postDelayed(runnable, 2000)

            binding.layoutPremium.setBackgroundAnimation(
                intArrayOf(
                    "#303F9F".toColorInt(),
                    "#1976D2".toColorInt(),
                    "#512DA8".toColorInt(),
                    "#C2185B".toColorInt(),
                    "#D32F2F".toColorInt(),
                )
            )
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.aiProfileList.collectLatest {
                if (it == null) {
                    // error OR sign out OR loading data
                } else {
                    // success
                    if (binding.rvAi.adapter !is AIAdapter) {
                        binding.rvAi.adapter = null
                        binding.rvAi.layoutManager = null
                        binding.rvAi.invalidate()

                        binding.rvAi.post {
                            // profile list available to display
                            binding.rvAi.layoutManager = adapter.gridLayoutManager
                            binding.rvAi.adapter = adapter
                        }
                    } else {
                        val previousItemCount = adapter.itemCount
                        adapter.notifyItemRangeInserted(previousItemCount, it.size)
                    }
                }
            }
        }
    }

    private fun appendProfiles(data: Resource<AIProfilesResponse?>) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (data.status) {
                Status.LOADING -> {

                }

                Status.SIGN_OUT -> {
                    Toast.makeText(getKoinContext(), "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ADMIN_BLOCKED -> {
                    Toast.makeText(getKoinContext(), "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ERROR -> {
                    Toast.makeText(getKoinContext(), data.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                }

                Status.SUCCESS -> {
                    viewModel.isDataLoaded.set(true)
                    viewModel.isLoading.set(false)

                    viewModel.isAIMatchAvailable.set(getUserPrefs().isAiMatchMaker)

                    aiPremiumMessage()

                    premiumBannerUI()

                    if (data.data != null) {
                        val list = ArrayList(viewModel.aiProfileList.value ?: arrayListOf())
                        data.data.aiProfileList.let { users ->
                            users.forEach { user ->
                                if (list.none { it.id == user.id }) {
                                    list.add(user)
                                }
                            }
                        }
                        viewModel.aiProfileList.emit(list)
                    }
                }
            }
        }
    }

    private fun authOut() {
        LoadingDialog.show(requireActivity())

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(requireActivity())

                startActivity(Intent(requireActivity(), SignInActivity::class.java))
                requireActivity().finishAffinity()
                requireActivity().swipeLeft()
            },
        )
    }

    override fun onLastItemListener() {
        if (viewModel.isAIMatchAvailable.get() == true) {
            if (viewModel.isDataLoaded.get() == true) {
                if (viewModel.isLoading.get() == false) {
                    viewModel.isLoading.set(true)

                    lifecycleScope.launch(Dispatchers.IO) {
                        getUserPrefs().countryCode?.let { countryCode ->
                            viewModel.getAIProfiles(countryCode).collectLatest {
                                appendProfiles(it)
                            }
                        }
                    }
                }
            }
        } else {
            if (adapter.itemCount > 12) {
                startActivity(Intent(requireActivity(), SubscriptionActivity::class.java).apply {
                    putExtra("is_gold_available", false)
                    putExtra("restriction_message", "AI Match Maker is not available with Gold Subscriptions")
                })
                requireActivity().swipeUp()
            }
        }
    }

    override fun onAIItemClick(holder: AIAdapter.ContentViewHolder) {
        val openDetails = if (viewModel.isAIMatchAvailable.get() == true) {
            true
        } else {
            adapter.getContentPosition(holder.bindingAdapterPosition) < getUserPrefs().aiRemainProfileCount
        }

        if (openDetails) {
            viewModel.aiProfileList.value?.get(adapter.getContentPosition(holder.bindingAdapterPosition))?.id?.let {
                (requireActivity() as MainActivity).openProfileDetails(it, "ai")
            }
        } else {
            startActivity(Intent(requireActivity(), SubscriptionActivity::class.java).apply {
                putExtra("is_gold_available", false)
                putExtra("restriction_message", "AI Match Maker is not available with Gold Subscriptions")
            })
            requireActivity().swipeUp()
        }
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.INIT_SUCCESS -> {
                viewModel.isAIMatchAvailable.set(getUserPrefs().isAiMatchMaker)

                getAIProfiles()
            }

            EventConstants.USER_DETAIL_PAGE_ACTION -> {

            }

            EventConstants.UPDATE_PURCHASE -> {
                viewModel.isAIMatchAvailable.set(getUserPrefs().isAiMatchMaker)

                viewModel.aiProfileList.value?.size?.let { adapter.notifyItemRangeChanged(0, it) }

                aiPremiumMessage()

                premiumBannerUI()
            }
        }
    }
}
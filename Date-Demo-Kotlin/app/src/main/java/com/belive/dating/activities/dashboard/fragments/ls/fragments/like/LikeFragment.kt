package com.belive.dating.activities.dashboard.fragments.ls.fragments.like

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.belive.dating.activities.EventBusFragment
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.activities.paywalls.subscriptions.subscription.SubscriptionActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.api.user.models.liked_me_profiles.LikedMeProfilesResponse
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.FragmentLikeBinding
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeUp
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LikeFragment : EventBusFragment(), LikeAdapter.LikeItemClickListener, LikeAdapter.LastItemListener {

    val binding: FragmentLikeBinding by lazy {
        FragmentLikeBinding.inflate(layoutInflater)
    }

    val viewModel: LikeViewModel by viewModels()

    val adapter: LikeAdapter by lazy {
        LikeAdapter(viewModel, this@LikeFragment, this@LikeFragment)
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

        logger("--fragment--", "onDestroy: LikeFragment")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.getState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logger("--fragment--", "onViewCreated: LikeFragment")

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initViews()

        listenEvents()

        observeData()

        getLikeFromProfiles()
    }

    private fun getLikeFromProfiles() {
        if (((requireActivity() as MainActivity).viewModel.isInitLoading.get() == false) && (viewModel.isDataLoaded.get() == false)) {
            getUserPrefs().countryCode?.let { countryCode ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getLikeProfiles(countryCode).collectLatest {
                        appendProfiles(it)
                    }
                }
            }
        }
    }

    private fun initViews() {

    }

    private fun observeData() {
        viewModel.isLoading.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                EventManager.postEvent(Event(EventConstants.DISPLAY_LS_PROGRESS, viewModel.isLoading.get()))
            }
        })

        lifecycleScope.launch {
            viewModel.likedMeProfileList.collectLatest {
                if (it != null) {
                    // success
                    if (binding.rvLike.adapter !is LikeAdapter) {
                        // profile list available to display
                        binding.rvLike.layoutManager = adapter.gridLayoutManager
                        binding.rvLike.adapter = adapter
                    }
                }
            }
        }
    }

    private fun appendProfiles(data: Resource<LikedMeProfilesResponse?>) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (data.status) {
                Status.LOADING -> {

                }

                Status.SIGN_OUT -> {
                    Toast.makeText(getKoinContext(), "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ADMIN_BLOCKED -> {
                    Toast.makeText(getKoinContext(), "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                    authOut()
                }

                Status.ERROR -> {
                    Toast.makeText(requireContext(), data.message ?: "Something went wrong...!", Toast.LENGTH_SHORT).show()
                }

                Status.SUCCESS -> {
                    viewModel.isDataLoaded.set(true)
                    viewModel.isLoading.set(false)

                    val list = ArrayList(viewModel.likedMeProfileList.value ?: arrayListOf())
                    data.data?.likedMeProfileList?.let { users ->
                        users.forEach { user ->
                            if (list.none { it.id == user.id }) {
                                list.add(user)
                            }
                        }
                    }
                    viewModel.likedMeProfileList.emit(list)
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
        if (getUserPrefs().isAiMatchMaker) {
            if (viewModel.isDataLoaded.get() == true) {
                if (viewModel.isLoading.get() == false) {
                    viewModel.isLoading.set(true)

                    lifecycleScope.launch(Dispatchers.IO) {
                        getUserPrefs().countryCode?.let { countryCode ->
                            viewModel.getLikeProfiles(countryCode).collectLatest {
                                appendProfiles(it)
                            }
                        }
                    }
                }
            }
        } else {
            if (adapter.itemCount > 12) {
                startActivity(Intent(getKoinActivity(), SubscriptionActivity::class.java).apply {
                    putExtra("is_gold_available", false)
                    putExtra("restriction_message", "You cannot see who like you with Gold Subscriptions")
                })
                getKoinActivity().swipeUp()
            }
        }
    }

    override fun onLikeItemClick(holder: LikeAdapter.ContentViewHolder) {
        if (getUserPrefs().isAiMatchMaker) {
            viewModel.likedMeProfileList.value?.get(holder.bindingAdapterPosition)?.id?.let {
                (requireActivity() as MainActivity).openProfileDetails(it, "like")
            }
        } else {
            startActivity(Intent(getKoinActivity(), SubscriptionActivity::class.java).apply {
                putExtra("is_gold_available", false)
                putExtra("restriction_message", "You cannot see who like you with Gold Subscriptions")
            })
            getKoinActivity().swipeUp()
        }
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.INIT_SUCCESS -> {
                getLikeFromProfiles()
            }

            EventConstants.UPDATE_PURCHASE -> {
                viewModel.likedMeProfileList.value?.size?.let { adapter.notifyItemRangeChanged(0, it) }
            }
        }
    }
}
package com.eventbus.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.eventbus.app.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class HomeFragment : Fragment() {
    private val appEventBus by inject<AppEventBus>()

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentHomeBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            // Send event with data
            appEventBus.emit(AppEvent.MyEventWithData(message = "Hello from Fragment!"))

            // Send event without data for performing any action
            appEventBus.emit(AppEvent.MyEventWithoutData)
        }
    }
}
package com.udemy.hilt.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class BlankFragment : Fragment() {

    @Inject
    @Named(Constants.FIREBASE)
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        userRepository.saveUser("Email", "Password")

        return inflater.inflate(R.layout.fragment_blank, container, false)
    }
}
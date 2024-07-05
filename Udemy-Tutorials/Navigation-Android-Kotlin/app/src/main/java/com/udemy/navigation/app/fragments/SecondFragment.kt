package com.udemy.navigation.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.udemy.navigation.app.R
import com.udemy.navigation.app.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private lateinit var binding: FragmentSecondBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            val args = requireArguments()
            if (args.containsKey("name")) {
                val name = args.getString("name")

                Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMove.setOnClickListener {
            val bundle = bundleOf(
                "name" to "Hello Manoj"
            )
            it.findNavController().navigate(R.id.action_secondFragment_to_firstFragment, bundle)
        }
    }
}
package com.udemy.navigation.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.udemy.navigation.app.R
import com.udemy.navigation.app.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
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
                "name" to "Hii Nilesh"
            )
            it.findNavController().navigate(R.id.action_firstFragment_to_secondFragment, bundle)
        }
    }
}
package com.udemy.journal.app.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.udemy.journal.app.R
import com.udemy.journal.app.databinding.ActivitySignUpBinding
import com.udemy.journal.app.utils.createFactory
import com.udemy.journal.app.viewmodels.SignUpFactory
import com.udemy.journal.app.viewmodels.SignUpViewModel

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.username = ""
        binding.email = ""
        binding.password = ""

        viewModel = ViewModelProvider(
            this,
            SignUpViewModel(SignUpFactory(this, binding)).createFactory()
        )[SignUpViewModel::class.java]
        binding.viewModel = viewModel
    }
}
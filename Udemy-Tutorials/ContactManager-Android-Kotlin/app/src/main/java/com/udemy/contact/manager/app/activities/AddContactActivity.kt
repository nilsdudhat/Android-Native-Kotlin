package com.udemy.contact.manager.app.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.udemy.contact.manager.app.R
import com.udemy.contact.manager.app.clickHandlers.AddContactActivityClickHandler
import com.udemy.contact.manager.app.database.Contact
import com.udemy.contact.manager.app.database.ContactViewModel
import com.udemy.contact.manager.app.databinding.ActivityAddContactBinding

class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        val viewModel: ContactViewModel = ViewModelProvider(this)[ContactViewModel::class.java]

        var contact: Contact? = Contact()
        val bundle: Bundle? = intent.extras

        if (bundle != null) {
            binding.isUpdate = true

            contact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable("", Contact::class.java)
            } else {
                bundle.getSerializable("contact") as Contact?
            }
        } else {
            binding.isUpdate = false
        }

        binding.contact = contact
        binding.viewModel = viewModel
        binding.saveClick = AddContactActivityClickHandler(
            activity = this,
            binding = binding,
        )
    }
}
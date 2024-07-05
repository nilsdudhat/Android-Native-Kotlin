package com.udemy.contact.manager.app.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.udemy.contact.manager.app.R
import com.udemy.contact.manager.app.adapter.ContactAdapter
import com.udemy.contact.manager.app.clickHandlers.MainActivityClickHandler
import com.udemy.contact.manager.app.database.Contact
import com.udemy.contact.manager.app.database.ContactViewModel
import com.udemy.contact.manager.app.databinding.ActivityMainBinding
import com.udemy.contact.manager.app.utils.ProgressUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var adapter: ContactAdapter? = null

    private var contactList: ArrayList<Contact> = ArrayList()

    private lateinit var contactViewModel: ContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        contactViewModel = ViewModelProvider(this)[ContactViewModel::class.java]
        binding.clickHandler = MainActivityClickHandler(this)

        ProgressUtil.showLoading(this)

        contactViewModel.getAllContacts().observe(this) { it: List<Contact>? ->
            ProgressUtil.hideLoading()

            contactList = if (it != null) {
                ArrayList(it)
            } else {
                ArrayList()
            }

            setUpRecyclerView()
        }
    }

    private fun setUpRecyclerView() {
        if (binding.rvContacts.layoutManager == null) {
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.rvContacts.layoutManager = layoutManager
        }
        binding.rvContacts.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

        if (adapter == null) {
            adapter = ContactAdapter(this, contactViewModel)
            binding.rvContacts.adapter = adapter
        }

        binding.isListEmpty = contactList.isEmpty()

        adapter?.setContactList(contactList)
    }
}
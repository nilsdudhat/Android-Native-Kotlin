package com.udemy.journal.app.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.udemy.journal.app.R
import com.udemy.journal.app.adapters.JournalAdapter
import com.udemy.journal.app.databinding.ActivityJournalsBinding
import com.udemy.journal.app.models.Journal
import com.udemy.journal.app.utils.ProgressUtils
import com.udemy.journal.app.utils.createFactory
import com.udemy.journal.app.viewmodels.JournalsFactory
import com.udemy.journal.app.viewmodels.JournalsViewModel

class JournalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJournalsBinding

    private lateinit var viewModel: JournalsViewModel

    private var journalAdapter: JournalAdapter? = null

    private val journalList = ArrayList<Journal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJournalsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.menu_sign_out) {
                viewModel.signOut()
            }
            true
        }

//        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(MainViewModel::class.java)
        viewModel = ViewModelProvider(
            this,
            JournalsViewModel(JournalsFactory(this)).createFactory()
        )[JournalsViewModel::class.java]
        binding.viewModel = viewModel

        ProgressUtils.showLoading(this)
        binding.isEmpty = false

        viewModel.getJournals().observe(this) {
            ProgressUtils.hideLoading()

            journalList.clear()

            if (it != null) {
                for (document in it) {
                    val journal: Journal = document.toObject(Journal::class.java)!!
                    journal.docID = document.id
                    journalList.add(journal)
                }
            }
            journalList.sortByDescending { journal -> journal.timeAdded }

            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        if (binding.rvJournals.layoutManager == null) {
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.rvJournals.layoutManager = layoutManager
        }

        binding.rvJournals.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        if (journalAdapter == null) {
            journalAdapter = JournalAdapter(viewModel)
            binding.rvJournals.adapter = journalAdapter
        }

        binding.isEmpty = journalList.isEmpty()
        journalAdapter!!.setJournals(journalList)
    }
}
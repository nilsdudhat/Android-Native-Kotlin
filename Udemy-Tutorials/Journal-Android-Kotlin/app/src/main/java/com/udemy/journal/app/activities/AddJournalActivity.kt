package com.udemy.journal.app.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.udemy.journal.app.R
import com.udemy.journal.app.databinding.ActivityAddJournalBinding
import com.udemy.journal.app.models.Journal
import com.udemy.journal.app.utils.createFactory
import com.udemy.journal.app.viewmodels.AddJournalFactory
import com.udemy.journal.app.viewmodels.AddJournalViewModel

class AddJournalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddJournalBinding

    private lateinit var viewModel: AddJournalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(
            this, AddJournalViewModel(AddJournalFactory(this, binding)).createFactory()
        )[AddJournalViewModel::class.java]
        binding.viewModel = viewModel

        val journal: Journal
        if (intent.hasExtra("journal")) {
            journal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("journal", Journal::class.java)!!
            } else {
                intent.getParcelableExtra("journal")!!
            }
            binding.isUpdate = true
        } else {
            journal = Journal()
            binding.isUpdate = false
        }
        binding.journal = journal
    }
}
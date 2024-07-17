package com.udemy.journal.app.viewmodels

import android.content.Intent
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.udemy.journal.app.R
import com.udemy.journal.app.activities.AddJournalActivity
import com.udemy.journal.app.activities.LoginActivity
import com.udemy.journal.app.databinding.DialogDeleteJournalBinding
import com.udemy.journal.app.databinding.DialogSignOutBinding
import com.udemy.journal.app.models.Journal
import com.udemy.journal.app.repositories.FirebaseRepository
import com.udemy.journal.app.utils.ProgressUtils
import com.udemy.journal.app.utils.commonDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class JournalsFactory(
    val activity: AppCompatActivity,
)

class JournalsViewModel(private val factory: JournalsFactory) : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    fun getJournals(): LiveData<List<DocumentSnapshot>> {
        return firebaseRepository.getDocuments("journals")
    }

    fun onAddJournalClick() {
        val intent = Intent(factory.activity, AddJournalActivity::class.java)
        factory.activity.startActivity(intent)
    }

    fun onJournalClick(journal: Journal) {
        val intent = Intent(factory.activity, AddJournalActivity::class.java)
        intent.putExtra("journal", journal)
        factory.activity.startActivity(intent)
    }

    fun signOut() {
        factory.activity.commonDialog(R.layout.dialog_sign_out, dialogBuilder = {
            val binding = DialogSignOutBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.apply {
                btnYes.setOnClickListener {
                    dismiss()

                    firebaseRepository.signOut()

                    val intent = Intent(factory.activity, LoginActivity::class.java)
                    factory.activity.startActivity(intent)
                    factory.activity.finish()
                }
                btnNo.setOnClickListener {
                    dismiss()
                }
            }
        })
    }

    fun popUpClicked(view: View, journal: Journal) {
        val popupMenu = PopupMenu(factory.activity, view)
        popupMenu.menuInflater.inflate(R.menu.menu_item_journal, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete -> {
                    popupMenu.dismiss()

                    factory.activity.commonDialog(R.layout.dialog_delete_journal, dialogBuilder = {
                        val binding = DialogDeleteJournalBinding.inflate(layoutInflater)
                        setContentView(binding.root)

                        binding.journal = journal

                        binding.apply {
                            btnYes.setOnClickListener {
                                dismiss()

                                ProgressUtils.showLoading(factory.activity)

                                viewModelScope.launch(Dispatchers.IO) {
                                    firebaseRepository.deleteDocument("journals", journal.docID)

                                    viewModelScope.launch(Dispatchers.Main) {
                                        ProgressUtils.hideLoading()
                                    }
                                }
                            }
                            btnNo.setOnClickListener {
                                dismiss()
                            }
                        }
                    })
                }
            }
            true
        }
        popupMenu.show()
    }
}
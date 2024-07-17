package com.udemy.journal.app.viewmodels

import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.udemy.journal.app.databinding.ActivityAddJournalBinding
import com.udemy.journal.app.models.Journal
import com.udemy.journal.app.repositories.FirebaseRepository
import com.udemy.journal.app.utils.ProgressUtils
import com.udemy.journal.app.utils.hideKeyboard
import com.udemy.journal.app.utils.isWebUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class AddJournalFactory(
    val activity: AppCompatActivity,
    val binding: ActivityAddJournalBinding,
)

class AddJournalViewModel(private val factory: AddJournalFactory) : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    private val cameraResultLauncher = factory.activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Log.d("--result--", "image: $uri")

            factory.binding.journal =
                factory.binding.journal?.copy(imageUrl = uri.toString())
        }
    }

    fun onAddImageClick() {
        // getting image from the library
        cameraResultLauncher.launch("image/*")
    }

    private suspend fun addJournal(journal: Journal) {
        firebaseRepository.getCurrentUserID()?.let { journal.copy(userID = it) }
        firebaseRepository.addDocument("journals", journal)
    }

    private suspend fun updateJournal(documentID: String, map: MutableMap<String, Any>) {
        firebaseRepository.updateDocument("journals", documentID, map)
    }

    private suspend fun uploadImage(fileName: String, imageUrl: String): String {
        return firebaseRepository.uploadImage("journal_images", fileName, imageUrl)
    }

    fun onSubmitClick(isUpdate: Boolean, journal: Journal) {
        factory.activity.hideKeyboard()
        ProgressUtils.showLoading(factory.activity)

        viewModelScope.launch(Dispatchers.IO) {
            if (isUpdate) {
                if (!isWebUrl(journal.imageUrl)) {
                    val fileName = Timestamp.now().seconds

                    val imageUrl = uploadImage(fileName.toString(), journal.imageUrl)
                    journal.imageUrl = imageUrl

                    Log.d("--submit--", "imageUrl: $imageUrl")
                }

                val map = mutableMapOf<String, Any>(
                    "imageUrl" to journal.imageUrl,
                    "thoughts" to journal.thoughts,
                    "title" to journal.title,
                )

                updateJournal(journal.docID, map)
            } else {
                val fileName = Timestamp.now().seconds

                val imageUrl = uploadImage(fileName.toString(), journal.imageUrl)
                journal.imageUrl = imageUrl

                Log.d("--submit--", "imageUrl: $imageUrl")

                addJournal(journal)
            }
            viewModelScope.launch(Dispatchers.Main) {
                ProgressUtils.hideLoading()
                factory.activity.finish()
            }
        }
    }
}
package com.udemy.journal.app.repositories

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.udemy.journal.app.models.Journal
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val firestore = lazy { Firebase.firestore }
    private val storage = lazy { Firebase.storage }
    private val firebaseAuth = lazy { FirebaseAuth.getInstance() }

    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.value.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    suspend fun signUpWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.value.createUserWithEmailAndPassword(email, password).await()
        return result.user
    }

    fun getDocuments(collectionName: String): LiveData<List<DocumentSnapshot>> {
        val liveData = MutableLiveData<List<DocumentSnapshot>>()
        firestore.value.collection(collectionName)
            .addSnapshotListener { snapshot, e ->
                if ((snapshot != null) && (e == null)) {
                    liveData.postValue(snapshot.documents)
                } else {
                    liveData.postValue(ArrayList())
                }
            }
        return liveData
    }

    suspend fun uploadImage(folderName: String, fileName: String, imageUri: String): String {
        val result = storage.value.reference.child(folderName)
            .child(fileName)
            .putFile(Uri.parse(imageUri))
            .await()

        return if (result.error == null) {
            result.metadata?.reference?.downloadUrl?.await().toString()
        } else {
            result.error!!.message.toString()
        }
    }

    suspend fun addDocument(
        collectionName: String,
        journal: Journal,
    ): DocumentReference {
        val result = firestore.value.collection(collectionName)
            .add(journal)
            .await()
        return result
    }

    suspend fun deleteDocument(collectionName: String, documentId: String) {
        firestore.value.collection(collectionName)
            .document(documentId)
            .delete()
            .await()
    }

    suspend fun updateDocument(
        collectionName: String,
        documentId: String,
        map: MutableMap<String, Any>,
    ) {
        firestore.value.collection(collectionName)
            .document(documentId)
            .update(map)
            .await()
    }

    fun signOut() {
        firebaseAuth.value.signOut()
    }

    fun getCurrentUserID(): String? {
        return firebaseAuth.value.currentUser?.uid
    }
}
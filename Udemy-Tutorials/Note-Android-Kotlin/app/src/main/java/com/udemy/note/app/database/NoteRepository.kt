package com.udemy.note.app.database

import android.content.Context
import androidx.lifecycle.LiveData

class NoteRepository(context: Context) {

    private val noteDao: NoteDao = NoteDatabase.getInstance(context).getNoteDao()

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun getNoteByTitle(title: String): Note {
        return noteDao.getNoteByTitle(title)
    }

    suspend fun isNoteExist(title: String): Boolean {
        return noteDao.isNoteExist(title) == 1
    }

    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }

    fun searchNote(query: String): LiveData<List<Note>> {
        return noteDao.searchNote(query)
    }

    fun getAllNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }
}
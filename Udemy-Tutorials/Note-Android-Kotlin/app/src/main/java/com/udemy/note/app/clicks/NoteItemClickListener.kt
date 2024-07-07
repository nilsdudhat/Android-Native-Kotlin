package com.udemy.note.app.clicks

import android.view.View
import androidx.navigation.Navigation
import com.udemy.note.app.database.Note
import com.udemy.note.app.fragments.HomeFragmentDirections

class NoteItemClickListener {

    fun onClick(view: View, note: Note) {
        val directions = HomeFragmentDirections.actionHomeFragmentToAddNoteFragment(note)
        Navigation.findNavController(view).navigate(directions)
    }
}
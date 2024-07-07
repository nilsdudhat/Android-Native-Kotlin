package com.udemy.note.app.clicks

import android.view.View
import androidx.navigation.Navigation
import com.udemy.note.app.R

class FabClickListener {

    fun onClick(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_addNoteFragment)
    }
}
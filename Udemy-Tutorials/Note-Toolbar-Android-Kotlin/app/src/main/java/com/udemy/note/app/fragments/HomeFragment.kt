package com.udemy.note.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.udemy.note.app.R
import com.udemy.note.app.activities.MainActivity
import com.udemy.note.app.adapters.NoteAdapter
import com.udemy.note.app.clicks.FabClickListener
import com.udemy.note.app.database.Note
import com.udemy.note.app.databinding.FragmentHomeBinding
import com.udemy.note.app.utils.ProgressUtil
import com.udemy.note.app.viewmodels.NoteViewModel

class HomeFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: NoteViewModel
    private var noteAdapter: NoteAdapter? = null
    private var noteList = ArrayList<Note>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            val menuSearch = menu.findItem(R.id.search).actionView as SearchView
            menuSearch.isSubmitButtonEnabled = false
            menuSearch.setOnQueryTextListener(this@HomeFragment)
        }

        binding.clickHandler = FabClickListener()

        viewModel = (requireActivity() as MainActivity).viewModel

        ProgressUtil.showLoading(requireContext())

        viewModel.getAllNotes().observe(requireActivity()) {
            ProgressUtil.hideLoading()

            noteList = ArrayList(it)
            updateList()
        }
    }

    private fun updateList() {
        if (binding.rvNotes.layoutManager == null) {
            binding.rvNotes.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        if (noteAdapter == null) {
            noteAdapter = NoteAdapter()
            binding.rvNotes.adapter = noteAdapter
        }

        noteAdapter?.setNoteList(noteList)

        binding.isEmpty = noteList.isEmpty()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        searchNote(query)
        return false
    }

    private fun searchNote(query: String?) {
        viewModel.searchNote("%$query%").observe(requireActivity()) {
            Log.d("--search--", "searchNote: " + it.size)
            noteList = ArrayList(it)
            updateList()
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchNote(newText)
        return true
    }
}
package com.udemy.note.app.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.udemy.note.app.R
import com.udemy.note.app.activities.MainActivity
import com.udemy.note.app.database.Note
import com.udemy.note.app.databinding.FragmentAddNoteBinding
import com.udemy.note.app.getDrawableValue
import com.udemy.note.app.hideKeyboard
import com.udemy.note.app.viewmodels.NoteViewModel

class AddNoteFragment : Fragment() {

    private lateinit var binding: FragmentAddNoteBinding
    private var note: Note? = null
    private var isUpdate = false
    private lateinit var viewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            if (requireArguments().containsKey("note")) {
                isUpdate = true

                note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requireArguments().getParcelable("note", Note::class.java)
                } else {
                    requireArguments().getParcelable("note")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (requireActivity() as MainActivity).viewModel
        binding.note = note
        binding.isUpdate = isUpdate

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })

        binding.apply {
            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener {
                Log.d("--menu_title--", "onViewCreated: " + it.title)

                requireActivity().hideKeyboard()

                if (this@AddNoteFragment.note == null) {
                    this@AddNoteFragment.note = Note(
                        title = binding.edtTitle.text.toString(),
                        body = binding.edtBody.text.toString(),
                    )
                }

                if (it.itemId == R.id.add_note) {
                    if (this@AddNoteFragment.isUpdate) {
                        viewModel.updateNote(this@AddNoteFragment.note!!).invokeOnCompletion {
                            navigateToHome()
                        }
                    } else {
                        viewModel.insertNote(this@AddNoteFragment.note!!).invokeOnCompletion {
                            navigateToHome()
                        }
                    }
                } else if (it.itemId == R.id.delete_note) {
                    viewModel.deleteNote(this@AddNoteFragment.note!!).invokeOnCompletion {
                        navigateToHome()
                    }
                }

                return@OnMenuItemClickListener true
            })

            val deleteOption = toolbar.menu.findItem(R.id.delete_note)
            deleteOption.isVisible = this@AddNoteFragment.isUpdate

            val addOption = toolbar.menu.findItem(R.id.add_note)

            if (this@AddNoteFragment.isUpdate) {
                addOption.icon = requireContext().getDrawableValue(R.drawable.ic_done)
                addOption.title = "DONE"
            } else {
                addOption.icon = requireContext().getDrawableValue(R.drawable.ic_add)
                addOption.title = "ADD"
            }
        }
    }

    fun navigateToHome() {
        Navigation.findNavController(requireView())
            .navigate(R.id.action_addNoteFragment_to_homeFragment)
    }
}
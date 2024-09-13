package com.demo.movie.tmdb.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.demo.movie.tmdb.app.activities.MainActivity
import com.demo.movie.tmdb.app.adapters.GridAdapter
import com.demo.movie.tmdb.app.databinding.FragmentGridBinding
import com.demo.movie.tmdb.app.interfaces.OnLastViewAttached
import com.demo.movie.tmdb.app.models.Movie

class GridFragment : Fragment(), OnLastViewAttached {

    lateinit var binding: FragmentGridBinding

    private var adapter: GridAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (!::binding.isInitialized) {
            binding = FragmentGridBinding.inflate(inflater, container, false)
            initRecyclerView()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).movieListData.observe(requireActivity()) {
            if (it == null) {
                return@observe
            }
            adapter?.setMovies(it)
            binding.isEmpty = it.isEmpty()
        }
    }

    private fun initRecyclerView() {
        Log.d("--fragment--", "initRecyclerView: ")
        if (binding.rvMovies.layoutManager == null) {
            binding.rvMovies.layoutManager = GridLayoutManager(requireContext(), 2)
        }

        if (adapter == null) {
            adapter = GridAdapter(
                this,
                (requireActivity() as MainActivity).mainViewModel.value,
            )
            binding.rvMovies.adapter = adapter
        }
    }

    override fun onLastViewAttach() {
        if ((requireActivity() as MainActivity).totalPages > (requireActivity() as MainActivity).currentPage) {
            (requireActivity() as MainActivity).getPopularMovies((requireActivity() as MainActivity).currentPage + 1)
        }
    }
}
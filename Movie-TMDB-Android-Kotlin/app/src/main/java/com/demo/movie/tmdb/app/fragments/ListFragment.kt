package com.demo.movie.tmdb.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.movie.tmdb.app.activities.MainActivity
import com.demo.movie.tmdb.app.adapters.ListAdapter
import com.demo.movie.tmdb.app.databinding.FragmentListBinding
import com.demo.movie.tmdb.app.interfaces.OnLastViewAttached
import com.demo.movie.tmdb.app.models.Movie

class ListFragment : Fragment(), OnLastViewAttached {

    private lateinit var binding: FragmentListBinding

    private var adapter: ListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).movieListData.observe(requireActivity()) {
            if (it == null) {
                return@observe
            }
            displayMovies(it)
        }
    }

    private fun displayMovies(moviesList: List<Movie>) {
        if (binding.rvMovies.layoutManager == null) {
            binding.rvMovies.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        if (adapter == null) {
            adapter = ListAdapter(this)
            binding.rvMovies.adapter = adapter
        }

        adapter?.setMovies(moviesList)
        binding.isEmpty = moviesList.isEmpty()
    }

    override fun onLastViewAttach() {
        if ((requireActivity() as MainActivity).totalPages > (requireActivity() as MainActivity).currentPage) {
            (requireActivity() as MainActivity).getPopularMovies((requireActivity() as MainActivity).currentPage + 1)
        }
    }
}
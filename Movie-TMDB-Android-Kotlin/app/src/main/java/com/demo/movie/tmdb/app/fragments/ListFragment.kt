package com.demo.movie.tmdb.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.movie.tmdb.app.activities.MainActivity
import com.demo.movie.tmdb.app.adapters.GridAdapter
import com.demo.movie.tmdb.app.adapters.ListAdapter
import com.demo.movie.tmdb.app.databinding.FragmentGridBinding
import com.demo.movie.tmdb.app.databinding.FragmentListBinding
import com.demo.movie.tmdb.app.interfaces.OnLastViewAttached
import com.demo.movie.tmdb.app.models.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListFragment : Fragment(), OnLastViewAttached {

    private val binding by lazy {
        FragmentListBinding.inflate(LayoutInflater.from(requireContext()))
    }

    private val adapter by lazy {
        ListAdapter(this, (requireActivity() as MainActivity).mainViewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        initRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (binding.rvMovies.layoutManager == null) {
            binding.rvMovies.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        binding.isSkeleton = true

        (requireActivity() as MainActivity).movieListData.observe(requireActivity()) {
            if (it == null) {
                return@observe
            }
            lifecycleScope.launch(Dispatchers.Main) {
                binding.isSkeleton = false

                binding.root.post {
                    initRecyclerView()

                    adapter.movieList = it
                    binding.isEmpty = it.isEmpty()
                }
            }
        }
    }

    private fun initRecyclerView() {
        Log.d("--fragment--", "initRecyclerView: ")
        if (binding.rvMovies.layoutManager == null) {
            binding.rvMovies.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        if (binding.rvMovies.tag == "skeleton") {
            binding.rvMovies.tag = null
            binding.rvMovies.adapter = adapter
        }
    }

    override fun onLastViewAttach() {
        if ((requireActivity() as MainActivity).totalPages > (requireActivity() as MainActivity).currentPage) {
            (requireActivity() as MainActivity).getPopularMovies((requireActivity() as MainActivity).currentPage + 1)
        }
    }
}
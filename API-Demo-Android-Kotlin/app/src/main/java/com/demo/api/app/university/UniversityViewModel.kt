package com.demo.api.app.university

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.api.app.ProgressUtils
import com.demo.api.app.databinding.ActivityUniversitiesBinding
import com.demo.api.app.getClient
import com.demo.api.app.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UniversityFactory(
    val activity: Activity,
    val binding: ActivityUniversitiesBinding,
)

class UniversityViewModel(
    private val universityFactory: UniversityFactory
) : ViewModel() {

    var countryName = ""
    private var adapter: UniversityAdapter? = null

    fun searchUniversities(countryName: String) {
        universityFactory.activity.hideKeyboard()

        if (countryName.isEmpty()) {
            universityFactory.binding.countryContainer.error = "Please enter Country Name"
            return
        }
        ProgressUtils.showLoading(universityFactory.activity)

        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                val retrofit = getClient("http://universities.hipolabs.com/")
                return@withContext retrofit.create(UniversityInterface::class.java)
                    .getUniversities(countryName)
            }

            ProgressUtils.hideLoading()

            if (response.isSuccessful && response.code() == 200 && response.body() != null) {
                setUpRecyclerView(response.body()!!)
                this@UniversityViewModel.countryName = countryName
            } else {
                Snackbar.make(
                    universityFactory.binding.root,
                    response.errorBody().toString(),
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun setUpRecyclerView(list: ArrayList<University>) {
        universityFactory.binding.rvUniversities.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(
                    universityFactory.activity,
                    LinearLayoutManager.VERTICAL,
                    false,
                )
            }
            addItemDecoration(
                DividerItemDecoration(
                    universityFactory.activity,
                    LinearLayoutManager.VERTICAL,
                )
            )
            if (adapter == null) {
                this@UniversityViewModel.adapter = UniversityAdapter()
                adapter = this@UniversityViewModel.adapter
            }
            this@UniversityViewModel.adapter!!.setList(list)
        }
        universityFactory.binding.isEmpty = list.isEmpty()
        universityFactory.binding.country = ""
    }
}
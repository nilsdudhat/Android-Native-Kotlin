package com.belive.dating.activities.report

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ReportViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "REPORT_VIEW_MODEL"

    val currentIndex = ObservableField(0)

    fun updateState() {
        savedStateHandle["${TAG}_currentIndex"] = currentIndex.get()
    }

    fun getState() {
        currentIndex.set(savedStateHandle["${TAG}_currentIndex"])
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    fun reportUser(jsonBody: JsonObject): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.reportUser(jsonBody)
                val errorBody = response.errorBody()?.string()

                logger("--report--", "request.url: ${response.raw().request.url}")
                logger("--report--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--report--", "code: ${response.code()}")
                logger("--report--", "isSuccessful: ${response.isSuccessful}")
                logger("--report--", "errorBody: $errorBody")
                logger("--report--", "body: ${gsonString(response.body())}")
                logger("--report--", "request: ${gsonString(jsonBody)}")

                if (response.isSuccessful) {
                    data.emit(Resource.success(response.body()))
                } else {
                    if (response.code() == 401) {
                        data.emit(Resource.signOut(getErrorMessage(errorBody), null))
                    } else if (response.code() == 403) {
                        data.emit(Resource.adminBlocked(getErrorMessage(errorBody), null))
                    } else {
                        if (!errorBody.isNullOrEmpty()) {
                            data.emit(Resource.error(getErrorMessage(errorBody), null))
                        } else {
                            data.emit(Resource.error("Something went wrong...!", null))
                        }
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }

    fun getReasons(): ArrayList<String> {
        val reasonList = arrayListOf<String>()
        reasonList.add("User Bio")
        reasonList.add("User Profile Photos")
        reasonList.add("An incident that occurred outside the Belive app or in person")
        return reasonList
    }

    fun getDetails(): HashMap<String, ArrayList<String>> {
        val details = hashMapOf<String, ArrayList<String>>()
        details["Profile Information Incorrect?"] = arrayListOf(
            "Fake profile, scammer, not one person.",
            "Someone is selling something.",
            "Someone under 18 is involved.",
        )
        details["Offensive or Unacceptable Conduct?"] = arrayListOf(
            "Nudity or something sexually explicit",
            "Abusive/hate/threatening behavior",
        )
        details["Physical Safety Concerns?"] = arrayListOf(
            "Possible threat to themselves or others",
        )
        details["Others?"] = arrayListOf(
            "Not mentioned in the above things",
        )
        return details
    }

    fun getSequenceList(): HashMap<Int, String> {
        val details = hashMapOf<Int, String>()
        details[0] = "Profile Information Incorrect?"
        details[1] = "Offensive or Unacceptable Conduct?"
        details[2] = "Physical Safety Concerns?"
        details[3] = "Others?"
        return details
    }
}
package com.belive.dating.activities.search_user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.search_user.User
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchUserViewModel : ViewModel() {

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    val searchData = MutableStateFlow<Resource<List<User>>>(Resource.loading(null))

    private val searchFlow = MutableSharedFlow<String>()
    private var searchJob: Job? = null

    init {
        searchFlow
            .debounce(1000) // Debounce for 1000 milliseconds
            .distinctUntilChanged()
            .onEach { query ->
                try {
                    val response = userRepository.searchUsers(query)
                    val errorBody = response.errorBody()?.string()

                    logger("--search--", "request.url: ${response.raw().request.url}")
                    logger("--search--", "request.body: ${gsonString(response.raw().request.body)}")
                    logger("--search--", "code: ${response.code()}")
                    logger("--search--", "isSuccessful: ${response.isSuccessful}")
                    logger("--search--", "errorBody: $errorBody")
                    logger("--search--", "body: ${gsonString(response.body())}")

                    if (response.isSuccessful) {
                        searchData.emit(Resource.success(response.body()?.userList))
                    } else {
                        if (response.code() == 401) {
                            searchData.emit(Resource.signOut(getErrorMessage(errorBody), null))
                        } else if (response.code() == 403) {
                            searchData.emit(Resource.adminBlocked(getErrorMessage(errorBody), null))
                        } else {
                            if (!errorBody.isNullOrEmpty()) {
                                searchData.emit(Resource.error(getErrorMessage(errorBody), null))
                            } else {
                                searchData.emit(Resource.error("Something went wrong", null))
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.message?.let { Resource.error(it, null) }?.let { searchData.emit(it) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun fetchUser(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (query.length < 3) {
                searchData.emit(Resource(Status.SUCCESS, null, null))
            } else {
                searchData.emit(Resource(Status.LOADING, null, null))

                searchJob?.cancel()
                searchJob = viewModelScope.launch(Dispatchers.Default) {
                    searchFlow.emit(query)
                }
            }
        }
    }
}
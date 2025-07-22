package com.belive.dating.activities.filter.location.search_location

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.add_location.AddLocationModel
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchByTextResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchLocationViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "SEARCH_LOCATION_VIEW_MODEL"

    val selectedPlace = MutableLiveData<Place?>(null)
    val searchedValue = ObservableField("")

    fun updateState() {
        savedStateHandle["${TAG}_searchedValue"] = searchedValue.get()
    }

    fun getState() {
        Places.initialize(getKoinActivity(), getGistPrefs().gcpKey)
        placesClient = Places.createClient(getKoinActivity())

        searchedValue.set(savedStateHandle["${TAG}_searchedValue"])
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var placesClient: PlacesClient? = null
    val placeData = MutableStateFlow<Resource<List<Place>>>(Resource.loading(null))

    private val searchFlow = MutableSharedFlow<String>()
    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    init {
        try {
            Places.initialize(getKoinActivity(), getGistPrefs().gcpKey)
            placesClient = Places.createClient(getKoinActivity())

            searchFlow
                .debounce(1000) // Debounce for 1000 milliseconds
                .distinctUntilChanged()
                .onEach { query ->
                    // Specify the list of fields to return.
                    val placeFields = listOf(
                        Place.Field.ID,
                        Place.Field.DISPLAY_NAME,
                        Place.Field.LOCATION,
                        Place.Field.ADDRESS_COMPONENTS,
                    )

                    // Use the builder to create a SearchByTextRequest object.
                    val searchByTextRequest = SearchByTextRequest.builder(query, placeFields)
                        .setMaxResultCount(3)
                        .setStrictTypeFiltering(true)
                        .build()

                    // Call PlacesClient.searchByText() to perform the search.
                    // Define a response handler to process the returned List of Place objects.
                    placesClient?.searchByText(searchByTextRequest)
                        ?.addOnSuccessListener { response: SearchByTextResponse ->
                            viewModelScope.launch(Dispatchers.IO) {
                                placeData.emit(Resource(Status.SUCCESS, response.places, null))
                            }
                        }
                        ?.addOnFailureListener {
                            viewModelScope.launch(Dispatchers.IO) {
                                placeData.emit(Resource(Status.ERROR, null, it.message))
                            }
                        }
                }
                .launchIn(viewModelScope)
        } catch (e: Exception) {
            catchLog("SearchLocationViewModel: ${gsonString(e)}")
        }
    }

    fun fetchPlaces(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (query.length < 3) {
                placeData.emit(Resource(Status.SUCCESS, null, null))
            } else {
                placeData.emit(Resource(Status.LOADING, null, null))

                job?.cancel()
                job = viewModelScope.launch(Dispatchers.Default) {
                    searchFlow.emit(query)
                }
            }
        }
    }

    fun addLocation(selectedPlace: Place): MutableStateFlow<Resource<AddLocationModel>> {
        val data = MutableStateFlow<Resource<AddLocationModel>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val mutableMap = mutableMapOf<String, String>()
                mutableMap["name"] = selectedPlace.displayName!!
                mutableMap["longitude"] = selectedPlace.location!!.longitude.toString()
                mutableMap["latitude"] = selectedPlace.location!!.latitude.toString()

                logger("--add_location--", "mutableMap: ${gsonString(mutableMap)}")

                val response = userRepository.addNewLocation(mutableMap)
                val errorBody = response.errorBody()?.string()

                logger("--add_location--", "request.url: ${response.raw().request.url}")
                logger("--add_location--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--add_location--", "code: ${response.code()}")
                logger("--add_location--", "isSuccessful: ${response.isSuccessful}")
                logger("--add_location--", "errorBody: $errorBody")
                logger("--add_location--", "body: ${gsonString(response.body())}")

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
}
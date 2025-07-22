package com.belive.dating.activities.edit_profile.relationship_goal

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.introduction.models.RelationshipGoalData
import com.belive.dating.api.introduction.models.RelationshipGoalsResponse
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.user.UserResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the "Edit Relationship Goal" screen.
 *
 * This ViewModel handles the logic for fetching relationship goals,
 * saving the user's selected relationship goal, and managing the UI state.
 * It uses SavedStateHandle to persist data across configuration changes.
 *
 * @property savedStateHandle Handle for saving and retrieving state across configuration changes.
 */
class EditRelationshipGoalViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_RELATIONSHIP_GOAL_VIEW_MODEL"

    val selectedRelationshipGoal = ObservableField<Int?>()
    val relationshipGoalList = ObservableField<ArrayList<RelationshipGoalData>?>()
    val isButtonEnabled = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_selectedRelationshipGoal"] = selectedRelationshipGoal.get()
        savedStateHandle["${TAG}_relationshipGoalList"] = gsonString(relationshipGoalList.get())
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
    }

    fun getState() {
        selectedRelationshipGoal.set(savedStateHandle["${TAG}_selectedRelationshipGoal"])
        relationshipGoalList.set(savedStateHandle.get<String>("${TAG}_relationshipGoalList")?.fromJson())
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getAllRelationshipGoals(): MutableStateFlow<Resource<RelationshipGoalsResponse?>> {
        val resource = MutableStateFlow<Resource<RelationshipGoalsResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.getAllRelationshipGoals()
            val errorBody = response.errorBody()?.string()

            logger("--relationship_goals--", "request.url: ${response.raw().request.url}")
            logger("--relationship_goals--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--relationship_goals--", "code: ${response.code()}")
            logger("--relationship_goals--", "isSuccessful: ${response.isSuccessful}")
            logger("--relationship_goals--", "errorBody: $errorBody")
            logger("--relationship_goals--", "body: ${gsonString(response.body())}")

            if (response.isSuccessful) {
                if (response.body() != null) {
                    resource.emit(Resource.success(response.body()))
                } else {
                    resource.emit(Resource.error("Something went wrong...!", null))
                }
            } else {
                if (!errorBody.isNullOrEmpty()) {
                    resource.emit(Resource.error(getErrorMessage(errorBody), null))
                } else {
                    resource.emit(Resource.error("Something went wrong...!", null))
                }
            }
        }

        return resource
    }

    fun saveRelationshipGoal(relationshipGoalId: Int): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("looking_for", relationshipGoalId)

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_relationship_goal--", "request.url: ${response.raw().request.url}")
            logger("--update_relationship_goal--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_relationship_goal--", "code: ${response.code()}")
            logger("--update_relationship_goal--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_relationship_goal--", "errorBody: $errorBody")
            logger("--update_relationship_goal--", "body: ${gsonString(response.body())}")
            logger("--update_relationship_goal--", "query: ${gsonString(json)}")

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
        }

        return data
    }
}
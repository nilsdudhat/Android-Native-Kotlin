package com.belive.dating.activities.introduction.relationship_goal

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionRepository
import com.belive.dating.api.introduction.models.RelationshipGoalsResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RelationshipGoalViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "RELATIONSHIP_GOAL_VIEW_MODEL"

    val isSkeleton = ObservableField(true)
    val isNextEnabled = ObservableField(false)
    val selectedRelationshipGoal = ObservableField(-1)

    fun updateState() {
        savedStateHandle["${TAG}_isSkeleton"] = isSkeleton.get()
        savedStateHandle["${TAG}_isNextEnabled"] = isNextEnabled.get()
        savedStateHandle["${TAG}_selectedRelationshipGoal"] = selectedRelationshipGoal.get()
        savedStateHandle["${TAG}_relationshipGoalList"] = gsonString(relationshipGoalResource.value)
    }

    fun getState() {
        isSkeleton.set(savedStateHandle["${TAG}_isSkeleton"])
        isNextEnabled.set(savedStateHandle["${TAG}_isNextEnabled"])
        selectedRelationshipGoal.set(savedStateHandle["${TAG}_selectedRelationshipGoal"])
        relationshipGoalResource.value =
            (savedStateHandle.get<String>("${TAG}_relationshipGoalList")?.fromJson(object : TypeToken<Resource<RelationshipGoalsResponse?>>() {}))
                ?: Resource.loading(null)
    }

    private val introductionRepository = getKoinObject().get<IntroductionRepository>()

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    val relationshipGoalResource = MutableStateFlow<Resource<RelationshipGoalsResponse?>>(Resource.loading(null))

    fun getAllRelationshipGoals() {
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = introductionRepository.getAllRelationshipGoals()
                val errorBody = response.errorBody()?.string()

                logger("--relationship_goal--", "request.url: ${response.raw().request.url}")
                logger("--relationship_goal--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--relationship_goal--", "code: ${response.code()}")
                logger("--relationship_goal--", "isSuccessful: ${response.isSuccessful}")
                logger("--relationship_goal--", "errorBody: $errorBody")
                logger("--relationship_goal--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    if (response.body() != null) {
                        relationshipGoalResource.emit(Resource.success(response.body()))
                    } else {
                        relationshipGoalResource.emit(Resource.error("Something went wrong...!", null))
                    }
                } else {
                    if (!errorBody.isNullOrEmpty()) {
                        relationshipGoalResource.emit(Resource.error(getErrorMessage(errorBody), null))
                    } else {
                        relationshipGoalResource.emit(Resource.error("Something went wrong...!", null))
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { relationshipGoalResource.emit(it) }
            }
        }
    }
}
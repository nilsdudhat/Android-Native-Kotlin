package com.belive.dating.activities.edit_profile.basics.family_plan

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.introduction.models.FamilyPlanData
import com.belive.dating.api.introduction.models.FamilyPlanResponse
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.user.UserResponse
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

class EditFamilyPlanViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_FAMILY_PLAN_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val familyPlanList = ObservableField<ArrayList<FamilyPlanData>?>()
    val selectedFamilyPlan = ObservableField<Int?>() // family plan id

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_familyPlanList"] = familyPlanList.get()
        savedStateHandle["${TAG}_selectedFamilyPlan"] = selectedFamilyPlan.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        familyPlanList.set(savedStateHandle["${TAG}_familyPlanList"])
        selectedFamilyPlan.set(savedStateHandle["${TAG}_selectedFamilyPlan"])
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getAllFamilyPlans(): MutableStateFlow<Resource<FamilyPlanResponse?>> {
        val resource = MutableStateFlow<Resource<FamilyPlanResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.getAllFamilyPlan()
            val errorBody = response.errorBody()?.string()

            logger("--family_plans--", "request.url: ${response.raw().request.url}")
            logger("--family_plans--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--family_plans--", "code: ${response.code()}")
            logger("--family_plans--", "isSuccessful: ${response.isSuccessful}")
            logger("--family_plans--", "errorBody: $errorBody")
            logger("--family_plans--", "body: ${gsonString(response.body())}")

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

    fun saveFamilyPlan(familyPlanId: Int?): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val json = JsonObject()
            json.addProperty("family_plan", familyPlanId)

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_family_plan--", "request.url: ${response.raw().request.url}")
            logger("--update_family_plan--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_family_plan--", "code: ${response.code()}")
            logger("--update_family_plan--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_family_plan--", "errorBody: $errorBody")
            logger("--update_family_plan--", "body: ${gsonString(response.body())}")
            logger("--update_family_plan--", "query: ${gsonString(json)}")

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
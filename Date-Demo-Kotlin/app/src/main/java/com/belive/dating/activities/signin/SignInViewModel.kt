package com.belive.dating.activities.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.gist.GISTRepository
import com.belive.dating.api.introduction.IntroductionRepository
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.safeApiCallResponse
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SignInViewModel : ViewModel(), KoinComponent {

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun signIn(mail: String?, deviceId: String?, fcmToken: String?): MutableStateFlow<Resource<Pair<Int, String?>>> {
        val data = MutableStateFlow<Resource<Pair<Int, String?>>>(Resource.loading(null))

        job = viewModelScope.launch {
            try {
                val introductionRepository = getKoinObject().get<IntroductionRepository>()

                val response = introductionRepository.signIn(mail, deviceId, fcmToken)
                val errorBody = response.errorBody()?.string()

                logger("--sign_in--", "request.url: ${response.raw().request.url}")
                logger("--sign_in--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--sign_in--", "code: ${response.code()}")
                logger("--sign_in--", "isSuccessful: ${response.isSuccessful}")
                logger("--sign_in--", "errorBody: $errorBody")
                logger("--sign_in--", "body: ${gsonString(response.body())}")

                data.emit(Resource.success(Pair(response.code(), response.body()?.getAsJsonPrimitive("data")?.asString)))
            } catch (e: Exception) {
                logger("--sign_in--", "catch: ${gsonString(e)}")

                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }

    fun getRaw(): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val repository = getKoinObject().get<GISTRepository>() // Retrieve the repository
                val response = safeApiCallResponse {
                    repository.getRaw()
                }
                val errorBody = response.errorBody()?.string()

                logger("--gist--", "request.url: ${response.raw().request.url}")
                logger("--gist--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--gist--", "code: ${response.code()}")
                logger("--gist--", "isSuccessful: ${response.isSuccessful}")
                logger("--gist--", "errorBody: $errorBody")
                logger("--gist--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    if (response.body() != null) {
                        data.emit(Resource.success(response.body()))
                    } else {
                        data.emit(Resource.error("Something went wrong...!", null))
                    }
                } else {
                    if (!errorBody.isNullOrEmpty()) {
                        data.emit(Resource.error(getErrorMessage(errorBody), null))
                    } else {
                        data.emit(Resource.error("Something went wrong...!", null))
                    }
                }
            } catch (e: Exception) {
                logger("--gist--", "body: ${gsonString(e)}")

                data.emit(Resource.error("Something went wrong...!", null))
            }
        }

        return data
    }
}
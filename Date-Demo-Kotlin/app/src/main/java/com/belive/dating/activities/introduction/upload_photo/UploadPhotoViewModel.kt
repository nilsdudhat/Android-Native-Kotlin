package com.belive.dating.activities.introduction.upload_photo

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionRepository
import com.belive.dating.api.nsfw.NSFWRepository
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.prepareImagePart
import com.google.gson.JsonObject
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

enum class PhotoState {
    PHOTOS_PENDING, PHOTOS_REJECTED, PHOTOS_VERIFIED, SELFIE_PENDING, SELFIE_VERIFIED, UPLOADING_DATA,
}

class UploadPhotoViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "UPLOAD_PHOTO_VIEW_MODEL"

    val isNextEnabled = ObservableField(false)
    val validationError = ObservableField("")
    val photoCount = ObservableField(0)
    val uploadList = ObservableField<ArrayList<PhotoValidationModel>>()
    val selfieFile = ObservableField<File>()
    val selfiePath = ObservableField<String>()
    val photoState = ObservableField(PhotoState.PHOTOS_PENDING)

    fun updateState() {
        savedStateHandle["${TAG}_isNextEnabled"] = isNextEnabled.get()
        savedStateHandle["${TAG}_validationError"] = validationError.get()
        savedStateHandle["${TAG}_photosCount"] = photoCount.get()
        savedStateHandle["${TAG}_uploadList"] = gsonString(uploadList.get())
        savedStateHandle["${TAG}_selfieFile"] = selfieFile.get()
        savedStateHandle["${TAG}_selfiePath"] = selfiePath.get()
        savedStateHandle["${TAG}_photoState"] = photoState.get()
    }

    fun getState() {
        isNextEnabled.set(savedStateHandle["${TAG}_isNextEnabled"])
        validationError.set(savedStateHandle["${TAG}_validationError"])
        photoCount.set(savedStateHandle["${TAG}_photosCount"])
        uploadList.set(savedStateHandle.get<String>("${TAG}_uploadList")?.fromJson<ArrayList<PhotoValidationModel>>())
        selfieFile.set(savedStateHandle["${TAG}_selfieFile"])
        selfiePath.set(savedStateHandle["${TAG}_selfiePath"])
        photoState.set(savedStateHandle["${TAG}_photoState"])
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    val nsfwRepository by lazy {
        getKoinObject().get<NSFWRepository>()
    }

    // Detect Face using ML Kit
    suspend fun detectFace(bitmap: Bitmap?): Boolean = withContext(Dispatchers.Default) {
        if (bitmap == null) {
            return@withContext false
        }

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()

        val detector = FaceDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)

        try {
            val faces = detector.process(image).await()

            logger("--validate--", "faces: ${faces.size}")

            faces.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Check NSFW using Retrofit
    suspend fun checkNSFW(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val image = prepareImagePart("image", path)

            val response = nsfwRepository.verifyNSFW(image)
            val errorBody = response.errorBody()?.string()

            logger("--nsfw--", "url: ${response.raw().request.url}")
            logger("--nsfw--", "isSuccessful: " + response.isSuccessful)
            logger("--nsfw--", "message: " + response.message())
            logger("--nsfw--", "body: " + gsonString(response.body()))
            logger("--nsfw--", "errorBody: $errorBody")
            logger("--nsfw--", "code: " + response.code())

            if (response.isSuccessful) {
                val body = response.body()?.asJsonObject

                if (body != null) {
                    val isNSFW = body.getAsJsonPrimitive("isNude").asBoolean

                    logger("--validate--", "isNSFW: $isNSFW")

                    return@withContext !isNSFW
                } else {
                    return@withContext false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            logger("--nsfw--", gsonString(e))
            return@withContext false
        }
    }

    suspend fun verifyMultiColor(bitmap: Bitmap?): Boolean = withContext(Dispatchers.IO) {
        if (bitmap == null) {
            return@withContext false
        }

        val scaledBitmap = bitmap.scale(100, 100) // scale small for speed
        val colorCountMap = mutableMapOf<Int, Int>()
        val totalPixels = scaledBitmap.width * scaledBitmap.height

        for (x in 0 until scaledBitmap.width) {
            for (y in 0 until scaledBitmap.height) {
                val pixelColor = scaledBitmap[x, y]

                if (Color.alpha(pixelColor) < 128) continue // ignore transparent

                val rgbColor = Color.rgb(
                    (Color.red(pixelColor) / 64) * 64,   // <<<<< Stronger rounding
                    (Color.green(pixelColor) / 64) * 64,
                    (Color.blue(pixelColor) / 64) * 64
                )

                val currentCount = colorCountMap[rgbColor] ?: 0
                colorCountMap[rgbColor] = currentCount + 1
            }
        }

        // Sort colors by percentage
        val colorPercentageMap = mutableMapOf<Int, Float>()
        for ((color, count) in colorCountMap) {
            colorPercentageMap[color] = (count.toFloat() / totalPixels) * 100f
        }

        val sortedMap = colorPercentageMap.toList()
            .sortedByDescending { (_, value) -> value }
            .toMap()

        logger("--validate--", "Dominant Colors: ${gsonString(sortedMap)}")

        // Dominant color should NOT cover too much area (example: < 75%)
        return@withContext sortedMap.values.first() < 75
    }

    val introductionRepository by lazy {
        getKoinObject().get<IntroductionRepository>()
    }

    fun signUp(
        fullName: RequestBody?,
        email: RequestBody?,
        gender: RequestBody?,
        birthDate: RequestBody?,
        seeingInterest: RequestBody?,
        orientationId: RequestBody?,
        myInterests: RequestBody?,
        relationshipGoal: RequestBody?,
        loginType: RequestBody?,
        deviceToken: RequestBody?,
        fcmToken: RequestBody?,
        versionName: RequestBody?,
        images: List<MultipartBody.Part>,
        selfie: List<MultipartBody.Part>,
    ): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = introductionRepository.signUp(
                    fullName = fullName,
                    email = email,
                    gender = gender,
                    birthDate = birthDate,
                    seeingInterest = seeingInterest,
                    orientationId = orientationId,
                    myInterests = myInterests,
                    relationshipGoal = relationshipGoal,
                    loginType = loginType,
                    deviceToken = deviceToken,
                    fcmToken = fcmToken,
                    versionName = versionName,
                    images = images,
                    selfie = selfie,
                )
                val errorBody = response.errorBody()?.string()

                logger("--sign_up--", "request.url: ${response.raw().request.url}")
                logger("--sign_up--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--sign_up--", "code: ${response.code()}")
                logger("--sign_up--", "isSuccessful: ${response.isSuccessful}")
                logger("--sign_up--", "errorBody: $errorBody")
                logger("--sign_up--", "body: ${gsonString(response.body())}")

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
                catchLog("--sign_up-- ${gsonString(e)}")

                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }
}
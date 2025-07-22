package com.belive.dating.activities.rejection

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.activities.introduction.upload_photo.PhotoValidationModel
import com.belive.dating.api.nsfw.NSFWRepository
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.edit_images.EditImagesResponse
import com.belive.dating.api.user.models.images.ImagesResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getMediaType
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
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

enum class ProfileState {
    PHOTOS_PENDING, PHOTOS_REJECTED, PHOTOS_VERIFIED, SELFIE_PENDING, SELFIE_TAKEN, SELFIE_VERIFIED, UPLOADING_DATA,
}

class PhotosRejectionViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "PHOTOS_REJECTION_VIEW_MODEL"

    val photoList = ObservableField<ArrayList<PhotoValidationModel>>()
    val photoCount = ObservableField(0)
    val isNextButtonEnabled = ObservableField(false)
    val validationError = ObservableField("")
    val isSelfieRequired = ObservableField(true)
    val selfieFile = ObservableField<File>()
    val selfiePath = ObservableField<String>()
    val profileState = ObservableField(ProfileState.PHOTOS_REJECTED)

    fun updateState() {
        savedStateHandle["${TAG}_photoList"] = gsonString(photoList.get())
        savedStateHandle["${TAG}_photosCount"] = photoCount.get()
        savedStateHandle["${TAG}_isNextButtonEnabled"] = isNextButtonEnabled.get()
        savedStateHandle["${TAG}_validationError"] = validationError.get()
        savedStateHandle["${TAG}_isSelfieRequired"] = isSelfieRequired.get()
        savedStateHandle["${TAG}_selfieFile"] = selfieFile.get()
        savedStateHandle["${TAG}_selfiePath"] = selfiePath.get()
        savedStateHandle["${TAG}_profileState"] = profileState.get()
    }

    fun getState() {
        photoList.set(savedStateHandle.get<String>("${TAG}_photoList")?.fromJson<ArrayList<PhotoValidationModel>>())
        photoCount.set(savedStateHandle["${TAG}_photosCount"])
        isNextButtonEnabled.set(savedStateHandle["${TAG}_isNextButtonEnabled"])
        validationError.set(savedStateHandle["${TAG}_validationError"])
        isSelfieRequired.set(savedStateHandle["${TAG}_isSelfieRequired"])
        selfieFile.set(savedStateHandle["${TAG}_selfieFile"])
        selfiePath.set(savedStateHandle["${TAG}_selfiePath"])
        profileState.set(savedStateHandle["${TAG}_profileState"])
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getImages(): MutableStateFlow<Resource<ImagesResponse>> {
        val data = MutableStateFlow<Resource<ImagesResponse>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getImages()
                val errorBody = response.errorBody()?.string()

                logger("--images--", "request.url: ${response.raw().request.url}")
                logger("--images--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--images--", "code: ${response.code()}")
                logger("--images--", "isSuccessful: ${response.isSuccessful}")
                logger("--images--", "errorBody: $errorBody")
                logger("--images--", "body: ${gsonString(response.body())}")

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
                            data.emit(Resource.error("Something went wrong", null))
                        }
                    }
                }
            } catch (e: Exception) {
                data.emit(Resource.error("Something went wrong", null))
            }
        }

        return data
    }

    fun updateImages(selfie: MultipartBody.Part?): MutableStateFlow<Resource<EditImagesResponse>> {
        val data = MutableStateFlow<Resource<EditImagesResponse>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                var img1: RequestBody? = null
                var img2: RequestBody? = null
                var img3: RequestBody? = null
                var img4: RequestBody? = null
                var img5: RequestBody? = null
                var img6: RequestBody? = null
                val images: MutableList<MultipartBody.Part> = ArrayList()

                val list = photoList.get()?.apply {
                    removeAll { it.path == null }
                }

                logger("--image--", "photoList: ${gsonString(list)}")

                list?.forEachIndexed { index, photo ->
                    if (photo.path != null) {
                        if (photo.isNetworkImage) {
                            if (index + 1 == 1) {
                                img1 = photo.path.toRequestBody(getMediaType())
                            } else if (index + 1 == 2) {
                                img2 = photo.path.toRequestBody(getMediaType())
                            } else if (index + 1 == 3) {
                                img3 = photo.path.toRequestBody(getMediaType())
                            } else if (index + 1 == 4) {
                                img4 = photo.path.toRequestBody(getMediaType())
                            } else if (index + 1 == 5) {
                                img5 = photo.path.toRequestBody(getMediaType())
                            } else if (index + 1 == 6) {
                                img6 = photo.path.toRequestBody(getMediaType())
                            }
                        } else {
                            images.add(prepareImagePart("img_${index + 1}", photo.path))
                        }
                    }
                }

                logger("--update_images--", "img1: ${gsonString(img1)}")
                logger("--update_images--", "img2: ${gsonString(img2)}")
                logger("--update_images--", "img3: ${gsonString(img3)}")
                logger("--update_images--", "img4: ${gsonString(img4)}")
                logger("--update_images--", "img5: ${gsonString(img5)}")
                logger("--update_images--", "img6: ${gsonString(img6)}")
                logger("--update_images--", "images: ${gsonString(images)}")
                logger("--update_images--", "selfie: ${gsonString(selfie)}")

                val response = userRepository.updateImages(img1, img2, img3, img4, img5, img6, images, selfie)
                val errorBody = response.errorBody()?.string()

                logger("--update_images--", "url: ${response.raw().request.url}")
                logger("--update_images--", "isSuccessful: " + response.isSuccessful)
                logger("--update_images--", "message: " + response.message())
                logger("--update_images--", "body: " + gsonString(response.body()))
                logger("--update_images--", "errorBody: $errorBody")
                logger("--update_images--", "code: " + response.code())

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
                logger("--update_images--", "error: " + e.message)

                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
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

    fun readNotification(notificationId: Int): MutableStateFlow<Resource<JsonObject>> {
        val data = MutableStateFlow<Resource<JsonObject>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.readNotification(notificationId)
                val errorBody = response.errorBody()?.string()

                logger("--notification--", "url: ${response.raw().request.url}")
                logger("--notification--", "isSuccessful: " + response.isSuccessful)
                logger("--notification--", "message: " + response.message())
                logger("--notification--", "body: " + gsonString(response.body()))
                logger("--notification--", "errorBody: $errorBody")
                logger("--notification--", "code: " + response.code())

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
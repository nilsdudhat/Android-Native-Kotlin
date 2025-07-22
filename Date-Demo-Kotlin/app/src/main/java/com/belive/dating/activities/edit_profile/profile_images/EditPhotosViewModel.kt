package com.belive.dating.activities.edit_profile.profile_images

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.activities.introduction.upload_photo.PhotoValidationModel
import com.belive.dating.api.google_vision.GoogleVisionRepository
import com.belive.dating.api.google_vision.models.CombinedRequest
import com.belive.dating.api.google_vision.models.Feature
import com.belive.dating.api.google_vision.models.Image
import com.belive.dating.api.google_vision.models.Request
import com.belive.dating.api.nsfw.NSFWRepository
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.edit_images.EditImagesResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getMediaType
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.prepareImagePart
import com.belive.dating.extensions.safeApiCallResponse
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

/**
 * ViewModel for managing the editing of user photos.
 *
 * This ViewModel handles operations related to updating user profile images,
 * including managing the list of photos, validating changes, and communicating
 * with the backend services for updating images and performing image analysis
 * using Google Cloud Vision API.
 *
 * @property savedStateHandle A handle to saved state passed down to the view model.
 *                           Used for persisting data across process death.  It stores and retrieves
 *                           the `photoList` and `photoCount`.
 */
class EditPhotosViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_PROFILE_VIEW_MODEL"

    val previousList = ObservableField(ArrayList<String>())
    val photoList = ObservableField(ArrayList<PhotoValidationModel>())
    val validationError = ObservableField("")
    val photoCount = ObservableField(0)
    val isImagesUpdated = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_photoList"] = photoList.get()
        savedStateHandle["${TAG}_photoCount"] = photoCount.get()
    }

    fun getState() {
        photoList.set(savedStateHandle["${TAG}_photoList"])
        photoCount.set(savedStateHandle["${TAG}_photoCount"])
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun updateImages(): MutableStateFlow<Resource<EditImagesResponse>> {
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

                logger("--image--", "img1: ${gsonString(img1)}")
                logger("--image--", "img2: ${gsonString(img2)}")
                logger("--image--", "img3: ${gsonString(img3)}")
                logger("--image--", "img4: ${gsonString(img4)}")
                logger("--image--", "img5: ${gsonString(img5)}")
                logger("--image--", "img6: ${gsonString(img6)}")
                logger("--image--", "images: ${gsonString(images)}")

                val response = userRepository.updateImages(img1, img2, img3, img4, img5, img6, images)
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
}
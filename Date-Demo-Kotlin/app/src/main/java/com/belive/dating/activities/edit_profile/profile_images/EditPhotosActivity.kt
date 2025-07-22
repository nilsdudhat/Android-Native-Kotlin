package com.belive.dating.activities.edit_profile.profile_images

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.MenuItem
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.Observable
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.introduction.upload_photo.PhotoValidationModel
import com.belive.dating.activities.introduction.upload_photo.Reject
import com.belive.dating.activities.introduction.upload_photo.policy.PhotoUploadPolicyActivity
import com.belive.dating.activities.profile.ProfileActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.api.user.models.user.UserImage
import com.belive.dating.constants.EventConstants
import com.belive.dating.constants.IntroductionConstants
import com.belive.dating.databinding.ActivityEditPhotosBinding
import com.belive.dating.di.googleVisionModule
import com.belive.dating.di.profileViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.askPermissions
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getBitmapFromLocalPath
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getRealPathFromUri
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.hasPermission
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.processImage
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.extensions.validateResolutionOfImage
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.event_management.Event
import com.belive.dating.helpers.helper_functions.event_management.EventManager
import com.belive.dating.helpers.helper_functions.grid_layout_manager.WrapperGridLayoutManager
import com.google.gson.JsonObject
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.context.unloadKoinModules
import java.io.File
import java.util.Collections

/**
 * Activity for editing user photos.
 *
 * This activity allows users to view, reorder, delete, and add photos to their profile.
 * It implements features such as image picking, cropping, compression, Google Vision API integration for image validation,
 */
class EditPhotosActivity : NetworkReceiverActivity(), EditPhotoAdapter.OnImageClickListener {

    val binding: ActivityEditPhotosBinding by lazy {
        ActivityEditPhotosBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: EditPhotosViewModel

    private val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
    )
    else arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    private val photoAdapter by lazy {
        EditPhotoAdapter(viewModel, this)
    }

    private val PERMISSION_CODE = 103

    private val permissionDialog: Dialog by lazy {
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.dialog_permission_required)
            setCanceledOnTouchOutside(true)
            window?.setDimAmount(0.75f)
            window?.apply {
                setLayout(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(android.R.color.transparent)
            }
            val messageView = findViewById<TextView>(R.id.txt_message_permission)
            messageView.text =
                StringBuilder().append("To select images from your device, we need access to your media storage. Please click below and allow it from settings.")
        }
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(ProfileActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(ProfileActivity::class.java.simpleName)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.updateState()
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        viewModel.getState()
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        viewModel = tryKoinViewModel(listOf(profileViewModel))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        initViews()

        clickListeners()

        observers()
    }

    private fun observers() {
        viewModel.photoList.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                photoCount()
            }
        })
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                unloadKoinModules(googleVisionModule)

                finish()
                swipeLeft()
            }
        })

        binding.btnPolicy.setOnClickListener {
            startActivity(Intent(this, PhotoUploadPolicyActivity::class.java))
            swipeRight()
        }

        binding.btnSave.setOnClickListener {
            updateImages()
        }
    }

    private fun updateImages() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.updateImages().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@EditPhotosActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@EditPhotosActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@EditPhotosActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()
                            Toast.makeText(this@EditPhotosActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                val newImageList = ArrayList<UserImage>()

                                it.data.userModel.editedImageList.forEachIndexed { index, editedImage ->
                                    newImageList.add(
                                        index,
                                        UserImage(image = editedImage.editedImage, position = index, verification = "pending"),
                                    )
                                }

                                getUserPrefs().userImages = newImageList
                                getUserPrefs().completeProfilePercentage = it.data.userModel.completeProfilePer

                                EventManager.postEvent(Event(EventConstants.UPDATE_IMAGES, null))
                                EventManager.postEvent(Event(EventConstants.UPDATE_PROFILE_PERCENTAGE, null))

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@EditPhotosActivity, "Something went wrong...!!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        setUpRecyclerView()
    }

    private fun initiateUserPhotos() {
        if (viewModel.photoList.get().isNullOrEmpty()) {
            for (index in 1..IntroductionConstants.UPLOAD_PHOTO_LIMIT) {
                photoAdapter.insertPlaceholder()
            }

            val photoList = getUserPrefs().userImages

            photoList?.forEachIndexed { index, photo ->
                val photoModel = PhotoValidationModel(path = photo.image, isNetworkImage = true)
                viewModel.photoList.get()?.set(index, photoModel)
                viewModel.previousList.set(viewModel.previousList.get()?.apply {
                    add(photo.image)
                })
            }
        }

        photoAdapter.notifyItemRangeChanged(0, viewModel.photoList.get()!!.size)

        photoCount()
    }

    private fun setUpRecyclerView() {
        if (binding.rvPhotos.layoutManager == null) {
            binding.rvPhotos.layoutManager = WrapperGridLayoutManager(this, 3)
        }
        if (binding.rvPhotos.adapter == null) {
            binding.rvPhotos.adapter = photoAdapter
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0 // No swipe action
        ) {
            override fun isLongPressDragEnabled(): Boolean {
                return true // Enable long-press to start dragging
            }

            override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                // Disable dragging for photos holder only
                return if (viewHolder.bindingAdapterPosition >= viewModel.photoCount.get()!!) 0 else super.getDragDirs(recyclerView, viewHolder)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                logger("--drag--", "onMove: ${viewHolder.bindingAdapterPosition} to ${target.bindingAdapterPosition}")

                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                // Prevent dragging for photos holder only
                if (fromPosition >= viewModel.photoCount.get()!! || toPosition >= viewModel.photoCount.get()!!) {
                    return false
                }

                // Swap the items in your data list
                val list = viewModel.photoList.get()!!
                Collections.swap(list, fromPosition, toPosition)
                viewModel.photoList.set(list)

                // Notify adapter about the moved item
                photoAdapter.notifyItemMoved(fromPosition, toPosition)
                if (fromPosition < toPosition) {
                    photoAdapter.notifyItemRangeChanged(fromPosition, toPosition + 1)
                } else {
                    photoAdapter.notifyItemRangeChanged(toPosition, fromPosition + 1)
                }

                checkUploadError()
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // No swipe action needed

                logger("--drag--", "onSwiped: ${viewHolder.bindingAdapterPosition}")
            }
        }

        // Attach the ItemTouchHelper to RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvPhotos)
    }

    private var selectedImageList = ArrayList<String>()
    private var croppedImageList = ArrayList<String>()

    private var lstTemp = ArrayList<PhotoValidationModel>()

    private var cropPosition = 0
    private var position = 0

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { it ->
        if (it.resultCode == RESULT_OK) {
            val data = it.data
            val selectedImageUris: ArrayList<Uri> = arrayListOf()
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    selectedImageUris.add(imageUri)
                }
            } else if (data?.data != null) {
                val imageUri = data.data
                if (imageUri != null) {
                    selectedImageUris.add(imageUri)
                }
            }

            logger("--uri--", "gallery path: ${gsonString(selectedImageUris)}")

            if (selectedImageUris.isNotEmpty()) {
                val remainingPhotosCount = IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.photoList.get()?.count { it.path != null } ?: 0)
                if (selectedImageUris.size > remainingPhotosCount) {
                    Toast.makeText(
                        this@EditPhotosActivity,
                        "Please try again with $remainingPhotosCount Photos Selection",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    selectedImageList = ArrayList()
                    croppedImageList = ArrayList()

                    selectedImageUris.forEach { uri ->
                        val path = getRealPathFromUri(uri)

                        if (path != null) {
                            selectedImageList.add(path)
                        }
                    }

                    for (photo in viewModel.photoList.get()!!) {
                        val photoFile = photo.path?.let { it1 -> File(it1).nameWithoutExtension }

                        loop@ for (path in selectedImageList) {

                            val pathFile = File(path).nameWithoutExtension

                            if (photoFile == pathFile) {
                                selectedImageList.remove(path)
                                break@loop
                            }
                        }
                    }

                    if (selectedImageList.isEmpty()) {
                        Toast.makeText(this, "Please select distinct images only", Toast.LENGTH_SHORT).show()
                        return@registerForActivityResult
                    }

                    cropPosition = 0
                    uCrop()
                }
            } else {
                Toast.makeText(this, "Something went wrong, try again...!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uCrop() {
        val options = UCrop.Options()
        options.setStatusBarColor(getColorFromAttr(android.R.attr.windowBackground))
        options.setToolbarColor(getColorFromAttr(android.R.attr.windowBackground))
        options.setToolbarTitle("Crop Image")
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.colorOnBackground))
        options.setRootViewBackgroundColor(getColorFromAttr(android.R.attr.windowBackground))
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorSurfaceVariant))
        options.setDimmedLayerColor(getColorFromAttr(android.R.attr.windowBackground))
        options.setAspectRatioOptions(
            0,
            AspectRatio("3:4", 3F, 4F),
            AspectRatio("2:3", 2F, 3F),
            AspectRatio("3:5", 3F, 5F),
            AspectRatio("9:16", 9F, 16F),
        )
        options.setImageToCropBoundsAnimDuration(500)
        UCrop.of(
            Uri.fromFile(File(selectedImageList[cropPosition])),
            Uri.fromFile(File(cacheDir, File("/upload/images/cropped/" + selectedImageList[cropPosition]).name))
        ).withOptions(options).start(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.setType("image/*")
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(
                    Build.VERSION_CODES.R
                ) >= 2
            ) {
                intent.putExtra(
                    MediaStore.EXTRA_PICK_IMAGES_MAX,
                    IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.photoList.get()?.count { it.path != null } ?: 0))
            }
            intent.setAction(Intent.ACTION_GET_CONTENT)
            imagePickerLauncher.launch(intent)
        } else {
            var openSettings = false

            for (permission in permissions) {
                if (!shouldShowRequestPermissionRationale(permission)) {
                    openSettings = true
                    break
                }
            }

            if (openSettings) {
                permissionDialog.show()

                val btnContinue = permissionDialog.findViewById<TextView>(R.id.btn_open_settings)
                btnContinue.setOnClickListener {
                    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                    with(intent) {
                        data = Uri.fromParts("package", packageName, null)
                        addCategory(CATEGORY_DEFAULT)
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                        addFlags(FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }
                    startActivity(intent)

                    permissionDialog.dismiss()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = data?.let { UCrop.getOutput(it) }

            if (resultUri != null) {
                val path = getRealPathFromUri(resultUri)
                logger("--uri--", "cropped path: $path")
                path?.let { croppedImageList.add(it) }

                cropPosition++

                if (cropPosition < selectedImageList.size) {
                    uCrop()
                } else if (cropPosition == selectedImageList.size) {
                    cropPosition = 0

                    LoadingDialog.show(this)

                    lifecycleScope.launch(Dispatchers.IO) {
                        val compressedImages = arrayListOf<PhotoValidationModel>()

                        croppedImageList.forEach { path ->
                            val compressedImage = processImage(File(path))

                            if (compressedImage != null) {
                                compressedImages.add(PhotoValidationModel(path = compressedImage.absolutePath))
                            } else {
                                compressedImages.add(PhotoValidationModel(path = path, reject = Reject.SOMETHING_WRONG))
                            }
                        }

                        if (compressedImages.isEmpty()) {
                            launch(Dispatchers.Main) {
                                LoadingDialog.hide()
                            }
                        } else {
                            val resolutionValidatedImages = arrayListOf<PhotoValidationModel>()

                            compressedImages.forEach { photoValidationModel ->
                                if (validateResolutionOfImage(photoValidationModel.path)) {
                                    resolutionValidatedImages.add(photoValidationModel)
                                } else {
                                    resolutionValidatedImages.add(
                                        PhotoValidationModel(
                                            path = photoValidationModel.path, reject = Reject.TOO_LOW_RESOLUTION
                                        )
                                    )
                                }
                            }

                            if (resolutionValidatedImages.isEmpty()) {
                                launch(Dispatchers.Main) {
                                    LoadingDialog.hide()
                                }
                            } else {
                                lstTemp = arrayListOf()
                                resolutionValidatedImages.forEach { photoBlur ->
                                    lstTemp.add(photoBlur)
                                }

                                if (lstTemp.isNotEmpty()) {
                                    position = 0
                                    startProcess(position)
                                }
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Something went wrong...!", Toast.LENGTH_SHORT).show()
            }
        } else if ((resultCode == RESULT_CANCELED) && (requestCode == UCrop.REQUEST_CROP)) {
            cropPosition++

            if (cropPosition < selectedImageList.size) {
                uCrop()
            } else if (cropPosition == selectedImageList.size) {
                cropPosition = 0

                LoadingDialog.show(this)

                lifecycleScope.launch(Dispatchers.IO) {
                    val compressedImages = arrayListOf<PhotoValidationModel>()

                    croppedImageList.forEach { path ->
                        val compressedImage = processImage(File(path))

                        if (compressedImage != null) {
                            compressedImages.add(PhotoValidationModel(path = compressedImage.absolutePath))
                        } else {
                            compressedImages.add(PhotoValidationModel(path = path, reject = Reject.SOMETHING_WRONG))
                        }
                    }

                    if (compressedImages.isEmpty()) {
                        launch(Dispatchers.Main) {
                            LoadingDialog.hide()
                        }
                    } else {
                        val resolutionValidatedImages = arrayListOf<PhotoValidationModel>()

                        compressedImages.forEach { photoValidationModel ->
                            if (validateResolutionOfImage(photoValidationModel.path)) {
                                resolutionValidatedImages.add(photoValidationModel)
                            } else {
                                resolutionValidatedImages.add(
                                    PhotoValidationModel(
                                        path = photoValidationModel.path, reject = Reject.TOO_LOW_RESOLUTION
                                    )
                                )
                            }
                        }

                        if (resolutionValidatedImages.isEmpty()) {
                            launch(Dispatchers.Main) {
                                LoadingDialog.hide()
                            }
                        } else {
                            lstTemp = arrayListOf()

                            resolutionValidatedImages.forEach { photoBlur ->
                                lstTemp.add(photoBlur)
                            }

                            if (lstTemp.isNotEmpty()) {
                                position = 0
                                startProcess(position)
                            }
                        }
                    }
                }
            }
        } else {
            //
            /*data?.let {
                Toast.makeText(
                    this,
                    UCrop.getError(it)?.message ?: "Something went wrong...!",
                    Toast.LENGTH_SHORT,
                ).show()
            }*/
        }
    }

    private fun startProcess(index: Int) {
        if (lstTemp[index].reject != null) {
            lifecycleScope.launch(Dispatchers.Main) {
                LoadingDialog.hide()

                photoAdapter.updateItem(lstTemp[index])
                checkUploadError()
                position++

                if (lstTemp.size != position) {
                    LoadingDialog.show(this@EditPhotosActivity)

                    launch(Dispatchers.IO) {
                        startProcess(position)
                    }
                }
            }
        } else {
            val path = lstTemp[index].path
            if (path != null) {
                validateImage(path)

                /*lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.annotateImage(File(path)).collectLatest {
                        validateImageWithGoogleVisionResponse(it)
                    }
                }*/
            } else {
                lifecycleScope.launch(Dispatchers.Main) {
                    LoadingDialog.hide()

                    photoAdapter.updateItem(lstTemp[index])
                    checkUploadError()
                    position++

                    if (lstTemp.size != position) {
                        LoadingDialog.show(this@EditPhotosActivity)

                        launch(Dispatchers.IO) {
                            startProcess(position)
                        }
                    }
                }
            }
        }
    }

    private fun validateImage(path: String) {
        LoadingDialog.show(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = getBitmapFromLocalPath(path)

            val faceCheck = async { viewModel.detectFace(bitmap) }
            val colorCheck = async { viewModel.verifyMultiColor(bitmap) }
            val nsfwCheck = async { viewModel.checkNSFW(path) }

            val isFaceAvailable = faceCheck.await()
            val isSafe = nsfwCheck.await()
            val isMultiColor = colorCheck.await()

            val detectHashmap = hashMapOf<String, Boolean>()
            detectHashmap["FACE_DETECTION"] = isFaceAvailable
            detectHashmap["SAFE_SEARCH_DETECTION"] = isSafe
            detectHashmap["IMAGE_PROPERTIES"] = isMultiColor

            launch(Dispatchers.Main) {
                LoadingDialog.hide()

                val isValidImage = if (!detectHashmap.containsKey("SAFE_SEARCH_DETECTION") || !detectHashmap.containsKey(
                        "FACE_DETECTION"
                    ) || !detectHashmap.containsKey("IMAGE_PROPERTIES")
                ) {
                    false
                } else {
                    detectHashmap["SAFE_SEARCH_DETECTION"]!! && detectHashmap["FACE_DETECTION"]!! && detectHashmap["IMAGE_PROPERTIES"]!!
                }

                setViolationMessage(isValidImage, detectHashmap)

                photoAdapter.updateItem(lstTemp[position])
                checkUploadError()
                position++

                if (lstTemp.size != position) {
                    LoadingDialog.show(this@EditPhotosActivity)

                    launch(Dispatchers.IO) {
                        startProcess(position)
                    }
                }
            }
        }
    }

    private fun validateImageWithGoogleVisionResponse(it: Resource<JsonObject?>) {
        when (it.status) {
            Status.LOADING -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    LoadingDialog.show(this@EditPhotosActivity)
                }
            }

            Status.ERROR -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        LoadingDialog.hide()

                        lstTemp[position].reject = Reject.SOMETHING_WRONG
                        photoAdapter.updateItem(lstTemp[position])
                        checkUploadError()
                        position++

                        if (lstTemp.size != position) {
                            LoadingDialog.show(this@EditPhotosActivity)

                            launch(Dispatchers.IO) {
                                startProcess(position)
                            }
                        }
                    } catch (e: Exception) {
                        catchLog("validateImageWithGoogleVisionResponse: " + gsonString(e))
                    }
                }
            }

            Status.SUCCESS -> {
                val rootJson = it.data

                if ((rootJson == null) || !rootJson.has("responses")) {
                    validateImageWithGoogleVisionResponse(Resource.error("Something went wrong, try again...!", null))
                } else {
                    val responseJson = rootJson.getAsJsonArray("responses")

                    if ((responseJson != null) && !responseJson.isEmpty) {
                        val detectHashmap = HashMap<String, Boolean>()

                        val responseObject = responseJson[0].asJsonObject

                        if (responseObject.has("faceAnnotations")) {
                            val faceAnnotations = responseObject.getAsJsonArray("faceAnnotations")

                            detectHashmap["FACE_DETECTION"] = if (faceAnnotations.isEmpty) {
                                false
                            } else {
                                faceAnnotations.any {
                                    try {
                                        logger("--vision--", "detectionConfidence: ${it.asJsonObject.get("detectionConfidence").asDouble}")

                                        val blurredLikelihood = it.asJsonObject.get("blurredLikelihood").asString
                                        logger("--vision--", "blurredLikelihood: $blurredLikelihood")

                                        val isBlurred = (blurredLikelihood == "LIKELY") || (blurredLikelihood == "VERY_LIKELY")

                                        (it.asJsonObject.get("detectionConfidence").asDouble >= 0.50) && (!isBlurred)
                                    } catch (e: Exception) {
                                        catchLog("validateImageWithGoogleVisionResponse: " + gsonString(e))
                                        return
                                    }
                                }
                            }
                        }

                        if (responseObject.has("safeSearchAnnotation")) {
                            try {
                                val safeSearchAnnotation = responseObject.getAsJsonObject("safeSearchAnnotation")

                                val safeSearchHashmap = hashMapOf<String, Boolean>()

                                if (safeSearchAnnotation.has("adult")) {
                                    val adult = safeSearchAnnotation.get("adult").asString
                                    safeSearchHashmap["adult"] = adult == "VERY_LIKELY" || adult == "LIKELY"
                                }
                                if (safeSearchAnnotation.has("spoof")) {
                                    val spoof = safeSearchAnnotation.get("spoof").asString
                                    safeSearchHashmap["spoof"] = spoof == "VERY_LIKELY" || spoof == "LIKELY"
                                }
                                if (safeSearchAnnotation.has("medical")) {
                                    val medical = safeSearchAnnotation.get("medical").asString
                                    safeSearchHashmap["medical"] = medical == "VERY_LIKELY" || medical == "LIKELY"
                                }
                                if (safeSearchAnnotation.has("violence")) {
                                    val violence = safeSearchAnnotation.get("violence").asString
                                    safeSearchHashmap["violence"] = violence == "VERY_LIKELY" || violence == "LIKELY"
                                }
                                if (safeSearchAnnotation.has("racy")) {
                                    val racy = safeSearchAnnotation.get("racy").asString
                                    safeSearchHashmap["racy"] = racy == "VERY_LIKELY" || racy == "LIKELY"
                                }

                                logger("--vision--", "safeSearchAnnotation: ${gsonString(safeSearchAnnotation)}")
                                logger("--vision--", "safeSearchHashmap: ${gsonString(safeSearchHashmap)}")

                                detectHashmap["SAFE_SEARCH_DETECTION"] = !safeSearchHashmap.containsValue(true)
                            } catch (e: Exception) {
                                catchLog("validateImageWithGoogleVisionResponse: " + gsonString(e))
                            }
                        }

                        if (responseObject.has("imagePropertiesAnnotation")) {
                            try {
                                val imagePropertiesAnnotation = responseObject.getAsJsonObject("imagePropertiesAnnotation")
                                val dominantColors = imagePropertiesAnnotation.getAsJsonObject("dominantColors")
                                val colors = dominantColors.getAsJsonArray("colors")

                                // Initialize variables for calculating pixelFraction and RGB sum
                                var score = 0.0
                                var pixelFraction = 0.0
                                var rgbSum = 0

                                // Loop through each color object in the "colors" array
                                colors.forEach { colorJsonElement ->
                                    try {
                                        val colorObject = colorJsonElement.asJsonObject

                                        // Extract pixelFraction and RGB values
                                        val color = colorObject.getAsJsonObject("color")
                                        val fraction = colorObject.get("pixelFraction").asDouble
                                        val dominantColorProbability = colorObject.get("score").asDouble
                                        val red = color.get("red").asInt
                                        val green = color.get("green").asInt
                                        val blue = color.get("blue").asInt

                                        // Update pixelFraction and RGB sum for the color with the highest pixelFraction
                                        if (dominantColorProbability > score) {
                                            score = dominantColorProbability
                                            pixelFraction = fraction
                                            rgbSum = red + green + blue
                                        }
                                    } catch (e: Exception) {
                                        catchLog("validateImageWithGoogleVisionResponse: " + gsonString(e))
                                    }
                                }

                                logger("--vision--", "pixelFraction: $pixelFraction")
                                logger("--vision--", "rgbSum: $rgbSum")

                                val blurryHashmap = hashMapOf<String, Boolean>()

                                blurryHashmap["pixelFraction"] = (pixelFraction > 0.75)
                                blurryHashmap["rgbSum"] = (rgbSum < 75)

                                detectHashmap["IMAGE_PROPERTIES"] = !blurryHashmap.containsValue(true)
                            } catch (e: Exception) {
                                catchLog("validateImageWithGoogleVisionResponse: " + gsonString(e))
                            }
                        }

                        logger("detectHashmap", gsonString(detectHashmap))

                        lifecycleScope.launch(Dispatchers.Main) {
                            LoadingDialog.hide()

                            val isValidImage = if (!detectHashmap.containsKey("SAFE_SEARCH_DETECTION") || !detectHashmap.containsKey(
                                    "FACE_DETECTION"
                                ) || !detectHashmap.containsKey("IMAGE_PROPERTIES")
                            ) {
                                false
                            } else {
                                detectHashmap["SAFE_SEARCH_DETECTION"]!! && detectHashmap["FACE_DETECTION"]!! && detectHashmap["IMAGE_PROPERTIES"]!!
                            }

                            setViolationMessage(isValidImage, detectHashmap)

                            photoAdapter.updateItem(lstTemp[position])
                            checkUploadError()
                            position++

                            if (lstTemp.size != position) {
                                LoadingDialog.show(this@EditPhotosActivity)

                                launch(Dispatchers.IO) {
                                    startProcess(position)
                                }
                            }
                        }
                    } else {
                        validateImageWithGoogleVisionResponse(
                            Resource.error(
                                "Something went wrong, try again...!",
                                null,
                            )
                        )
                    }
                }
            }

            Status.SIGN_OUT -> {

            }

            Status.ADMIN_BLOCKED -> {

            }
        }
    }

    private fun checkUploadError() {
        val count = viewModel.photoList.get()?.count { it.path != null }
        viewModel.photoCount.set(count)

        val isValid = viewModel.photoList.get()?.any { it.reject != null }

        if ((count == null) || (count < 2)) {
            viewModel.validationError.set("Minimum 2 photos required.")
            viewModel.isImagesUpdated.set(false)
        } else if (isValid == true) {
            viewModel.validationError.set("Please remove invalid images.")
            viewModel.isImagesUpdated.set(false)
        } else {
            // no image errors and have at least 2 images
            viewModel.isImagesUpdated.set(false)
            viewModel.validationError.set("")

            if (viewModel.photoList.get()!!.count { it.path != null } < viewModel.previousList.get()!!.size) {
                // user has deleted images
                viewModel.isImagesUpdated.set(true)
            } else {
                // user has updated sequence or added some images
                viewModel.photoList.get()?.forEachIndexed { index, photo ->
                    if ((photo.reject == null) && (photo.path != null)) {
                        if (index < viewModel.previousList.get()!!.size) {
                            if (viewModel.previousList.get()?.get(index) != photo.path) {
                                // sequence changed
                                viewModel.isImagesUpdated.set(true)
                                return@forEachIndexed
                            }
                        } else {
                            // image added
                            viewModel.isImagesUpdated.set(true)
                            return@forEachIndexed
                        }
                    }
                }
            }
        }
    }

    private fun setViolationMessage(
        isValidImage: Boolean,
        detectHashmap: HashMap<String, Boolean>,
    ) {
        try {
            if (!isValidImage) {
                val faceDetection = if (detectHashmap.containsKey("FACE_DETECTION")) {
                    detectHashmap["FACE_DETECTION"] as Boolean
                } else {
                    false
                }

                if (!faceDetection) {
                    lstTemp[position].reject = Reject.FACE_NOT_FOUND
                } else {
                    val safeSearchDetection = if (detectHashmap.containsKey("SAFE_SEARCH_DETECTION")) {
                        detectHashmap["SAFE_SEARCH_DETECTION"] as Boolean
                    } else {
                        false
                    }

                    if (!safeSearchDetection) {
                        lstTemp[position].reject = Reject.NOT_SAFE
                    } else {
                        val imageAnnotation = if (detectHashmap.containsKey("IMAGE_PROPERTIES")) {
                            detectHashmap["IMAGE_PROPERTIES"] as Boolean
                        } else {
                            false
                        }

                        if (!imageAnnotation) {
                            lstTemp[position].reject = Reject.BLURRED
                        }
                    }
                }
            }
        } catch (e: Exception) {
            catchLog("setViolationMessage: " + gsonString(e))

            lstTemp[position].reject = Reject.SOMETHING_WRONG
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // Handles back action
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun photoCount() {
        val photoCount = if (viewModel.photoList.get()?.count { it.path != null } == null) {
            0
        } else {
            viewModel.photoList.get()!!.count { it.path != null }
        }
        viewModel.photoCount.set(photoCount)
    }

    private fun getImagesFromGallery() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
            // user cannot pick images from anywhere with ACTION_PICK_IMAGES
            Intent(MediaStore.ACTION_PICK_IMAGES, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                val remainingImage = IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.photoList.get()?.count { it.path != null } ?: 0)
                if (remainingImage != 1) {
                    putExtra(
                        MediaStore.EXTRA_PICK_IMAGES_MAX,
                        IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.photoList.get()?.count { it.path != null } ?: 0))
                }
                type = "image/*"
            }
        } else {
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
        imagePickerLauncher.launch(intent)
    }

    private fun authOut() {
        LoadingDialog.show(this)

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@EditPhotosActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()

        initiateUserPhotos()
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)

        if (isConnected && viewModel.previousList.get()?.size == 0) {
            initiateUserPhotos()
        }
    }

    override fun onPlaceHolderClick(pos: Int) {
        if (!hasPermission(*imagePermission)) {
            askPermissions(imagePermission, PERMISSION_CODE)
        } else {
            if (viewModel.photoList.get()!!.size - 1 < IntroductionConstants.UPLOAD_PHOTO_LIMIT) {
                if ((viewModel.previousList.get()?.size != null) && (viewModel.previousList.get()?.size != 0)) {
                    getImagesFromGallery()
                } else {
                    Toast.makeText(this, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this@EditPhotosActivity,
                    "Upload a maximum of ${IntroductionConstants.UPLOAD_PHOTO_LIMIT} photos only...",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    override fun onDeleteClick(pos: Int) {
        checkUploadError()
    }
}
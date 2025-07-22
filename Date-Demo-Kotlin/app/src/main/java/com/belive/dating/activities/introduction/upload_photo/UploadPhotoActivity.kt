package com.belive.dating.activities.introduction.upload_photo

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
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.Observable
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.BuildConfig
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.camera.CameraActivity
import com.belive.dating.activities.camera.SelfieGuidelineAdapter
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.activities.introduction.upload_photo.policy.PhotoUploadPolicyActivity
import com.belive.dating.activities.permission.PermissionsManagerActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.InterstitialGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.ads.admob.AdmobAds
import com.belive.dating.constants.IntroductionConstants
import com.belive.dating.databinding.ActivityUploadPhotoBinding
import com.belive.dating.di.gistModule
import com.belive.dating.di.googleVisionModule
import com.belive.dating.di.introductionModule
import com.belive.dating.di.introductionViewModels
import com.belive.dating.di.mainViewModel
import com.belive.dating.di.signInViewModel
import com.belive.dating.di.splashDataModule
import com.belive.dating.di.userModule
import com.belive.dating.dialogs.AppDialog
import com.belive.dating.dialogs.InitFailedDialog
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.askPermissions
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.checkPermissions
import com.belive.dating.extensions.getBitmapFromLocalPath
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getDeviceID
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getIntroductionPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getMediaType
import com.belive.dating.extensions.getRealPathFromUri
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gone
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.hasPermission
import com.belive.dating.extensions.isAppUpdateRequired
import com.belive.dating.extensions.loadSelfie
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.noAnimation
import com.belive.dating.extensions.prepareImagePart
import com.belive.dating.extensions.prepareSelfiePart
import com.belive.dating.extensions.processImage
import com.belive.dating.extensions.reOpenApp
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.extensions.validateResolutionOfImage
import com.belive.dating.extensions.visible
import com.belive.dating.helpers.helper_functions.get_gist.getGistData
import com.belive.dating.helpers.helper_functions.grid_layout_manager.WrapperGridLayoutManager
import com.belive.dating.helpers.helper_functions.splash_data.SplashData
import com.belive.dating.preferences.pref_utils.IntroductionPrefUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import java.io.File
import java.util.Collections

class UploadPhotoActivity : NetworkReceiverActivity(), UploadPhotoAdapter.OnUploadImageListener {

    private val TAG = "--upload_photo--"

    val binding: ActivityUploadPhotoBinding by lazy {
        ActivityUploadPhotoBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: UploadPhotoViewModel

    private val photoAdapter by lazy {
        UploadPhotoAdapter(this@UploadPhotoActivity, viewModel)
    }

    private val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
    )
    else arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
    )
    private val cameraPermission = arrayOf(android.Manifest.permission.CAMERA)

    private val PHOTO_PERMISSION_CODE = 103
    private val CAMERA_PERMISSION_CODE = 104

    private var imagePermissionDialog: Dialog? = null

    private fun showImagePermissionDialog() {
        if (imagePermissionDialog == null) {
            imagePermissionDialog = Dialog(this).apply {
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
                    StringBuilder().append("To select photos from your device, we need access to your media storage. Please click below and allow it from settings.")
            }
        }
        imagePermissionDialog?.show()
    }

    private var cameraPermissionDialog: Dialog? = null

    private fun showCameraPermissionDialog() {
        if (cameraPermissionDialog == null) {
            cameraPermissionDialog = Dialog(this).apply {
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
                    StringBuilder().append("Camera permission required to verify yourself, please click below and allow it from settings.")
            }
        }
        cameraPermissionDialog?.show()
    }

    private var takeSelfieDialog: Dialog? = null

    private fun showTakeSelfieDialog() {
        takeSelfieDialog?.cancel()
        takeSelfieDialog = null

        takeSelfieDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.dialog_take_selfie)
            setCanceledOnTouchOutside(true)
            window?.setDimAmount(0.75f)
            window?.apply {
                setLayout(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(android.R.color.transparent)
            }

            val rvSelfiePolicies = findViewById<RecyclerView>(R.id.rv_selfie_policies)
            val rvRecheckPolicies = findViewById<RecyclerView>(R.id.rv_recheck_policies)
            val recheckSelfieTitle = findViewById<TextView>(R.id.txt_recheck_selfie_title)
            val layoutSelfiePlaceHolder = findViewById<ConstraintLayout>(R.id.layout_placeholder)
            val imgSelfie = findViewById<ImageView>(R.id.img_selfie)
            val btnNext = findViewById<Button>(R.id.btn_next)
            val btnRetakeSelfie = findViewById<Button>(R.id.btn_retake_selfie)
            val btnClose = findViewById<ImageButton>(R.id.btn_close)

            val selfiePolicies = arrayListOf<String>()
            selfiePolicies.add("<b>Center your face</b> in the frame and <b>hold the device steady</b>.")
            selfiePolicies.add("<b>Remove glasses, hats, and face coverings.</b>")
            selfiePolicies.add("<b>Use a well-lit area</b> with a plain, shadow-free background.")
            selfiePolicies.add("<b>Keep both eyes open</b> and look directly at the camera.")

            val selfiePolicyAdapter = SelfieGuidelineAdapter()
            selfiePolicyAdapter.guidelineList = selfiePolicies

            rvSelfiePolicies.layoutManager = LinearLayoutManager(this@UploadPhotoActivity, LinearLayoutManager.VERTICAL, false)
            rvSelfiePolicies.adapter = selfiePolicyAdapter

            val recheckPolicies = arrayListOf<String>()
            recheckPolicies.add("Can you see your <b>whole face</b>?")
            recheckPolicies.add("Is your <b>selfie</b> matching with <b>uploaded photos</b>?")

            val recheckAdapter = SelfieGuidelineAdapter()
            recheckAdapter.guidelineList = recheckPolicies

            rvRecheckPolicies.layoutManager = LinearLayoutManager(this@UploadPhotoActivity, LinearLayoutManager.VERTICAL, false)
            rvRecheckPolicies.adapter = recheckAdapter

            if (viewModel.photoState.get() == PhotoState.SELFIE_PENDING) {
                btnNext.text = StringBuilder().append("Take a Selfie")
                btnRetakeSelfie.gone()
                rvSelfiePolicies.visible()
                recheckSelfieTitle.gone()
                rvRecheckPolicies.gone()

                imgSelfie.loadSelfie(null)
            } else if (viewModel.photoState.get() == PhotoState.SELFIE_VERIFIED) {
                btnNext.text = StringBuilder().append("Start Matching Now")
                btnRetakeSelfie.visible()
                rvSelfiePolicies.gone()
                recheckSelfieTitle.visible()
                rvRecheckPolicies.visible()

                imgSelfie.loadSelfie(viewModel.selfiePath.get())
            }

            viewModel.photoState.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    val state = viewModel.photoState.get()

                    when (state) {
                        PhotoState.SELFIE_PENDING -> {
                            btnNext.text = StringBuilder().append("Take a Selfie")
                            btnRetakeSelfie.gone()
                            rvSelfiePolicies.visible()
                            recheckSelfieTitle.gone()
                            rvRecheckPolicies.gone()

                            imgSelfie.loadSelfie(null)
                        }

                        PhotoState.SELFIE_VERIFIED -> {
                            btnNext.text = StringBuilder().append("Get Started Dating")
                            btnRetakeSelfie.visible()
                            rvSelfiePolicies.gone()
                            recheckSelfieTitle.visible()
                            rvRecheckPolicies.visible()

                            imgSelfie.loadSelfie(viewModel.selfiePath.get())
                        }

                        else -> {

                        }
                    }
                }
            })

            setOnDismissListener {
                viewModel.selfiePath.set(null)
                viewModel.selfieFile.set(null)
            }

            btnClose.setOnClickListener {
                cancel()
            }

            layoutSelfiePlaceHolder.setOnClickListener {
                // take a selfie
                if (!hasPermission(*cameraPermission)) {
                    askPermissions(cameraPermission, CAMERA_PERMISSION_CODE)
                } else {
                    captureSelfie()
                }
            }

            btnNext.setOnClickListener {
                if (viewModel.selfiePath.get() == null) {
                    // take a selfie
                    if (!hasPermission(*cameraPermission)) {
                        askPermissions(cameraPermission, CAMERA_PERMISSION_CODE)
                    } else {
                        captureSelfie()
                    }
                } else {
                    viewModel.photoState.set(PhotoState.UPLOADING_DATA)

                    cancel()

                    signUp()
                }
            }

            btnRetakeSelfie.setOnClickListener {
                // take a selfie
                if (!hasPermission(*cameraPermission)) {
                    askPermissions(cameraPermission, CAMERA_PERMISSION_CODE)
                } else {
                    captureSelfie()
                }
            }
        }

        takeSelfieDialog?.show()
    }

    private var selectedImageList = ArrayList<String>()
    private var croppedImageList = ArrayList<String>()

    private var lstTemp = ArrayList<PhotoValidationModel>()

    private var cropPosition = 0
    private var position = 0

    override fun onResume() {
        super.onResume()

        catchLog("--introduction-- onResume: $TAG")

        mixPanel?.timeEvent(UploadPhotoActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        catchLog("--introduction-- onPause: $TAG")

        if (isFinishing) {
            mixPanel?.track(UploadPhotoActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", true)
            })
        } else {
            mixPanel?.track(UploadPhotoActivity::class.java.simpleName, JSONObject().apply {
                put("isBackPressed", false)
            })
        }
    }

    override fun onDestroy() {
        catchLog("--introduction-- onDestroy: $TAG")

        if (imagePermissionDialog?.isShowing == true) {
            imagePermissionDialog?.cancel()
            imagePermissionDialog = null
        }
        if (cameraPermissionDialog?.isShowing == true) {
            cameraPermissionDialog?.cancel()
            cameraPermissionDialog = null
        }
        if (takeSelfieDialog?.isShowing == true) {
            takeSelfieDialog?.cancel()
            takeSelfieDialog = null
        }

        super.onDestroy()
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

        viewModel = tryKoinViewModel(listOf(introductionModule, introductionViewModels, googleVisionModule))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getIntroductionPrefs().photoList = null
                getIntroductionPrefs().selfie = null
                finish()
                swipeLeft()
            }
        })

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnPolicy.setOnClickListener {
            startActivity(Intent(this, PhotoUploadPolicyActivity::class.java))
            swipeRight()
        }

        binding.btnTakeSelfie.setOnClickListener {
            getIntroductionPrefs().photoList = viewModel.uploadList.get()?.filter { (it.path != null) && (it.reject == null) }?.map { it.path!! }

            logger("--photo_list--", getIntroductionPrefs().photoList)

            mixPanel?.track(TAG, JSONObject().apply {
                put("Photo List", getIntroductionPrefs().photoList?.joinToString(", ") { it })
            })

            getIntroductionPrefs().selfie = null
            viewModel.photoState.set(PhotoState.SELFIE_PENDING)
            viewModel.selfiePath.set(null)
            viewModel.selfieFile.set(null)

            showTakeSelfieDialog()
        }
    }

    private fun initViews() {
        setUpRecyclerView()
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
                logger("--move--", "onMove: ${viewHolder.bindingAdapterPosition} to ${target.bindingAdapterPosition}")

                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                // Prevent dragging for photos holder only
                if (fromPosition >= viewModel.photoCount.get()!! || toPosition >= viewModel.photoCount.get()!!) {
                    return false
                }

                // Swap the items in your data list
                val list = viewModel.uploadList.get()!!
                Collections.swap(list, fromPosition, toPosition)
                viewModel.uploadList.set(list)

                // Notify adapter about the moved item
                photoAdapter.notifyItemMoved(fromPosition, toPosition)
                if (fromPosition < toPosition) {
                    photoAdapter.notifyItemRangeChanged(fromPosition, toPosition + 1)
                } else {
                    photoAdapter.notifyItemRangeChanged(toPosition, fromPosition + 1)
                }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // No swipe action needed

                logger("--move--", "onSwiped: ${viewHolder.bindingAdapterPosition}")
            }
        }

        // Attach the ItemTouchHelper to RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvPhotos)
    }

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

            logger("--uri--", gsonString(selectedImageUris))

            if (selectedImageUris.isNotEmpty()) {
                val remainingPhotosCount = IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.uploadList.get()?.count { it.path != null } ?: 0)
                if (selectedImageUris.size > remainingPhotosCount) {
                    Toast.makeText(
                        this@UploadPhotoActivity,
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

                    for (photo in viewModel.uploadList.get()!!) {
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
        options.setActiveControlsWidgetColor(
            ContextCompat.getColor(
                this, R.color.colorSurfaceVariant
            )
        )
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
            Uri.fromFile(File(selectedImageList[cropPosition])), Uri.fromFile(
                File(
                    cacheDir, File("/upload/images/cropped/" + selectedImageList[cropPosition]).name
                )
            )
        ).withOptions(options).start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            mixPanel?.timeEvent(TAG)

            val resultUri = data?.let { UCrop.getOutput(it) }

            if (resultUri != null) {
                val path = getRealPathFromUri(resultUri)
                path?.let { croppedImageList.add(it) }

                cropPosition++

                if (cropPosition < selectedImageList.size) {
                    uCrop()
                } else if (cropPosition == selectedImageList.size) {
                    cropPosition = 0

                    LoadingDialog.show(this)

                    lifecycleScope.launch(Dispatchers.IO) {
                        val compressedImages = arrayListOf<PhotoValidationModel>()

                        croppedImageList.forEachIndexed { index, path ->
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

                            compressedImages.forEachIndexed { index, photoValidationModel ->
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
            mixPanel?.timeEvent(TAG)

            cropPosition++

            if (cropPosition < selectedImageList.size) {
                uCrop()
            } else if (cropPosition == selectedImageList.size) {
                cropPosition = 0

                LoadingDialog.show(this)

                lifecycleScope.launch(Dispatchers.IO) {
                    val compressedImages = arrayListOf<PhotoValidationModel>()

                    croppedImageList.forEachIndexed { index, path ->
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

                        compressedImages.forEachIndexed { index, photoValidationModel ->
                            if (validateResolutionOfImage(photoValidationModel.path)) {
                                resolutionValidatedImages.add(photoValidationModel)
                            } else {
                                resolutionValidatedImages.add(PhotoValidationModel(photoValidationModel.path, reject = Reject.TOO_LOW_RESOLUTION))
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
                    LoadingDialog.show(this@UploadPhotoActivity)

                    launch(Dispatchers.IO) {
                        startProcess(position)
                    }
                }
            }
        } else {
            val path = lstTemp[index].path
            if (path != null) {
                validateImage(path)
            } else {
                lifecycleScope.launch(Dispatchers.Main) {
                    LoadingDialog.hide()

                    photoAdapter.updateItem(lstTemp[index])
                    checkUploadError()
                    position++

                    if (lstTemp.size != position) {
                        LoadingDialog.show(this@UploadPhotoActivity)

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

                try {
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
                        LoadingDialog.show(this@UploadPhotoActivity)

                        launch(Dispatchers.IO) {
                            startProcess(position)
                        }
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)

                    catchLog("validateImageWithGoogleVisionResponse: " + gsonString(e))

                    Toast.makeText(this@UploadPhotoActivity, "Something went wrong, try again...!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateImageWithGoogleVisionResponse(it: Resource<JsonObject?>) {
        when (it.status) {
            Status.LOADING -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    LoadingDialog.show(this@UploadPhotoActivity)
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
                            LoadingDialog.show(this@UploadPhotoActivity)

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

                            try {
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
                                    LoadingDialog.show(this@UploadPhotoActivity)

                                    launch(Dispatchers.IO) {
                                        startProcess(position)
                                    }
                                }
                            } catch (e: Exception) {
                                FirebaseCrashlytics.getInstance().recordException(e)

                                catchLog("validateImageWithGoogleVisionResponse: " + gsonString(e))

                                Toast.makeText(this@UploadPhotoActivity, "Something went wrong, try again...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        validateImageWithGoogleVisionResponse(Resource.error("Something went wrong, try again...!", null))
                    }
                }
            }

            Status.SIGN_OUT -> {

            }

            Status.ADMIN_BLOCKED -> {

            }
        }
    }

    private fun setViolationMessage(isValidImage: Boolean, detectHashmap: HashMap<String, Boolean>) {
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

            FirebaseCrashlytics.getInstance().recordException(e)

            lstTemp[position].reject = Reject.SOMETHING_WRONG
        }
    }

    private fun captureSelfie() {
        viewModel.selfieFile.set(File(cacheDir, "${System.currentTimeMillis()}-selfie.jpg"))

        val cameraIntent = Intent(this, CameraActivity::class.java).apply {
            putExtra("path", viewModel.selfieFile.get()?.absolutePath)
        }
        cameraLauncher.launch(cameraIntent)
        noAnimation()
    }

    // Register activity result for capturing image
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.photoState.set(PhotoState.SELFIE_PENDING)
            val filePath = viewModel.selfieFile.get()?.absolutePath
            viewModel.selfiePath.set(filePath)
            viewModel.photoState.set(PhotoState.SELFIE_VERIFIED)
            getIntroductionPrefs().selfie = viewModel.selfiePath.get()
        }
    }

    private fun signUp() {
        logger("--sign_up_data--", "LOGIN_TYPE: ${getIntroductionPrefs().loginType}")
        logger("--sign_up_data--", "EMAIL: ${getIntroductionPrefs().email}")
        logger("--sign_up_data--", "NAME: ${getIntroductionPrefs().name}")
        logger("--sign_up_data--", "GENDER: ${getIntroductionPrefs().gender}")
        logger("--sign_up_data--", "BIRTH_DATE: ${getIntroductionPrefs().birthDate}")
        logger("--sign_up_data--", "OPPOSITE_GENDER: ${getIntroductionPrefs().oppositeGender}")
        logger("--sign_up_data--", "SEXUAL_ORIENTATION: ${getIntroductionPrefs().sexualOrientation}")
        logger("--sign_up_data--", "PHOTO_LIST: ${getIntroductionPrefs().photoList}")
        logger("--sign_up_data--", "INTEREST_LIST: ${getIntroductionPrefs().interestList}")
        logger("--sign_up_data--", "RELATIONSHIP_GOAL: ${getIntroductionPrefs().relationshipGoal}")
        logger("--sign_up_data--", "SELFIE: ${getIntroductionPrefs().selfie}")

        if (getIntroductionPrefs().loginType == -1 || getIntroductionPrefs().email == null || getIntroductionPrefs().name == null || getIntroductionPrefs().gender == -1 || getIntroductionPrefs().birthDate == null || getIntroductionPrefs().oppositeGender == -1 || getIntroductionPrefs().sexualOrientation == -1 || getIntroductionPrefs().photoList == null || getIntroductionPrefs().interestList == null || getIntroductionPrefs().relationshipGoal == -1 || getIntroductionPrefs().selfie == null) {
            Toast.makeText(this@UploadPhotoActivity, "Something went wrong, try again please...!", Toast.LENGTH_SHORT).show()

            loadKoinModules(signInViewModel)
            loadKoinModules(introductionModule)

            getKoinObject().get<IntroductionPrefUtils>().clear()

            startActivity(Intent(this, SignInActivity::class.java))
            finishAffinity()
            swipeRight()
            return
        }

        val images: MutableList<MultipartBody.Part> = ArrayList()

        getIntroductionPrefs().photoList?.forEachIndexed { index, path ->
            images.add(prepareImagePart("img_${index + 1}", path))
        }

        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.signUp(
                        fullName = getIntroductionPrefs().name?.toRequestBody(getMediaType()),
                        email = getIntroductionPrefs().email?.toRequestBody(getMediaType()),
                        gender = getIntroductionPrefs().gender.toString().toRequestBody(getMediaType()),
                        birthDate = getIntroductionPrefs().birthDate?.toRequestBody(getMediaType()),
                        seeingInterest = getIntroductionPrefs().oppositeGender.toString().toRequestBody(getMediaType()),
                        orientationId = getIntroductionPrefs().sexualOrientation.toString().toRequestBody(getMediaType()),
                        myInterests = getIntroductionPrefs().interestList?.joinToString(",") { it }?.toRequestBody(getMediaType()),
                        relationshipGoal = getIntroductionPrefs().relationshipGoal.toString().toRequestBody(getMediaType()),
                        loginType = getIntroductionPrefs().loginType.toString().toRequestBody(getMediaType()),
                        deviceToken = getDeviceID().toString().toRequestBody(getMediaType()),
                        fcmToken = task.result.toRequestBody(getMediaType()),
                        versionName = BuildConfig.VERSION_NAME.toRequestBody(getMediaType()),
                        images = images,
                        selfie = mutableListOf(prepareSelfiePart(getIntroductionPrefs().selfie!!)),
                    ).collectLatest {
                        launch(Dispatchers.Main) {
                            when (it.status) {
                                Status.LOADING -> {
                                    LoadingDialog.show(this@UploadPhotoActivity)
                                }

                                Status.ADMIN_BLOCKED -> {
                                    LoadingDialog.hide()
                                }

                                Status.SIGN_OUT -> {
                                    LoadingDialog.hide()
                                }

                                Status.ERROR -> {
                                    viewModel.photoState.set(PhotoState.PHOTOS_VERIFIED)

                                    LoadingDialog.hide()

                                    mixPanel?.track(TAG, JSONObject().apply {
                                        put("Error", it.message)
                                        put("Name", getIntroductionPrefs().name)
                                        put("Email", getIntroductionPrefs().email)
                                        put("Gender", getIntroductionPrefs().gender)
                                        put("Birth Date", getIntroductionPrefs().birthDate)
                                        put("Opposite Gender", getIntroductionPrefs().oppositeGender)
                                        put("Sexuality", getIntroductionPrefs().sexualOrientation)
                                        put("Interests", getIntroductionPrefs().interestList?.joinToString(", ") { it })
                                        put("Relationship Goal", getIntroductionPrefs().relationshipGoal)
                                        put("Login Type", getIntroductionPrefs().loginType)
                                        put("Device ID", getDeviceID())
                                        put("FCM Token", task.result)
                                        put("Version Name", BuildConfig.VERSION_NAME)
                                        put("images", getIntroductionPrefs().photoList?.joinToString(", ") { it })
                                        put("Selfie", getIntroductionPrefs().selfie)
                                        put("Type", "android")
                                    })

                                    if (it.message != null) {
                                        Toast.makeText(this@UploadPhotoActivity, it.message, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@UploadPhotoActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                Status.SUCCESS -> {
                                    LoadingDialog.hide()

                                    mixPanel?.track(TAG, JSONObject().apply {
                                        put("Data", it.data)
                                        put("Status", it.status)
                                        put("Name", getIntroductionPrefs().name)
                                        put("Email", getIntroductionPrefs().email)
                                        put("Gender", getIntroductionPrefs().gender)
                                        put("Birth Date", getIntroductionPrefs().birthDate)
                                        put("Opposite Gender", getIntroductionPrefs().oppositeGender)
                                        put("Sexuality", getIntroductionPrefs().sexualOrientation)
                                        put("Interests", getIntroductionPrefs().interestList?.joinToString(", ") { it })
                                        put("Relationship Goal", getIntroductionPrefs().relationshipGoal)
                                        put("Login Type", getIntroductionPrefs().loginType)
                                        put("Device ID", getDeviceID())
                                        put("FCM Token", task.result)
                                        put("Version Name", BuildConfig.VERSION_NAME)
                                        put("images", getIntroductionPrefs().photoList?.joinToString(", ") { it })
                                        put("Selfie", getIntroductionPrefs().selfie)
                                        put("Type", "android")
                                    })

                                    val file = File(cacheDir, "/upload/images/")
                                    if (file.exists()) {
                                        file.delete()
                                    }

                                    if (it.data != null) {
                                        val rootJson = it.data
                                        if (rootJson.has("data")) {
                                            val token = rootJson.getAsJsonPrimitive("data").asString

                                            if (token != null) {
                                                // sign-up success
                                                getUserPrefs().userToken = token

                                                getKoinObject().get<IntroductionPrefUtils>().clear()

                                                ManageAds.showInterstitialAd(InterstitialGroup.Intro) {
                                                    unloadKoinModules(introductionModule)
                                                    unloadKoinModules(googleVisionModule)
                                                    unloadKoinModules(introductionViewModels)
                                                    unloadKoinModules(signInViewModel)

                                                    if (checkPermissions()) {
                                                        loadKoinModules(userModule)
                                                        loadKoinModules(mainViewModel)

                                                        getUserPrefs().fcmToken = task.result

                                                        startActivity(Intent(this@UploadPhotoActivity, MainActivity::class.java).apply {
                                                            putExtra("display_splash", false)
                                                        })
                                                    } else {
                                                        startActivity(Intent(this@UploadPhotoActivity, PermissionsManagerActivity::class.java))
                                                    }
                                                    finishAffinity()
                                                    swipeRight()
                                                }
                                            } else {
                                                Toast.makeText(this@UploadPhotoActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(this@UploadPhotoActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(this@UploadPhotoActivity, "Something went wrong...!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            catchLog("signUp: ${gsonString(e)}")
            Toast.makeText(this@UploadPhotoActivity, "Something went wrong..!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                captureSelfie()
            } else {
                var openSettings = false

                for (permission in permissions) {
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        openSettings = true
                        break
                    }
                }

                if (openSettings) {
                    showCameraPermissionDialog()

                    val btnContinue = cameraPermissionDialog?.findViewById<TextView>(R.id.btn_open_settings)
                    btnContinue?.setOnClickListener {
                        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                        with(intent) {
                            data = Uri.fromParts("package", packageName, null)
                            addCategory(CATEGORY_DEFAULT)
                            addFlags(FLAG_ACTIVITY_NEW_TASK)
                            addFlags(FLAG_ACTIVITY_NO_HISTORY)
                            addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        }
                        startActivity(intent)

                        cameraPermissionDialog?.cancel()
                        cameraPermissionDialog = null
                    }
                }
            }
        }

        if (requestCode == PHOTO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (viewModel.uploadList.get()!!.size - 1 < IntroductionConstants.UPLOAD_PHOTO_LIMIT) {
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                        // user cannot pick images from anywhere with ACTION_PICK_IMAGES
                        Intent(MediaStore.ACTION_PICK_IMAGES, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                            val remainingImage = IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.uploadList.get()?.count { it.path != null } ?: 0)
                            if (remainingImage != 1) {
                                putExtra(
                                    MediaStore.EXTRA_PICK_IMAGES_MAX,
                                    IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.uploadList.get()?.count { it.path != null } ?: 0))
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
                } else {
                    Toast.makeText(
                        this@UploadPhotoActivity,
                        "Upload a maximum of ${IntroductionConstants.UPLOAD_PHOTO_LIMIT} photos only...",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            } else {
                var openSettings = false

                for (permission in permissions) {
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        openSettings = true
                        break
                    }
                }

                if (openSettings) {
                    showImagePermissionDialog()

                    val btnContinue = imagePermissionDialog?.findViewById<TextView>(R.id.btn_open_settings)
                    btnContinue?.setOnClickListener {
                        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                        with(intent) {
                            data = Uri.fromParts("package", packageName, null)
                            addCategory(CATEGORY_DEFAULT)
                            addFlags(FLAG_ACTIVITY_NEW_TASK)
                            addFlags(FLAG_ACTIVITY_NO_HISTORY)
                            addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        }
                        startActivity(intent)

                        imagePermissionDialog?.cancel()
                        imagePermissionDialog = null
                    }
                }
            }
        }
    }

    private fun checkUploadError() {
        val list = ArrayList(viewModel.uploadList.get() ?: arrayListOf<PhotoValidationModel>())
        list.removeAll { it.path == null }
        if (list.isNotEmpty()) {
            mixPanel?.track(TAG, JSONObject().apply {
                put("Rejection Status", list.joinToString(", ") { it.reject?.name.toString() })
            })
        }

        val count = viewModel.uploadList.get()?.count { it.path != null }
        viewModel.photoCount.set(count)

        val isValid = viewModel.uploadList.get()?.any { it.reject != null }

        if ((count == null) || (count < 2)) {
            viewModel.validationError.set("Minimum 2 photos required.")
            viewModel.isNextEnabled.set(false)
            viewModel.photoState.set(PhotoState.PHOTOS_PENDING)
        } else if (isValid == true) {
            viewModel.validationError.set("Please remove invalid images.")
            viewModel.isNextEnabled.set(false)
            viewModel.photoState.set(PhotoState.PHOTOS_REJECTED)
        } else {
            viewModel.validationError.set("")
            viewModel.isNextEnabled.set(true)
            viewModel.photoState.set(PhotoState.PHOTOS_VERIFIED)
        }
    }

    private fun getImages() {
        if (viewModel.uploadList.get() == null) {
            val imageList = arrayListOf<PhotoValidationModel>()
            imageList.add(PhotoValidationModel(null))
            imageList.add(PhotoValidationModel(null))
            imageList.add(PhotoValidationModel(null))
            imageList.add(PhotoValidationModel(null))
            imageList.add(PhotoValidationModel(null))
            imageList.add(PhotoValidationModel(null))
            viewModel.uploadList.set(imageList)

            if (!getIntroductionPrefs().photoList.isNullOrEmpty()) {
                getIntroductionPrefs().photoList!!.forEachIndexed { index, path ->
                    viewModel.uploadList.set(viewModel.uploadList.get()?.apply {
                        set(index, PhotoValidationModel(path = path))
                    })
                }
                checkUploadError()

                viewModel.photoState.set(PhotoState.SELFIE_PENDING)
            } else {
                checkUploadError()
            }

            photoAdapter.notifyItemRangeChanged(0, photoAdapter.itemCount)
        }

        val selfie = getIntroductionPrefs().selfie
        if (!selfie.isNullOrEmpty()) {
            if (File(selfie).exists()) {
                viewModel.selfiePath.set(selfie)
                viewModel.selfieFile.set(File(selfie))
                viewModel.photoState.set(PhotoState.SELFIE_VERIFIED)
            }
        }

        when (viewModel.photoState.get()!!) {
            PhotoState.PHOTOS_PENDING -> {

            }

            PhotoState.PHOTOS_REJECTED -> {

            }

            PhotoState.PHOTOS_VERIFIED -> {

            }

            PhotoState.SELFIE_PENDING -> {
                showTakeSelfieDialog()
            }

            PhotoState.SELFIE_VERIFIED -> {
                showTakeSelfieDialog()
            }

            PhotoState.UPLOADING_DATA -> {
                signUp()
            }
        }
    }

    private fun uploadSplashData() {
        val isSplashDataAvailable = try {
            getKoinObject().get<SplashData>()
            true
        } catch (e: Exception) {
            false
        }
        if (isSplashDataAvailable) {
            val splashData = getKoinObject().get<SplashData>()
            splashData.referrers(
                onSuccess = {
                    splashData.getCountryDataApi(
                        onSuccess = {
                            splashData.sendUserData(onSuccess = {
                                unloadKoinModules(splashDataModule)
                            }, onError = {
                                unloadKoinModules(splashDataModule)
                            })
                        },
                        onError = {
                            unloadKoinModules(splashDataModule)
                        },
                    )
                },
                onError = {
                    unloadKoinModules(splashDataModule)
                },
            )
        }
    }

    private fun afterGist() {
        getImages()

        if (getGistPrefs().appRedirectOtherAppStatus) {
            AppDialog.showAppRedirectDialog(
                context = this,
                onManage = {
                    logger(TAG, "redirectPath: ${getGistPrefs().appNewPackageName}")

                    try {
                        val marketUri = getGistPrefs().appNewPackageName.toUri()
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (ignored: Exception) {
                        Toast.makeText(this, "Something want wrong", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        } else if (isAppUpdateRequired()) {
            val isFlexible = getGistPrefs().appUpdateAppDialogStatus

            AppDialog.showAppUpdateDialog(
                context = this,
                isFlexible = isFlexible,
                onClose = {

                },
                onManage = {
                    val packageName = getGistPrefs().appUpdatePackageName
                    logger(TAG, "packageName: $packageName")

                    try {
                        val marketUri = "https://play.google.com/store/apps/details?id=${packageName}".toUri()
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (ignored: Exception) {
                        Toast.makeText(this, "Something want wrong", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }

    private fun getGist() {
        getGistData(
            lifecycleScope,
            isLoading = {
                if (it) {
                    LoadingDialog.show(this)
                } else {
                    LoadingDialog.hide()
                }
            },
            gistNotAvailable = {
                uploadSplashData()

                afterGist()
            },
            onError = {
                InitFailedDialog.showAppUpdateDialog(onTryAgain = {
                    getGist()
                }, onReOpenApp = {
                    reOpenApp()
                })
            },
            onSuccess = {
                unloadKoinModules(gistModule)

                LoadingDialog.show(this)

                uploadSplashData()

                ManageAds.loadAds()

                AdmobAds.showAppOpenAdAfterSplash {
                    LoadingDialog.hide()

                    afterGist()
                }
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        if (intent.hasExtra("load_gist") && intent.getBooleanExtra("load_gist", true)) {
            getGist()
        } else {
            afterGist()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        if (isConnected) {
            if (intent.hasExtra("load_gist") && intent.getBooleanExtra("load_gist", true)) {
                getGist()
            } else {
                afterGist()
            }
        }
    }

    override fun onAddClickCallBack(pos: Int) {
        if (!hasPermission(*imagePermission)) {
            askPermissions(imagePermission, PHOTO_PERMISSION_CODE)
        } else {
            if (viewModel.uploadList.get()!!.size - 1 < IntroductionConstants.UPLOAD_PHOTO_LIMIT) {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                    // user cannot pick images from anywhere with ACTION_PICK_IMAGES
                    Intent(MediaStore.ACTION_PICK_IMAGES, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                        val remainingImage = IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.uploadList.get()?.count { it.path != null } ?: 0)
                        if (remainingImage != 1) {
                            putExtra(
                                MediaStore.EXTRA_PICK_IMAGES_MAX,
                                IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.uploadList.get()?.count { it.path != null } ?: 0))
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
            } else {
                Toast.makeText(
                    this@UploadPhotoActivity,
                    "Upload a maximum of ${IntroductionConstants.UPLOAD_PHOTO_LIMIT} photos only...",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    override fun onDeleteClickCallBack(pos: Int) {
        checkUploadError()
    }
}
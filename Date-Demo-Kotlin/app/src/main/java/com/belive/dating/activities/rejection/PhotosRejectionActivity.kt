package com.belive.dating.activities.rejection

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.camera.CameraActivity
import com.belive.dating.activities.camera.SelfieGuidelineAdapter
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.activities.introduction.upload_photo.PhotoValidationModel
import com.belive.dating.activities.introduction.upload_photo.Reject
import com.belive.dating.activities.introduction.upload_photo.policy.PhotoUploadPolicyActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.ads.ManageAds
import com.belive.dating.constants.IntroductionConstants
import com.belive.dating.databinding.ActivityPhotosRejectionBinding
import com.belive.dating.di.deepLinkViewModels
import com.belive.dating.di.gistModule
import com.belive.dating.di.splashDataModule
import com.belive.dating.dialogs.AppDialog
import com.belive.dating.dialogs.InitFailedDialog
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.askPermissions
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getBitmapFromLocalPath
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinObject
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
import com.belive.dating.extensions.processImage
import com.belive.dating.extensions.reOpenApp
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.extensions.validateResolutionOfImage
import com.belive.dating.extensions.visible
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.get_gist.getGistData
import com.belive.dating.helpers.helper_functions.grid_layout_manager.WrapperGridLayoutManager
import com.belive.dating.helpers.helper_functions.splash_data.SplashData
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

class PhotosRejectionActivity : NetworkReceiverActivity(), PhotoRejectionAdapter.OnImageClickListener {

    val binding by lazy {
        ActivityPhotosRejectionBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: PhotosRejectionViewModel

    val photoAdapter by lazy {
        PhotoRejectionAdapter(viewModel, this)
    }

    private val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
    ) else arrayOf(
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
                    StringBuilder().append("To select images from your device, we need access to your media storage. Please click below and allow it from settings.")
            }
        }
        imagePermissionDialog?.show()
    }

    private var cameraPermissionDialog: Dialog? = null

    private fun showCameraPermissionDialog() {
        if (cameraPermissionDialog == null) {
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

            rvSelfiePolicies.layoutManager = LinearLayoutManager(this@PhotosRejectionActivity, LinearLayoutManager.VERTICAL, false)
            rvSelfiePolicies.adapter = selfiePolicyAdapter

            val recheckPolicies = arrayListOf<String>()
            recheckPolicies.add("Can you see your <b>whole face</b>?")
            recheckPolicies.add("Is your <b>selfie</b> matching with <b>uploaded photos</b>?")

            val recheckAdapter = SelfieGuidelineAdapter()
            recheckAdapter.guidelineList = recheckPolicies

            rvRecheckPolicies.layoutManager = LinearLayoutManager(this@PhotosRejectionActivity, LinearLayoutManager.VERTICAL, false)
            rvRecheckPolicies.adapter = recheckAdapter

            if (viewModel.profileState.get() == ProfileState.SELFIE_PENDING) {
                btnNext.text = StringBuilder().append("Take a Selfie")
                btnRetakeSelfie.gone()
                rvSelfiePolicies.visible()
                recheckSelfieTitle.gone()
                rvRecheckPolicies.gone()

                imgSelfie.loadSelfie(null)
            } else if (viewModel.profileState.get() == ProfileState.SELFIE_VERIFIED) {
                btnNext.text = StringBuilder().append("Start Matching Now")
                btnRetakeSelfie.visible()
                rvSelfiePolicies.gone()
                recheckSelfieTitle.visible()
                rvRecheckPolicies.visible()

                imgSelfie.loadSelfie(viewModel.selfiePath.get())
            }

            viewModel.profileState.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    val state = viewModel.profileState.get()

                    when (state) {
                        ProfileState.SELFIE_PENDING -> {
                            btnNext.text = StringBuilder().append("Take a Selfie")
                            btnRetakeSelfie.gone()
                            rvSelfiePolicies.visible()
                            recheckSelfieTitle.gone()
                            rvRecheckPolicies.gone()

                            imgSelfie.loadSelfie(null)
                        }

                        ProfileState.SELFIE_VERIFIED -> {
                            btnNext.text = StringBuilder().append("Start Matching Now")
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
                viewModel.selfieFile.set(null)
                viewModel.selfiePath.set(null)
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
                    getUserPrefs().selfie = viewModel.selfiePath.get()

                    viewModel.profileState.set(ProfileState.UPLOADING_DATA)

                    cancel()

                    updateImages()
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

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(PhotosRejectionActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(PhotosRejectionActivity::class.java.simpleName)
    }

    override fun onDestroy() {
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

        viewModel = tryKoinViewModel(listOf(deepLinkViewModels))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.root.post {
            observeNetwork()
        }

        initViews()

        clickListeners()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@PhotosRejectionActivity, "You have to new photos to use the app", Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnPolicy.setOnClickListener {
            startActivity(Intent(this, PhotoUploadPolicyActivity::class.java))
            swipeRight()
        }

        binding.btnNext.setOnClickListener {
            if (viewModel.isSelfieRequired.get() == true) {
                viewModel.profileState.set(ProfileState.SELFIE_PENDING)
                viewModel.selfiePath.set(null)
                viewModel.selfieFile.set(null)

                showTakeSelfieDialog()
            } else {
                updateImages()
            }
        }
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

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

    private fun updateImages() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.updateImages(getUserPrefs().selfie?.let { prepareImagePart("selfie", it) }).collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@PhotosRejectionActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@PhotosRejectionActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT)
                                .show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@PhotosRejectionActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()
                            Toast.makeText(this@PhotosRejectionActivity, it.message, Toast.LENGTH_SHORT).show()

                            getUserPrefs().selfie = null
                            viewModel.profileState.set(ProfileState.SELFIE_PENDING)
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().selfie = null

                                startActivity(Intent(this@PhotosRejectionActivity, MainActivity::class.java).apply {
                                    putExtra("display_splash", false)
                                })
                                finishAffinity()
                                swipeLeft()
                            } else {
                                Toast.makeText(this@PhotosRejectionActivity, "Something went wrong...!!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkUploadError() {
        val count = viewModel.photoList.get()?.count { it.path != null }
        viewModel.photoCount.set(count)

        val isValid = viewModel.photoList.get()?.any { it.reject != null }

        if ((count == null) || (count < 2)) {
            viewModel.validationError.set("Minimum 2 photos required.")
            viewModel.isNextButtonEnabled.set(false)
            viewModel.profileState.set(ProfileState.PHOTOS_PENDING)
        } else if (isValid == true) {
            viewModel.validationError.set("Please remove invalid images.")
            viewModel.isNextButtonEnabled.set(false)
            viewModel.profileState.set(ProfileState.PHOTOS_REJECTED)
        } else {
            // no image errors and have at least 2 images
            viewModel.isNextButtonEnabled.set(true)
            viewModel.validationError.set("")
            viewModel.profileState.set(ProfileState.PHOTOS_VERIFIED)
        }
    }

    private fun getImages() {
        if (viewModel.photoList.get() == null) {
            val appLinkIntent: Intent = intent
            val notificationID: String? =
                appLinkIntent.data?.getQueryParameter("notiId") ?: if (appLinkIntent.hasExtra("notiId")) appLinkIntent.getIntExtra("notiId", -1)
                    .toString() else null

            lifecycleScope.launch(Dispatchers.IO) {
                launch {
                    viewModel.getImages().collectLatest {
                        launch(Dispatchers.Main) {
                            when (it.status) {
                                Status.LOADING -> {
                                    LoadingDialog.show(this@PhotosRejectionActivity)
                                }

                                Status.SIGN_OUT -> {
                                    Toast.makeText(this@PhotosRejectionActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT)
                                        .show()

                                    authOut()
                                }

                                Status.ADMIN_BLOCKED -> {
                                    Toast.makeText(
                                        this@PhotosRejectionActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT
                                    ).show()

                                    authOut()
                                }

                                Status.ERROR -> {
                                    LoadingDialog.hide()

                                    Toast.makeText(this@PhotosRejectionActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
                                }

                                Status.SUCCESS -> {
                                    LoadingDialog.hide()

                                    if (it.data != null) {
                                        val list = it.data.imageList

                                        val imageList = arrayListOf<PhotoValidationModel>()
                                        imageList.add(PhotoValidationModel(null))
                                        imageList.add(PhotoValidationModel(null))
                                        imageList.add(PhotoValidationModel(null))
                                        imageList.add(PhotoValidationModel(null))
                                        imageList.add(PhotoValidationModel(null))
                                        imageList.add(PhotoValidationModel(null))

                                        list.forEach {
                                            if (it.verification == "rejected") {
                                                imageList[it.position - 1] =
                                                    PhotoValidationModel(path = it.image, reject = Reject.ADMIN_REJECTED, isNetworkImage = true)
                                            } else {
                                                imageList[it.position - 1] =
                                                    PhotoValidationModel(path = it.image, reject = null, isNetworkImage = true)
                                            }
                                        }

                                        val isAnyApproved = list.any { it.verification == "approved" }

                                        if (isAnyApproved) {
                                            viewModel.isSelfieRequired.set(false)
                                        }

                                        viewModel.photoList.set(imageList)
                                        photoAdapter.notifyItemRangeChanged(0, 6)

                                        checkUploadError()
                                    } else {
                                        Toast.makeText(this@PhotosRejectionActivity, it.message ?: "Something went wrong...!", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                        }
                    }
                }

                if ((notificationID != null) && (notificationID != "-1")) {
                    launch {
                        viewModel.readNotification(notificationID.toInt()).collect {
                            when (it.status) {
                                Status.LOADING -> {

                                }

                                Status.ADMIN_BLOCKED -> {
                                    launch(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@PhotosRejectionActivity,
                                            "Admin has blocked you, because of security reasons.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }

                                    authOut()
                                }

                                Status.SIGN_OUT -> {
                                    launch(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@PhotosRejectionActivity,
                                            "Your session has expired, Please log in again.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }

                                    authOut()
                                }

                                Status.ERROR -> {
                                    logger("--error--", "notification read error: ${gsonString(it)}")
                                }

                                Status.SUCCESS -> {
                                    val unreadNotification = it.data?.get("unreadNotification")?.asJsonPrimitive?.asInt
                                    if (unreadNotification != null) {
                                        getUserPrefs().unreadNotificationCount = unreadNotification

                                        val intent = Intent("NOTIFICATION_COUNT")
                                        intent.putExtra("isNotificationCountChanged", true)
                                        LocalBroadcastManager.getInstance(this@PhotosRejectionActivity).sendBroadcast(intent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            when (viewModel.profileState.get()!!) {
                ProfileState.PHOTOS_PENDING -> {

                }

                ProfileState.PHOTOS_REJECTED -> {

                }

                ProfileState.PHOTOS_VERIFIED -> {

                }

                ProfileState.SELFIE_PENDING -> {
                    showTakeSelfieDialog()
                }

                ProfileState.SELFIE_TAKEN -> {
                    showTakeSelfieDialog()
                }

                ProfileState.SELFIE_VERIFIED -> {
                    showTakeSelfieDialog()
                }

                ProfileState.UPLOADING_DATA -> {
                    updateImages()
                }
            }
        }
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

        if (intent.resolveActivity(packageManager) != null) {
            // Launch the camera intent
            imagePickerLauncher.launch(intent)
        } else {
            Toast.makeText(this, "No Gallery found on this phone", Toast.LENGTH_SHORT).show()
        }
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

            logger("--uri--", gsonString(selectedImageUris))

            if (selectedImageUris.isNotEmpty()) {
                val remainingPhotosCount = IntroductionConstants.UPLOAD_PHOTO_LIMIT - (viewModel.photoList.get()?.count { it.path != null } ?: 0)
                if (selectedImageUris.size > remainingPhotosCount) {
                    Toast.makeText(
                        this@PhotosRejectionActivity,
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
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
                    LoadingDialog.show(this@PhotosRejectionActivity)

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
                        LoadingDialog.show(this@PhotosRejectionActivity)

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
                try {
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
                        LoadingDialog.show(this@PhotosRejectionActivity)

                        launch(Dispatchers.IO) {
                            startProcess(position)
                        }
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)

                    catchLog("validateImage: " + gsonString(e))

                    Toast.makeText(this@PhotosRejectionActivity, "Something went wrong, try again...!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateImageWithGoogleVisionResponse(it: Resource<JsonObject?>) {
        when (it.status) {
            Status.LOADING -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    LoadingDialog.show(this@PhotosRejectionActivity)
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
                            LoadingDialog.show(this@PhotosRejectionActivity)

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
                                LoadingDialog.show(this@PhotosRejectionActivity)

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
            viewModel.profileState.set(ProfileState.SELFIE_PENDING)
            val filePath = viewModel.selfieFile.get()?.absolutePath
            viewModel.selfiePath.set(filePath)
            viewModel.profileState.set(ProfileState.SELFIE_VERIFIED)
        }
    }

    private fun authOut() {
        LoadingDialog.show(this)

        val authenticationHelper = getKoinObject().get<AuthenticationHelper>()

        authenticationHelper.signOut(
            lifecycleScope = lifecycleScope,
            onSuccess = {
                LoadingDialog.hide()

                authenticationHelper.completeSignOutOnAuthOutSuccess(this)

                startActivity(Intent(this@PhotosRejectionActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
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
        ManageAds.loadAds()

        getImages()

        uploadSplashData()

        if (getGistPrefs().appRedirectOtherAppStatus) {
            AppDialog.showAppRedirectDialog(
                context = this,
                onManage = {
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

                afterGist()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        if (getGistPrefs().nsfwURL.isEmpty()) {
            getGist()
        } else {
            afterGist()
        }
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        if (isConnected) {
            if (getGistPrefs().nsfwURL.isEmpty()) {
                getGist()
            } else {
                afterGist()
            }
        }
    }

    override fun onPlaceHolderClick(pos: Int) {
        if (!hasPermission(*imagePermission)) {
            askPermissions(imagePermission, PHOTO_PERMISSION_CODE)
        } else {
            if (viewModel.photoList.get()!!.size - 1 < IntroductionConstants.UPLOAD_PHOTO_LIMIT) {
                if ((viewModel.photoList.get()?.size != null) && (viewModel.photoList.get()?.size != 0)) {
                    getImagesFromGallery()
                } else {
                    Toast.makeText(this, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this@PhotosRejectionActivity,
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
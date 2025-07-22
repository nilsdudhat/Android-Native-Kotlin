package com.belive.dating.activities.profile

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.Observable
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.belive.dating.activities.NetworkReceiverActivity
import com.belive.dating.activities.edit_profile.about_me.EditAboutMeActivity
import com.belive.dating.activities.edit_profile.basics.education.EditEducationActivity
import com.belive.dating.activities.edit_profile.basics.family_plan.EditFamilyPlanActivity
import com.belive.dating.activities.edit_profile.basics.marital_status.EditMaritalStatusActivity
import com.belive.dating.activities.edit_profile.basics.religion.EditReligionActivity
import com.belive.dating.activities.edit_profile.basics.zodiac.EditZodiacActivity
import com.belive.dating.activities.edit_profile.your_styles.communication_type.EditCommunicationActivity
import com.belive.dating.activities.edit_profile.height.EditHeightActivity
import com.belive.dating.activities.edit_profile.interests.EditInterestsActivity
import com.belive.dating.activities.edit_profile.languages.EditLanguagesActivity
import com.belive.dating.activities.edit_profile.lifestyles.diet.EditDietActivity
import com.belive.dating.activities.edit_profile.lifestyles.drinking.EditDrinkingActivity
import com.belive.dating.activities.edit_profile.lifestyles.pets.EditPetActivity
import com.belive.dating.activities.edit_profile.lifestyles.sleeping_habit.EditSleepingHabitActivity
import com.belive.dating.activities.edit_profile.lifestyles.smoking.EditSmokingActivity
import com.belive.dating.activities.edit_profile.lifestyles.social_media.EditSocialStatusActivity
import com.belive.dating.activities.edit_profile.lifestyles.workout.EditWorkoutActivity
import com.belive.dating.activities.edit_profile.your_styles.love_type.EditLoveTypeActivity
import com.belive.dating.activities.edit_profile.opposite_gender.EditOppositeGenderActivity
import com.belive.dating.activities.edit_profile.your_styles.personality_type.EditPersonalityActivity
import com.belive.dating.activities.edit_profile.profile_images.EditPhotosActivity
import com.belive.dating.activities.edit_profile.relationship_goal.EditRelationshipGoalActivity
import com.belive.dating.activities.edit_profile.school.EditSchoolActivity
import com.belive.dating.activities.edit_profile.sexual_orientation.EditSexualOrientationActivity
import com.belive.dating.activities.signin.SignInActivity
import com.belive.dating.activities.user_details.UserDetailsActivity
import com.belive.dating.constants.EventConstants
import com.belive.dating.databinding.ActivityProfileBinding
import com.belive.dating.di.googleVisionModule
import com.belive.dating.di.profileViewModel
import com.belive.dating.dialogs.LoadingDialog
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.setSystemBarColors
import com.belive.dating.extensions.swipeLeft
import com.belive.dating.extensions.swipeRight
import com.belive.dating.extensions.swipeUp
import com.belive.dating.extensions.tryKoinViewModel
import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import com.belive.dating.helpers.helper_functions.linear_layout_manager.WrapLinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import kotlin.math.roundToInt

/**
 * Activity for displaying and managing the user's profile information.
 *
 * This activity allows users to view and edit their personal details, photos,
 * lifestyle information, and more.  It utilizes data binding to connect UI
 * elements to a [ProfileViewModel], and interacts with various other activities
 * for editing specific profile sections.
 *
 * Key Features:
 *  - Displays user profile information, including photos, personal details,
 *    about me section, lifestyle choices, and interests.
 *  - Allows navigation to edit individual profile sections through dedicated activities.
 *  - Tracks profile completion percentage and displays it to the user.
 *  - Integrates with Mixpanel for event tracking.
 *  - Handles network connectivity changes through inheritance from [NetworkReceiverActivity].
 *  - Uses Koin for dependency injection of the [ProfileViewModel] and other modules.
 *  - Implements [PhotoAdapter.OnPhotoClickListener] to handle photo interactions.
 */
class ProfileActivity : NetworkReceiverActivity(), PhotoAdapter.OnPhotoClickListener {

    val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    lateinit var viewModel: ProfileViewModel

    private val photoAdapter by lazy {
        PhotoAdapter(viewModel, this)
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
            listenEvents()
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

    private fun initViews() {
        updateTitle()

        updateUserImages()

        updatePersonalDetails()

        updateAboutMe()

        updateHeight()

        updateHideAge()

        updateHideDistance()

        updateLivingIn()

        updateRelationshipGoal()

        updateOppositeGender()

        updateSexualOrientation()

        updateSchool()

        updateLanguages()

        updateZodiac()

        updateEducation()

        updateReligion()

        updateMaritalStatus()

        updateFamilyPlan()

        updatePersonality()

        updateCommunications()

        updateLoveStyles()

        updatePet()

        updateDrinking()

        updateSmoking()

        updateWorkout()

        updateSocialStatus()

        updatePreferredDiet()

        updateSleepingHabit()

        updateInterests()

        updateLifestyle()
    }

    private fun clickListeners() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if ((getUserPrefs().hideAge == viewModel.hideAge.get()) && (getUserPrefs().hideDistance == viewModel.hideDistance.get())) {
                    // no change detected

                    unloadKoinModules(profileViewModel)

                    finish()
                    swipeLeft()
                } else {
                    saveProfileChanges()
                }
            }
        })

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.layoutPreview.setOnClickListener {
            startActivity(
                Intent(this, UserDetailsActivity::class.java).apply {
                    putExtra("userId", getUserPrefs().userId)
                    putExtra("isPreview", true)
                })
            swipeUp()
        }

        binding.btnAboutMe.setOnClickListener {
            startActivity(Intent(this, EditAboutMeActivity::class.java))
            swipeRight()
        }

        binding.heightContainer.setOnClickListener {
            startActivity(Intent(this, EditHeightActivity::class.java))
            swipeRight()
        }

        binding.switchAge.setOnCheckedChangeListener { _, isChecked ->
            viewModel.hideAge.set(isChecked)
        }

        binding.switchDistance.setOnCheckedChangeListener { _, isChecked ->
            viewModel.hideDistance.set(isChecked)
        }

        binding.relationshipGoalContainer.setOnClickListener {
            startActivity(Intent(this, EditRelationshipGoalActivity::class.java))
            swipeRight()
        }

        binding.dateInterestContainer.setOnClickListener {
            startActivity(Intent(this, EditOppositeGenderActivity::class.java))
            swipeRight()
        }

        binding.sexualOrientationContainer.setOnClickListener {
            startActivity(Intent(this, EditSexualOrientationActivity::class.java))
            swipeRight()
        }

        binding.schoolContainer.setOnClickListener {
            startActivity(Intent(this, EditSchoolActivity::class.java))
            swipeRight()
        }

        binding.languagesContainer.setOnClickListener {
            startActivity(Intent(this, EditLanguagesActivity::class.java))
            swipeRight()
        }

        binding.layoutZodiac.setOnClickListener {
            startActivity(Intent(this, EditZodiacActivity::class.java))
            swipeRight()
        }

        binding.layoutEducation.setOnClickListener {
            startActivity(Intent(this, EditEducationActivity::class.java))
            swipeRight()
        }

        binding.layoutReligion.setOnClickListener {
            startActivity(Intent(this, EditReligionActivity::class.java))
            swipeRight()
        }

        binding.layoutMaritalStatus.setOnClickListener {
            startActivity(Intent(this, EditMaritalStatusActivity::class.java))
            swipeRight()
        }

        binding.layoutFamilyPlans.setOnClickListener {
            startActivity(Intent(this, EditFamilyPlanActivity::class.java))
            swipeRight()
        }

        binding.layoutPersonalityType.setOnClickListener {
            startActivity(Intent(this, EditPersonalityActivity::class.java))
            swipeRight()
        }

        binding.layoutCommunicationType.setOnClickListener {
            startActivity(Intent(this, EditCommunicationActivity::class.java))
            swipeRight()
        }

        binding.layoutLoveType.setOnClickListener {
            startActivity(Intent(this, EditLoveTypeActivity::class.java))
            swipeRight()
        }

        binding.interestsContainer.setOnClickListener {
            startActivity(Intent(this, EditInterestsActivity::class.java))
            swipeRight()
        }

        binding.layoutPets.setOnClickListener {
            startActivity(Intent(this, EditPetActivity::class.java))
            swipeRight()
        }

        binding.layoutDrinking.setOnClickListener {
            startActivity(Intent(this, EditDrinkingActivity::class.java))
            swipeRight()
        }

        binding.layoutSmoking.setOnClickListener {
            startActivity(Intent(this, EditSmokingActivity::class.java))
            swipeRight()
        }

        binding.layoutWorkout.setOnClickListener {
            startActivity(Intent(this, EditWorkoutActivity::class.java))
            swipeRight()
        }

        binding.layoutDiet.setOnClickListener {
            startActivity(Intent(this, EditDietActivity::class.java))
            swipeRight()
        }

        binding.layoutSocialMedia.setOnClickListener {
            startActivity(Intent(this, EditSocialStatusActivity::class.java))
            swipeRight()
        }

        binding.layoutSleepingHabit.setOnClickListener {
            startActivity(Intent(this, EditSleepingHabitActivity::class.java))
            swipeRight()
        }
    }

    private fun updateInterests() {
        binding.interestsContainer.post {
            val interestsCount = getUserPrefs().myInterests?.size
            viewModel.interestsCount.set(interestsCount)
        }
    }

    private fun updateZodiac() {
        binding.layoutZodiac.post {
            viewModel.zodiac.set(getUserPrefs().zodiac?.name)

            updateBasics()
        }
    }

    private fun updateEducation() {
        binding.layoutEducation.post {
            viewModel.education.set(getUserPrefs().education?.name)

            updateBasics()
        }
    }

    private fun updateReligion() {
        binding.layoutReligion.post {
            viewModel.religion.set(getUserPrefs().religion?.name)

            updateBasics()
        }
    }

    private fun updateMaritalStatus() {
        binding.layoutMaritalStatus.post {
            viewModel.maritalStatus.set(getUserPrefs().maritalStatus?.name)

            updateBasics()
        }
    }

    private fun updateFamilyPlan() {
        binding.layoutFamilyPlans.post {
            viewModel.familyPlan.set(getUserPrefs().familyPlan?.name)

            updateBasics()
        }
    }

    private fun updateBasics() {
        if (viewModel.zodiac.get().isNullOrEmpty()) {
            viewModel.isIncompleteBasics.set(true)
        } else if (viewModel.education.get().isNullOrEmpty()) {
            viewModel.isIncompleteBasics.set(true)
        } else if (viewModel.religion.get().isNullOrEmpty()) {
            viewModel.isIncompleteBasics.set(true)
        } else if (viewModel.maritalStatus.get().isNullOrEmpty()) {
            viewModel.isIncompleteBasics.set(true)
        } else if (viewModel.familyPlan.get().isNullOrEmpty()) {
            viewModel.isIncompleteBasics.set(true)
        } else {
            viewModel.isIncompleteBasics.set(false)
        }

        if (!viewModel.zodiac.get().isNullOrEmpty() || !viewModel.education.get().isNullOrEmpty() || !viewModel.religion.get()
                .isNullOrEmpty() || !viewModel.maritalStatus.get().isNullOrEmpty() || !viewModel.familyPlan.get().isNullOrEmpty()
        ) {
            viewModel.isShowBasicsPercentage.set(false)
        } else {
            viewModel.isShowBasicsPercentage.set(true)
        }
    }

    private fun updateSexualOrientation() {
        binding.sexualOrientationContainer.post {
            viewModel.sexualOrientation.set(getUserPrefs().sexualOrientation?.name)
        }
    }

    private fun updateOppositeGender() {
        binding.dateInterestContainer.post {
            viewModel.oppositeGender.set(getUserPrefs().oppositeGender?.lowercase()?.let {
                it.replaceFirstChar { char -> char.uppercaseChar() }
            })
        }
    }

    private fun updateRelationshipGoal() {
        binding.relationshipGoalContainer.post {
            viewModel.relationshipGoal.set(getUserPrefs().relationshipGoal?.name)
        }
    }

    private fun setUpRecyclerview() {
        if (binding.rvPhotos.layoutManager == null) {
            binding.rvPhotos.layoutManager = WrapLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }

        if (binding.rvPhotos.adapter == null) {
            binding.rvPhotos.adapter = photoAdapter
        }
    }

    private fun updatePersonality() {
        binding.layoutPersonalityType.post {
            val personalityCount = getUserPrefs().personalityTypes?.size
            viewModel.personalityCount.set(if (personalityCount == 0) null else personalityCount)

            updateStyle()
        }
    }

    private fun updateCommunications() {
        binding.layoutCommunicationType.post {
            val communicationsCount = getUserPrefs().communicationTypes?.size
            viewModel.communicationCount.set(if (communicationsCount == 0) null else communicationsCount)

            updateStyle()
        }
    }

    private fun updateLoveStyles() {
        binding.layoutLoveType.post {
            val loveTypesCount = getUserPrefs().loveTypes?.size
            viewModel.loveTypeCount.set(if (loveTypesCount == 0) null else loveTypesCount)

            updateStyle()
        }
    }

    private fun updateStyle() {
        if (viewModel.personalityCount.get() == null) {
            viewModel.isIncompleteStyle.set(true)
        } else if (viewModel.communicationCount.get() == null) {
            viewModel.isIncompleteStyle.set(true)
        } else if (viewModel.loveTypeCount.get() == null) {
            viewModel.isIncompleteStyle.set(true)
        } else {
            viewModel.isIncompleteStyle.set(false)
        }

        if ((viewModel.personalityCount.get() != null) || (viewModel.communicationCount.get() != null) || (viewModel.loveTypeCount.get() != null)) {
            viewModel.isShowStylePercentage.set(false)
        } else {
            viewModel.isShowStylePercentage.set(true)
        }
    }

    private fun updatePet() {
        binding.layoutPets.post {
            val pet = getUserPrefs().pet
            viewModel.pet.set(if (pet.isNullOrEmpty()) null else pet)

            updateLifestyle()
        }
    }

    private fun updateDrinking() {
        binding.layoutDrinking.post {
            val drinking = getUserPrefs().drinking
            viewModel.drinking.set(if (drinking.isNullOrEmpty()) null else drinking)

            updateLifestyle()
        }
    }

    private fun updateSmoking() {
        binding.layoutSmoking.post {
            val smoking = getUserPrefs().smoking
            viewModel.smoking.set(if (smoking.isNullOrEmpty()) null else smoking)

            updateLifestyle()
        }
    }

    private fun updateWorkout() {
        binding.layoutWorkout.post {
            val workout = getUserPrefs().workout
            viewModel.workout.set(if (workout.isNullOrEmpty()) null else workout)

            updateLifestyle()
        }
    }

    private fun updateSocialStatus() {
        binding.layoutSocialMedia.post {
            val socialStatus = getUserPrefs().socialStatus
            viewModel.socialMedia.set(if (socialStatus.isNullOrEmpty()) null else socialStatus)

            updateLifestyle()
        }
    }

    private fun updatePreferredDiet() {
        binding.layoutDiet.post {
            val preferredDiet = getUserPrefs().preferredDiet
            viewModel.preferredDiet.set(if (preferredDiet.isNullOrEmpty()) null else preferredDiet)

            updateLifestyle()
        }
    }

    private fun updateSleepingHabit() {
        binding.layoutSleepingHabit.post {
            val sleepingHabit = getUserPrefs().sleepingHabit
            viewModel.sleepingHabit.set(if (sleepingHabit.isNullOrEmpty()) null else sleepingHabit)

            updateLifestyle()
        }
    }

    private fun updateLifestyle() {
        if (viewModel.pet.get().isNullOrEmpty()) {
            viewModel.isIncompleteLifestyle.set(true)
        } else if (viewModel.drinking.get().isNullOrEmpty()) {
            viewModel.isIncompleteLifestyle.set(true)
        } else if (viewModel.smoking.get().isNullOrEmpty()) {
            viewModel.isIncompleteLifestyle.set(true)
        } else if (viewModel.workout.get().isNullOrEmpty()) {
            viewModel.isIncompleteLifestyle.set(true)
        } else if (viewModel.preferredDiet.get().isNullOrEmpty()) {
            viewModel.isIncompleteLifestyle.set(true)
        } else if (viewModel.socialMedia.get().isNullOrEmpty()) {
            viewModel.isIncompleteLifestyle.set(true)
        } else if (viewModel.sleepingHabit.get().isNullOrEmpty()) {
            viewModel.isIncompleteLifestyle.set(true)
        } else {
            viewModel.isIncompleteLifestyle.set(false)
        }

        if (!viewModel.pet.get().isNullOrEmpty() || !viewModel.drinking.get().isNullOrEmpty() || !viewModel.smoking.get()
                .isNullOrEmpty() || !viewModel.workout.get().isNullOrEmpty() || !viewModel.preferredDiet.get()
                .isNullOrEmpty() || !viewModel.socialMedia.get().isNullOrEmpty() || !viewModel.sleepingHabit.get().isNullOrEmpty()
        ) {
            viewModel.isShowLifestylePercentage.set(false)
        } else {
            viewModel.isShowLifestylePercentage.set(true)
        }
    }

    private fun updateLanguages() {
        binding.languagesContainer.post {
            val languagesCount = getUserPrefs().knownLanguages?.size
            viewModel.languagesCount.set(if (languagesCount == 0) null else languagesCount)
        }
    }

    private fun updateSchool() {
        binding.schoolContainer.post {
            viewModel.school.set(getUserPrefs().school)
        }
    }

    private fun updateLivingIn() {
        viewModel.livingIn.set(
            if (!getUserPrefs().currentCity.isNullOrEmpty() && !getUserPrefs().currentState.isNullOrEmpty() && !getUserPrefs().currentCountry.isNullOrEmpty()) {
                "${getUserPrefs().currentCity}, ${getUserPrefs().currentState}, ${getUserPrefs().currentCountry}"
            } else if (!getUserPrefs().currentState.isNullOrEmpty() && !getUserPrefs().currentCountry.isNullOrEmpty()) {
                "${getUserPrefs().currentState}, ${getUserPrefs().currentCountry}"
            } else if (!getUserPrefs().currentCountry.isNullOrEmpty()) {
                getUserPrefs().currentCountry
            } else {
                null
            }
        )
    }

    private fun updateHeight() {
        binding.heightContainer.post {
            if (getUserPrefs().height == null) {
                viewModel.height.set(null)
            } else {
                val feet = getUserPrefs().height!!.feet
                val inch = getUserPrefs().height!!.inch
                val centimeters = ((feet * 30.48) + (inch * 2.54)).roundToInt()

                viewModel.height.set("${feet}` ${inch}`` ( $centimeters cm )")
            }
        }
    }

    private fun updateHideAge() {
        binding.switchAge.post {
            viewModel.hideAge.set(getUserPrefs().hideAge)
        }
    }

    private fun updateHideDistance() {
        binding.switchDistance.post {
            viewModel.hideDistance.set(getUserPrefs().hideDistance)
        }
    }

    private fun updateAboutMe() {
        viewModel.aboutMe.set(getUserPrefs().aboutMe)
    }

    private fun updatePersonalDetails() {
        viewModel.name.set(getUserPrefs().fullName)
        viewModel.birthDate.set(getUserPrefs().birthDate)
        viewModel.gender.set(getUserPrefs().gender)

        if (viewModel.name.get().isNullOrEmpty()) {
            viewModel.isIncompletePersonalDetails.set(true)
        } else if (viewModel.birthDate.get().isNullOrEmpty()) {
            viewModel.isIncompletePersonalDetails.set(true)
        } else if (viewModel.gender.get().isNullOrEmpty()) {
            viewModel.isIncompletePersonalDetails.set(true)
        } else {
            viewModel.isIncompletePersonalDetails.set(false)
        }
    }

    private fun updateTitle() {
        binding.toolbarTitle.post {
            val title = StringBuilder().append(getUserPrefs().completeProfilePercentage).append("% Complete").toString()

            val spannable = SpannableString(title)

            viewModel.profilePercentageTitle.set(spannable)

            logger("--percentage--", getUserPrefs().completeProfilePercentage)
        }
    }

    private fun photoCount() {
        val photoCount = if (viewModel.photoList.get() == null) {
            0
        } else {
            viewModel.photoList.get()!!.size - 1
        }
        viewModel.photosCount.set(photoCount)
    }

    private fun updateUserImages() {
        setUpRecyclerview()

        if (viewModel.photoList.get()?.size != 0) {
            photoAdapter.notifyItemRangeRemoved(0, viewModel.photoList.get()!!.size)
        }
        viewModel.photoList.get()?.clear()

        val photoList = getUserPrefs().userImages

        photoList?.forEachIndexed { index, photo ->
            viewModel.photoList.get()?.add(index, photo.image)
        }

        viewModel.photoList.get()?.add(null)
        photoAdapter.notifyItemRangeInserted(0, viewModel.photoList.get()!!.size)

        photoCount()
    }

    private fun saveProfileChanges() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveProfileChanges().collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {
                            LoadingDialog.show(this@ProfileActivity)
                        }

                        Status.ADMIN_BLOCKED -> {
                            Toast.makeText(this@ProfileActivity, "Admin has blocked you, because of security reasons.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.SIGN_OUT -> {
                            Toast.makeText(this@ProfileActivity, "Your session has expired, Please log in again.", Toast.LENGTH_SHORT).show()

                            authOut()
                        }

                        Status.ERROR -> {
                            LoadingDialog.hide()
                            Toast.makeText(this@ProfileActivity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        Status.SUCCESS -> {
                            LoadingDialog.hide()

                            if (it.data != null) {
                                getUserPrefs().hideAge = it.data.user.hideAge
                                getUserPrefs().hideDistance = it.data.user.hideDistance

                                onBackPressedDispatcher.onBackPressed()
                            } else {
                                Toast.makeText(this@ProfileActivity, "Something went wrong, please try again...!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
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

                startActivity(Intent(this@ProfileActivity, SignInActivity::class.java))
                finishAffinity()
                swipeLeft()
            },
        )
    }

    override fun onInternetAvailableForFirstTime() {
        super.onInternetAvailableForFirstTime()
    }

    override fun onInternetConfigurationChanged(isConnected: Boolean) {
        super.onInternetConfigurationChanged(isConnected)
    }

    override fun observeEvents(key: String, subscriberId: String, value: Any?) {
        super.observeEvents(key, subscriberId, value)

        when (key) {
            EventConstants.UPDATE_PROFILE_PERCENTAGE -> {
                updateTitle()
            }

            EventConstants.UPDATE_IMAGES -> {
                updateUserImages()
            }

            EventConstants.UPDATE_ABOUT_ME -> {
                viewModel.aboutMe.set(getUserPrefs().aboutMe)
            }

            EventConstants.UPDATE_KNOWN_LANGUAGES -> {
                updateLanguages()
            }

            EventConstants.UPDATE_OPPOSITE_GENDER -> {
                updateOppositeGender()
            }

            EventConstants.UPDATE_DRINKING -> {
                updateDrinking()
            }

            EventConstants.UPDATE_SMOKING -> {
                updateSmoking()
            }

            EventConstants.UPDATE_WORKOUT -> {
                updateWorkout()
            }

            EventConstants.UPDATE_SOCIAL_STATUS -> {
                updateSocialStatus()
            }

            EventConstants.UPDATE_SCHOOL -> {
                updateSchool()
            }

            EventConstants.UPDATE_PREFERRED_DIET -> {
                updatePreferredDiet()
            }

            EventConstants.UPDATE_SLEEPING_HABIT -> {
                updateSleepingHabit()
            }

            EventConstants.UPDATE_PET -> {
                updatePet()
            }

            EventConstants.UPDATE_PERSONALITY -> {
                updatePersonality()
            }

            EventConstants.UPDATE_COMMUNICATIONS -> {
                updateCommunications()
            }

            EventConstants.UPDATE_LOVE_TYPES -> {
                updateLoveStyles()
            }

            EventConstants.UPDATE_HEIGHT -> {
                updateHeight()
            }

            EventConstants.UPDATE_SEXUAL_ORIENTATION -> {
                updateSexualOrientation()
            }

            EventConstants.UPDATE_RELATIONSHIP_GOAL -> {
                updateRelationshipGoal()
            }

            EventConstants.UPDATE_INTERESTS -> {
                updateInterests()
            }

            EventConstants.UPDATE_ZODIAC -> {
                updateZodiac()
            }

            EventConstants.UPDATE_RELIGION -> {
                updateReligion()
            }

            EventConstants.UPDATE_MARITAL_STATUS -> {
                updateMaritalStatus()
            }

            EventConstants.UPDATE_FAMILY_PLAN -> {
                updateFamilyPlan()
            }

            EventConstants.UPDATE_EDUCATION -> {
                updateEducation()
            }
        }
    }

    override fun onPhotoClick() {
        loadKoinModules(googleVisionModule)

        startActivity(Intent(this@ProfileActivity, EditPhotosActivity::class.java))
        swipeRight()
    }
}
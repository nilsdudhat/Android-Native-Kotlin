package com.belive.dating.di

import com.belive.dating.activities.chat.ChatViewModel
import com.belive.dating.activities.dashboard.main.MainViewModel
import com.belive.dating.activities.diamond.DiamondViewModel
import com.belive.dating.activities.diamond.history.DiamondHistoryViewModel
import com.belive.dating.activities.edit_profile.about_me.EditAboutMeViewModel
import com.belive.dating.activities.edit_profile.basics.education.EditEducationViewModel
import com.belive.dating.activities.edit_profile.basics.family_plan.EditFamilyPlanViewModel
import com.belive.dating.activities.edit_profile.basics.marital_status.EditMaritalStatusViewModel
import com.belive.dating.activities.edit_profile.basics.religion.EditReligionViewModel
import com.belive.dating.activities.edit_profile.basics.zodiac.EditZodiacViewModel
import com.belive.dating.activities.edit_profile.your_styles.communication_type.EditCommunicationViewModel
import com.belive.dating.activities.edit_profile.height.EditHeightViewModel
import com.belive.dating.activities.edit_profile.interests.EditInterestsViewModel
import com.belive.dating.activities.edit_profile.languages.EditLanguagesViewModel
import com.belive.dating.activities.edit_profile.lifestyles.diet.EditDietViewModel
import com.belive.dating.activities.edit_profile.lifestyles.drinking.EditDrinkingViewModel
import com.belive.dating.activities.edit_profile.lifestyles.pets.EditPetViewModel
import com.belive.dating.activities.edit_profile.lifestyles.sleeping_habit.EditSleepingHabitViewModel
import com.belive.dating.activities.edit_profile.lifestyles.smoking.EditSmokingViewModel
import com.belive.dating.activities.edit_profile.lifestyles.social_media.EditSocialStatusViewModel
import com.belive.dating.activities.edit_profile.lifestyles.workout.EditWorkoutViewModel
import com.belive.dating.activities.edit_profile.your_styles.love_type.EditLoveTypeViewModel
import com.belive.dating.activities.edit_profile.opposite_gender.EditOppositeGenderViewModel
import com.belive.dating.activities.edit_profile.your_styles.personality_type.EditPersonalityViewModel
import com.belive.dating.activities.edit_profile.profile_images.EditPhotosViewModel
import com.belive.dating.activities.edit_profile.relationship_goal.EditRelationshipGoalViewModel
import com.belive.dating.activities.edit_profile.school.EditSchoolMeViewModel
import com.belive.dating.activities.edit_profile.sexual_orientation.EditSexualOrientationViewModel
import com.belive.dating.activities.filter.FiltersViewModel
import com.belive.dating.activities.filter.location.ChangeLocationViewModel
import com.belive.dating.activities.filter.location.search_location.SearchLocationViewModel
import com.belive.dating.activities.introduction.birthdate.BirthDateViewModel
import com.belive.dating.activities.introduction.choose_interest.ChooseInterestViewModel
import com.belive.dating.activities.introduction.gender.GenderViewModel
import com.belive.dating.activities.introduction.name.NameViewModel
import com.belive.dating.activities.introduction.opposite_gender.OppositeGenderViewModel
import com.belive.dating.activities.introduction.relationship_goal.RelationshipGoalViewModel
import com.belive.dating.activities.introduction.sexual_orientation.SexualOrientationViewModel
import com.belive.dating.activities.introduction.upload_photo.UploadPhotoViewModel
import com.belive.dating.activities.notification.NotificationViewModel
import com.belive.dating.activities.paywalls.subscriptions.subscription.SubscriptionViewModel
import com.belive.dating.activities.paywalls.topups.boost.BoostPaywallViewModel
import com.belive.dating.activities.paywalls.topups.diamond.DiamondPaywallViewModel
import com.belive.dating.activities.paywalls.topups.like.LikePaywallViewModel
import com.belive.dating.activities.paywalls.topups.rewind.RewindPaywallViewModel
import com.belive.dating.activities.paywalls.topups.super_like.SuperLikePaywallViewModel
import com.belive.dating.activities.profile.ProfileViewModel
import com.belive.dating.activities.rejection.PhotosRejectionViewModel
import com.belive.dating.activities.report.ReportViewModel
import com.belive.dating.activities.search_user.SearchUserViewModel
import com.belive.dating.activities.settings.SettingsViewModel
import com.belive.dating.activities.settings.content_visibility.ContentVisibilityViewModel
import com.belive.dating.activities.settings.manage_notifications.ManageNotificationsViewModel
import com.belive.dating.activities.signin.SignInViewModel
import com.belive.dating.activities.user_details.UserDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainViewModel = module {
    viewModel {
        MainViewModel(get())
    }
}

val profileViewModel = module {
    viewModel {
        ProfileViewModel(get())
    }
    viewModel {
        EditPhotosViewModel(get())
    }
    viewModel {
        EditSexualOrientationViewModel(get())
    }
    viewModel {
        EditHeightViewModel(get())
    }
    viewModel {
        EditAboutMeViewModel(get())
    }
    viewModel {
        EditZodiacViewModel(get())
    }
    viewModel {
        EditLoveTypeViewModel(get())
    }
    viewModel {
        EditPetViewModel(get())
    }
    viewModel {
        EditOppositeGenderViewModel(get())
    }
    viewModel {
        EditSchoolMeViewModel(get())
    }
    viewModel {
        EditCommunicationViewModel(get())
    }
    viewModel {
        EditFamilyPlanViewModel(get())
    }
    viewModel {
        EditDietViewModel(get())
    }
    viewModel {
        EditWorkoutViewModel(get())
    }
    viewModel {
        EditSmokingViewModel(get())
    }
    viewModel {
        EditPersonalityViewModel(get())
    }
    viewModel {
        EditReligionViewModel(get())
    }
    viewModel {
        EditSocialStatusViewModel(get())
    }
    viewModel {
        EditSleepingHabitViewModel(get())
    }
    viewModel {
        EditDrinkingViewModel(get())
    }
    viewModel {
        EditMaritalStatusViewModel(get())
    }
    viewModel {
        EditRelationshipGoalViewModel(get())
    }
    viewModel {
        EditInterestsViewModel(get())
    }
    viewModel {
        EditEducationViewModel(get())
    }
    viewModel {
        EditLanguagesViewModel(get())
    }
}

val paywallViewModels = module {
    viewModel {
        SubscriptionViewModel(get())
    }
    viewModel {
        LikePaywallViewModel(get())
    }
    viewModel {
        SuperLikePaywallViewModel(get())
    }
    viewModel {
        RewindPaywallViewModel(get())
    }
    viewModel {
        BoostPaywallViewModel(get())
    }
    viewModel {
        DiamondPaywallViewModel(get())
    }
}

val deepLinkViewModels = module {
    viewModel {
        UserDetailsViewModel(get())
    }
    viewModel {
        ChatViewModel(get())
    }
    viewModel {
        PhotosRejectionViewModel(get())
    }
}

val diamondViewModel = module {
    viewModel {
        DiamondViewModel(get())
    }
    viewModel {
        DiamondHistoryViewModel(get())
    }
}

val filtersViewModel = module {
    viewModel {
        FiltersViewModel(get())
    }
    viewModel {
        ChangeLocationViewModel(get())
    }
    viewModel {
        SearchLocationViewModel(get())
    }
}

val settingsViewModel = module {
    viewModel {
        SettingsViewModel(get())
    }
    viewModel {
        ContentVisibilityViewModel(get())
    }
    viewModel {
        ManageNotificationsViewModel(get())
    }
}

val reportViewModel = module {
    viewModel {
        ReportViewModel(get())
    }
}

val signInViewModel = module {
    viewModel {
        SignInViewModel()
    }
}

val searchUserViewModel = module {
    viewModel {
        SearchUserViewModel()
    }
}

val notificationViewModel = module {
    viewModel {
        NotificationViewModel(get())
    }
}

val introductionViewModels = module {
    viewModel {
        NameViewModel(get())
    }
    viewModel {
        GenderViewModel(get())
    }
    viewModel {
        BirthDateViewModel(get())
    }
    viewModel {
        OppositeGenderViewModel(get())
    }
    viewModel {
        SexualOrientationViewModel(get())
    }
    viewModel {
        RelationshipGoalViewModel(get())
    }
    viewModel {
        ChooseInterestViewModel(get())
    }
    viewModel {
        UploadPhotoViewModel(get())
    }
}
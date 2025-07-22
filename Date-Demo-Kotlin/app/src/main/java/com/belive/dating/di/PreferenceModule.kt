package com.belive.dating.di

import com.belive.dating.preferences.pref_helpers.AdsPrefs
import com.belive.dating.preferences.pref_helpers.GistPrefs
import com.belive.dating.preferences.pref_helpers.IntroductionPrefs
import com.belive.dating.preferences.pref_helpers.UserPrefs
import com.belive.dating.preferences.pref_utils.AdPrefUtils
import com.belive.dating.preferences.pref_utils.GistPrefUtils
import com.belive.dating.preferences.pref_utils.IntroductionPrefUtils
import com.belive.dating.preferences.pref_utils.PrefUtils
import org.koin.dsl.module

val preferenceModule = module {
    single {
        GistPrefUtils(get())
    }
    single {
        GistPrefs
    }
    single {
        AdPrefUtils(get())
    }
    single {
        AdsPrefs
    }
    single {
        PrefUtils(get())
    }
    single {
        UserPrefs
    }
    single {
        IntroductionPrefUtils(get())
    }
    single {
        IntroductionPrefs
    }
}
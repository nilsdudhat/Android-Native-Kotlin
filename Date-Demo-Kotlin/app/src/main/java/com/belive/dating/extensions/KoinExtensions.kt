package com.belive.dating.extensions

import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.fragment.findNavController
import com.belive.dating.preferences.pref_helpers.AdsPrefs
import com.belive.dating.preferences.pref_helpers.GistPrefs
import com.belive.dating.preferences.pref_helpers.IntroductionPrefs
import com.belive.dating.preferences.pref_helpers.UserPrefs
import com.bumptech.glide.RequestManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.Koin
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.Qualifier
import org.koin.mp.KoinPlatform.getKoin
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException

fun getKoinObject(): Koin {
    return getKoin()
}

fun getKoinActivity(): AppCompatActivity {
    return getKoin().get<AppCompatActivity>()
}

fun getKoinContext(): Context {
    return getKoin().get<Context>()
}

@MainThread
inline fun <reified T : ViewModel> AppCompatActivity.getKoinViewModel(
    qualifier: Qualifier? = null,
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline parameters: (() -> ParametersHolder)? = null,
): Lazy<T> {
    return viewModel<T>(qualifier, extrasProducer, parameters)
}

@MainThread
inline fun <reified T : ViewModel> AppCompatActivity.tryKoinViewModel(list: List<Module>): T {
    try {
        val viewModel = getKoinViewModel<T>()
        logger("--koin--", "try")
        return viewModel.value
    } catch (e: Exception) {
        loadKoinModules(list)
        val viewModel = getKoinViewModel<T>()
        logger("--koin--", "catch")
        return viewModel.value
    }
}

fun getGlide(): RequestManager {
    return getKoinObject().get<RequestManager>()
}

fun getUserPrefs(): UserPrefs {
    return getKoinObject().get<UserPrefs>()
}

fun getIntroductionPrefs(): IntroductionPrefs {
    return getKoinObject().get<IntroductionPrefs>()
}

fun getGistPrefs(): GistPrefs {
    return getKoinObject().get<GistPrefs>()
}

fun getAdsPrefs(): AdsPrefs {
    return getKoinObject().get<AdsPrefs>()
}

suspend fun <T> safeApiCallResponse(
    call: suspend () -> Response<T>
): Response<T> {
    try {
        logger("--safeApiCallResponse--", "call")
        return call.invoke()
    } catch (e: SocketTimeoutException) {
        logger("--safeApiCallResponse--", "SocketTimeoutException")
        return Response.error(
            408,
            "Network timeout. Please check your connection.".toResponseBody("text/plain".toMediaType())
        )
    } catch (e: IOException) {
        logger("--safeApiCallResponse--", "IOException")
        return Response.error(
            503,
            "Network error. Please try again.".toResponseBody("text/plain".toMediaType())
        )
    } catch (e: HttpException) {
        logger("--safeApiCallResponse--", "HttpException")
        return Response.error(
            e.code(),
            "Unexpected HTTP error: ${e.code()}".toResponseBody("text/plain".toMediaType())
        )
    } catch (e: Exception) {
        logger("--safeApiCallResponse--", "Exception")
        return Response.error(
            500,
            "An unexpected error occurred.".toResponseBody("text/plain".toMediaType())
        )
    }
}

inline fun <reified T : ViewModel> Fragment.navGraphViewModel(
    @IdRes navGraphId: Int
): Lazy<T> {
    return lazy {
        val navController = findNavController()
        val navBackStackEntry = navController.getBackStackEntry(navGraphId)

        val factory = defaultViewModelProviderFactory

        ViewModelProvider(navBackStackEntry, factory)[T::class.java]
    }
}
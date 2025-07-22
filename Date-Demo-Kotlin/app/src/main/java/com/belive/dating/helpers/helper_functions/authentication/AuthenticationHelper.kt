package com.belive.dating.helpers.helper_functions.authentication

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.LifecycleCoroutineScope
import com.belive.dating.R
import com.belive.dating.di.activityModule
import com.belive.dating.di.adsSettingsModule
import com.belive.dating.di.authenticationHelperModule
import com.belive.dating.di.deepLinkViewModels
import com.belive.dating.di.gistModule
import com.belive.dating.di.glideModule
import com.belive.dating.di.introductionModule
import com.belive.dating.di.nsfwModule
import com.belive.dating.di.paywallViewModels
import com.belive.dating.di.preferenceModule
import com.belive.dating.di.signInViewModel
import com.belive.dating.di.userModule
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.helpers.helper_functions.socket.SocketManager
import com.belive.dating.onesignal.ManualOneSignal
import com.belive.dating.payment.activePlan
import com.belive.dating.preferences.pref_utils.PrefUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import java.security.MessageDigest
import java.util.UUID

fun isUserAvailable(): Boolean {
    val user = Firebase.auth.currentUser
    return user != null
}

class AuthenticationHelper {

    private val auth = Firebase.auth
    private var credentialRequest: GetCredentialRequest
    private val credentialManager by lazy {
        CredentialManager.create(getKoinActivity())
    }

    init {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getKoinContext().getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()

        credentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    fun createAccountWithEmail(email: String, password: String): Flow<JsonObject> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                trySend(JsonObject().apply {
                    addProperty("isSuccess", true)
                })
            } else {
                // handle error
                trySend(JsonObject().apply {
                    addProperty("isSuccess", false)
                    addProperty("message", it.exception?.message ?: "Something went wrong...!")
                })
            }
        }

        awaitClose()
    }

    fun loginWithEmail(email: String, password: String): Flow<JsonObject> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                trySend(JsonObject().apply {
                    addProperty("isSuccess", true)
                })
            } else {
                // handle error
                trySend(JsonObject().apply {
                    addProperty("isSuccess", false)
                    addProperty("message", it.exception?.message ?: "Something went wrong...!")
                })
            }
        }

        awaitClose()
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun signInWithGoogle(
        lifecycleScope: LifecycleCoroutineScope,
        onLoading: (isLoading: Boolean) -> Unit,
        onSuccess: (email: String?, fcmToken: String?) -> Unit,
        onError: (error: String?) -> Unit,
    ) {
        onLoading.invoke(true)

        if (auth.currentUser != null) {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener(
                    OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                onLoading.invoke(false)
                                onError.invoke(task.exception?.message ?: "Something went wrong...!")
                            }
                            return@OnCompleteListener
                        }

                        val fcmToken = task.result

                        lifecycleScope.launch(Dispatchers.Main) {
                            onLoading.invoke(false)
                            onSuccess.invoke(auth.currentUser?.email, fcmToken)
                        }
                    })
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val result = credentialManager.getCredential(getKoinActivity(), credentialRequest)

                    val credential = result.credential

                    if (credential is CustomCredential) {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                                auth.signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            if (auth.currentUser == null) {
                                                lifecycleScope.launch(Dispatchers.Main) {
                                                    onLoading.invoke(false)
                                                    onError.invoke("Authentication Failed")
                                                }
                                            } else {
                                                FirebaseMessaging.getInstance().token
                                                    .addOnCompleteListener(
                                                        OnCompleteListener { task ->
                                                            if (!task.isSuccessful) {
                                                                lifecycleScope.launch(Dispatchers.Main) {
                                                                    onLoading.invoke(false)
                                                                    onError.invoke(task.exception?.message ?: "Something went wrong...!")
                                                                }
                                                                return@OnCompleteListener
                                                            }

                                                            val fcmToken = task.result

                                                            lifecycleScope.launch(Dispatchers.Main) {
                                                                onLoading.invoke(false)
                                                                onSuccess.invoke(googleIdTokenCredential.id, fcmToken)
                                                            }
                                                        })
                                            }
                                        } else {
                                            lifecycleScope.launch(Dispatchers.Main) {
                                                onLoading.invoke(false)
                                                onError.invoke(it.exception?.message ?: "Something went wrong...!")
                                            }
                                        }
                                    }
                            } catch (e: GoogleIdTokenParsingException) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    onLoading.invoke(false)
                                    onError.invoke(e.message ?: "Something went wrong...!")
                                }
                            }
                        }
                    }
                } catch (e: GetCredentialCancellationException) {
                    catchLog("--google-- ${e.printStackTrace()}")

                    lifecycleScope.launch(Dispatchers.Main) {
                        onLoading.invoke(false)
                    }
                } catch (e: GetCredentialProviderConfigurationException) {
                    catchLog("--google-- ${e.printStackTrace()}")

                    tryAgain(lifecycleScope, onLoading, onError, onSuccess)
                } catch (e: NoCredentialException) {
                    catchLog("--google-- ${e.printStackTrace()}")

                    /*lifecycleScope.launch(Dispatchers.Main) {
                        onLoading.invoke(false)
                        onError.invoke("No Google Account found on this device")

                        val intent = Intent(Settings.ACTION_ADD_ACCOUNT)
                        intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                        intent.addCategory(CATEGORY_DEFAULT)
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(FLAG_ACTIVITY_NO_HISTORY)
                        intent.addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        if (intent.resolveActivity(getKoinContext().packageManager) != null) {
                            getKoinActivity().startActivity(intent)
                        } else {
                            Toast.makeText(getKoinContext(), "Cannot add a Google account on this device", Toast.LENGTH_SHORT).show()
                        }
                    }*/

                    tryAgain(lifecycleScope, onLoading, onError, onSuccess)
                } catch (e: Exception) {
                    catchLog("--google-- ${e.printStackTrace()}")

                    lifecycleScope.launch(Dispatchers.Main) {
                        onLoading.invoke(false)
                        onError.invoke("Something went wrong, please try again.")
                    }
                }
            }
        }
    }

    private suspend fun tryAgain(
        lifecycleScope: LifecycleCoroutineScope,
        onLoading: (Boolean) -> Unit,
        onError: (String?) -> Unit,
        onSuccess: (String?, String?) -> Unit,
    ) {
        try {
            val googleIdOption = GetSignInWithGoogleOption.Builder(getKoinContext().getString(R.string.default_web_client_id)).build()

            credentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(getKoinActivity(), credentialRequest)

            val credential = result.credential

            if (credential is CustomCredential) {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    if (auth.currentUser == null) {
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            onLoading.invoke(false)
                                            onError.invoke("Authentication Failed")
                                        }
                                    } else {
                                        FirebaseMessaging.getInstance().token
                                            .addOnCompleteListener(
                                                OnCompleteListener { task ->
                                                    if (!task.isSuccessful) {
                                                        lifecycleScope.launch(Dispatchers.Main) {
                                                            onLoading.invoke(false)
                                                            onError.invoke(task.exception?.message ?: "Something went wrong...!")
                                                        }
                                                        return@OnCompleteListener
                                                    }

                                                    val fcmToken = task.result

                                                    lifecycleScope.launch(Dispatchers.Main) {
                                                        onLoading.invoke(false)
                                                        onSuccess.invoke(googleIdTokenCredential.id, fcmToken)
                                                    }
                                                })
                                    }
                                } else {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        onLoading.invoke(false)
                                        onError.invoke(it.exception?.message ?: "Something went wrong...!")
                                    }
                                }
                            }
                    } catch (e: GoogleIdTokenParsingException) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            onLoading.invoke(false)
                            onError.invoke(e.message ?: "Something went wrong...!")
                        }
                    }
                }
            }
        } catch (e: GetCredentialCancellationException) {
            catchLog("--google-- ${e.printStackTrace()}")

            lifecycleScope.launch(Dispatchers.Main) {
                onLoading.invoke(false)
            }
        } catch (e: GetCredentialProviderConfigurationException) {
            catchLog("--google-- ${e.printStackTrace()}")

            lifecycleScope.launch(Dispatchers.Main) {
                onLoading.invoke(false)
                onError.invoke("Google Sign-In isn't supported on this device.")
            }
        } catch (e: NoCredentialException) {
            catchLog("--google-- ${e.printStackTrace()}")
        }
    }

    fun signOut(lifecycleScope: LifecycleCoroutineScope, onSuccess: () -> Unit) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseAuth.getInstance().signOut()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())

            launch(Dispatchers.Main) {
                onSuccess.invoke()
            }
        }
    }

    fun completeSignOutOnAuthOutSuccess(activity: Activity) {
        SocketManager.disconnect()

        getKoinObject().get<PrefUtils>().clear()

        // logout from OneSignal
        ManualOneSignal.logout()

        stopKoin()

        startKoin {
            printLogger(Level.DEBUG)
            androidContext(activity.applicationContext)
            modules(
                listOf(
                    activityModule,
                    preferenceModule,
                    nsfwModule,
                    glideModule,
                    adsSettingsModule,
                    gistModule,
                    authenticationHelperModule,
                    userModule,
                    deepLinkViewModels,
                    paywallViewModels,
                )
            )
        }

        loadKoinModules(signInViewModel)
        loadKoinModules(introductionModule)

        activePlan = null
    }
}
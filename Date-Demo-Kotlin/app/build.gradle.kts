plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("kotlin-kapt")

    id("kotlin-parcelize")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.belive.dating"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.belive.dating"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

        renderscriptSupportModeEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            ndk {
                debugSymbolLevel = "FULL"
            }
            // Disables PNG crunching for the "release" build type.
            isCrunchPngs = true
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/*"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(project(path = ":ucrop"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.exifinterface)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // to manage multiple requested dex files properly
    implementation(libs.androidx.multidex)

    // Event Track
    implementation(libs.mixpanel.android)

    // for responsive UI
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // encrypted data
    implementation(libs.androidx.security.crypto)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    // Add the dependency for the Firebase SDK for Google Analytics
    implementation(libs.firebase.analytics)
    // Add the dependencies for the Crashlytics and Analytics libraries
    implementation(libs.firebase.crashlytics)
    // Add the dependency for the Firebase Authentication library
    implementation(libs.firebase.auth)
    // Add the dependencies for the Performance Monitoring
    implementation(libs.firebase.perf)

    // For Kotlin users also import the Kotlin extensions library for Play In-App Review:
    implementation(libs.review.ktx)

    // get current location
    implementation(libs.play.services.location)

    // Also add the dependency for the Google Play services library and specify its version
    implementation(libs.play.services.auth)

    // credential manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // places find from text
    implementation(libs.places)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    // Annotation processor
    kapt(libs.androidx.lifecycle.compiler)

    // Dependency Injection with Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.core.coroutines)
    implementation(libs.koin.core.viewmodel)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.navigation)

    // api call
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // default splash
    implementation(libs.androidx.core.splashscreen)

    // blur view
    implementation(libs.blurview)

    // Squircle View
    implementation(libs.squircleview)

    // crop image
//    implementation(libs.yalantis.ucrop)

    // image loading
    implementation(libs.glide)

    // flex layout manager
    implementation(libs.google.flexbox)

    // in app update
    implementation(libs.app.update.ktx)

    // install referrer sdk
    implementation(libs.installreferrer)

    // fragment navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // common notification
    implementation(libs.onesignal)

    // lottie animation
    implementation(libs.lottie)

    // fulda udado
    implementation(libs.konfetti.xml)

    // revenuecat payment
    implementation(libs.purchases)

    // Google Auth
    implementation(libs.google.auth.library.oauth2.http)
    implementation(libs.google.api.services.androidpublisher)

    // socket
    implementation(libs.socket.io.client)

    // Worker Kotlin + coroutines
    implementation(libs.androidx.work.runtime.ktx)

    // tus using file sharing
    implementation(libs.tus.android.client)

    // take selfie with cameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // admob ads
    implementation(libs.play.services.ads)

    // facebook ads
    implementation(libs.audience.network.sdk)

    // ML Kit
    implementation(libs.face.detection)
    implementation(libs.androidx.camera.mlkit.vision)
}
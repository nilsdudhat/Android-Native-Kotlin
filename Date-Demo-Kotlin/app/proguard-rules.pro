#──────────────────────────────────────────────────────────────────────────────
# Project-specific ProGuard rules
#──────────────────────────────────────────────────────────────────────────────

# Preserve enum metadata
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin metadata
-keepattributes Signature, InnerClasses, EnclosingMethod, Annotation, Kotlin

# Preserve any @JavascriptInterface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

#──────────────────────────────────────────────────────────────────────────────
# Third-party libraries
#──────────────────────────────────────────────────────────────────────────────

## Glide
-keep public class com.bumptech.glide.** { *; }
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule

## Lottie
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

## gRPC (io.grpc)
-keep class io.grpc.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

## TUS Android Client
-keep class io.tus.java.client.** { *; }
-dontwarn io.tus.java.client.**

## BlurView
-keep class com.eightbitlab.blurview.** { *; }
-dontwarn com.eightbitlab.blurview.**

## UCrop
-keep class com.yalantis.ucrop.** { *; }
-dontwarn com.yalantis.ucrop.**

## AndroidX Credentials
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

## Retrofit & Gson
-keepclassmembers,allowshrinking,allowobfuscation interface * {
  @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep class com.squareup.retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.annotations.** { *; }
-dontwarn retrofit2.**

-keep interface retrofit2.Call

# Keep Retrofit interfaces (already covered, but reinforce)
-keep class * implements retrofit2.Call { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations

# Gson: Prevent obfuscation of classes used in serialization/deserialization
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

## OkHttp Logging
-dontwarn okhttp3.**
-dontwarn okhttp3.logging.**
-keep class okhttp3.logging.** { *; }

## Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

## WorkManager
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

## Navigation Component
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

## OneSignal
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**

## RevenueCat Purchases
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**

## Mixpanel
-keep class com.mixpanel.android.** { *; }
-dontwarn com.mixpanel.android.**

## ExifInterface
-keep class androidx.exifinterface.** { *; }
-dontwarn androidx.exifinterface.**

## Flexbox
-keep class com.google.android.flexbox.** { *; }
-dontwarn com.google.android.flexbox.**

## Socket.IO
-keep class io.socket.** { *; }
-dontwarn io.socket.**

## CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

## Play Billing
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

## Install Referrer
-keep class com.android.installreferrer.** { *; }
-dontwarn com.android.installreferrer.**

## Places API (com.google.android.libraries.places)
-keep class com.google.android.libraries.places.** { *; }
-dontwarn com.google.android.libraries.places.**

## In-App Update & Review (Play Core)
-keep class com.google.android.play.core.appupdate.** { *; }
-dontwarn com.google.android.play.core.appupdate.**
-keep class com.google.android.play.core.review.** { *; }
-dontwarn com.google.android.play.core.review.**

## Google ID library
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**

## Google API Services – AndroidPublisher
-keep class com.google.api.services.androidpublisher.** { *; }
-dontwarn com.google.api.services.androidpublisher.**

## Google Auth Library
-keep class com.google.auth.** { *; }
-dontwarn com.google.auth.**

## Multidex
-keep class androidx.multidex.** { *; }

## SDP & SSP (Intuit)
-keep class com.intuit.sdp.** { *; }
-keep class com.intuit.ssp.** { *; }

## Konfetti
-keep class nl.dionsegijn.konfetti.** { *; }
-dontwarn nl.dionsegijn.konfetti.**

## SquircleView (Smooth corners)
-keep class app.juky.squircleview.** { *; }
-dontwarn app.juky.squircleview.**

## RoundedImageView
-keep class com.makeramen.roundedimageview.** { *; }

#──────────────────────────────────────────────────────────────────────────────
# General Android / Renderscript / JSON
#──────────────────────────────────────────────────────────────────────────────

-keep class com.belive.dating.api.** { *; }
-keep class com.belive.dating.helpers.helper_functions.socket.** { *; }

# keeping some of classes and functions
-keep class com.belive.dating.helpers.helper_functions.** { *; }

# Keep Kotlin data classes in your app package
-keep class com.belive.dating.api.user.models.** { *; }
-keepclassmembers class com.belive.dating.api.user.models.** {
    <fields>;
    <methods>;
}

# Keep Kotlin data classes in your app package
-keep class com.belive.dating.api.introduction.models.** { *; }
-keepclassmembers class com.belive.dating.api.introduction.models.** {
    <fields>;
    <methods>;
}

-dontwarn android.net.http.**
-keep class android.net.http.**{ *; }

-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite
-dontwarn com.google.api.client.http.GenericUrl
-dontwarn com.google.api.client.http.HttpHeaders
-dontwarn com.google.api.client.http.HttpRequest
-dontwarn com.google.api.client.http.HttpRequestFactory
-dontwarn com.google.api.client.http.HttpResponse
-dontwarn com.google.api.client.http.HttpTransport
-dontwarn com.google.api.client.http.javanet.NetHttpTransport$Builder
-dontwarn com.google.api.client.http.javanet.NetHttpTransport
-dontwarn org.joda.time.Instant

# Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlin.coroutines.jvm.internal.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class com.google.gson.annotations.** { *; }

# Renderscript
-keep class android.support.v8.renderscript.** { *; }
-keep class androidx.renderscript.** { *; }

# JSON
-keep class org.json.** { *; }

# Preference
-dontwarn androidx.preference.**

# Guava
-keep class com.google.common.** { *; }

# Cronet & SSL Providers (if used)
-keep class org.chromium.** { *; }
-keep class com.google.** { *; }
-keep class org.conscrypt.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep class org.apache.harmony.** { *; }
-keep class com.android.org.conscrypt.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class org.conscrypt.** { *; }
-keep class com.google.android.gms.** { *; }
-keepclasseswithmembers class * {
    native <methods>;
}

# Swing / AWT stubs
-dontwarn javax.swing.**
-dontwarn java.awt.**

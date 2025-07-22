// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.3" apply false

    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "3.0.5" apply false

    // Add the dependency for the Performance Monitoring Gradle plugin
    id("com.google.firebase.firebase-perf") version "2.0.0" apply false
}
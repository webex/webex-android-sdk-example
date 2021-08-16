package com.ciscowebex.androidsdk.build

object Versions {
    const val kotlin = "1.3.72"
    const val koin= "2.1.3"

    const val ndkVersion = "21.3.6528147"
    const val buildTools = "29.0.3"

    const val compileSdk = 29
    const val targetSdk = 29
    const val minSdk = 24

    const val cmake = "3.12.4+"
    const val dokka = "0.9.18"
}

object Dependencies {
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val koin = "org.koin:koin-android:${Versions.koin}"
    const val koinViewModel = "org.koin:koin-android-viewmodel:${Versions.koin}"
    const val rxjava = "io.reactivex.rxjava2:rxjava:2.2.11"
    const val rxandroid = "io.reactivex.rxjava2:rxandroid:2.1.1"
    const val rxkotlin = "io.reactivex.rxjava2:rxkotlin:2.4.0"
    const val coreKtx = "androidx.core:core-ktx:1.3.0"
    const val appCompat = "androidx.appcompat:appcompat:1.2.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val okhttp = "com.squareup.okhttp3:okhttp:3.0.1"
    const val material = "com.google.android.material:material:1.1.0"
    const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"
    const val cardview = "androidx.cardview:cardview:1.0.0"
    const val viewpager2 = "androidx.viewpager2:viewpager2:1.0.0"
    const val swiperefresh = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    const val media = "androidx.media:media:1.1.0"
    const val nimbusJosh = "com.nimbusds:nimbus-jose-jwt:9.0"

    const val preferences = "androidx.preference:preference-ktx:1.1.1"
    const val firebaseBom = "com.google.firebase:firebase-bom:26.1.0"
    const val firebaseMessaging = "com.google.firebase:firebase-messaging"
    const val firebaseAnalytics = "com.google.firebase:firebase-analytics-ktx"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-ktx"
    const val gson = "com.google.code.gson:gson:2.8.6"

    object Test {
        const val junit = "junit:junit:4.12"
        const val androidxJunit = "androidx.test.ext:junit:1.1.2"
        const val espressoCore = "androidx.test.espresso:espresso-core:3.3.0"
        const val espressoContrib = "androidx.test.espresso:espresso-contrib:3.1.0"
        const val espressoWeb = "androidx.test.espresso:espresso-web:3.3.0"
        const val espressoIntents = "androidx.test.espresso:espresso-intents:3.3.0"
        const val fragmentScenerio = "androidx.fragment:fragment-testing:1.2.5"
        const val rules = "androidx.test:rules:1.3.0"
        const val testExt = "androidx.test:core-ktx:1.3.0"
        const val mockk = "io.mockk:mockk-android:1.10.6"
    }
}